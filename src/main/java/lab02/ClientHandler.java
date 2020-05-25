package lab02;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {

    private Socket       clientSocket;
    private OutputStream outputStream;
    private InputStream  inputStream;


    private int      maxTimeout;
    private TimeUnit timeUnit;

    ArrayList<Byte>     receivedBytes = new ArrayList<>(Packet.HEADER_LENGTH * 3);
    LinkedList<Integer> bMagicIndexes = new LinkedList<>();

    Object outputStreamLock = new Object();

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);


    public ClientHandler(Socket clientSocket, int maxTimeout, TimeUnit timeUnit) throws IOException {
        if (maxTimeout <= 0) throw new IllegalArgumentException("maxTimeout should be > 0");

        this.clientSocket = clientSocket;
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();

        this.maxTimeout = maxTimeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(Thread.currentThread().getName() + " - ClientHandler");
        try {
            int    wLen    = 0;
            byte[] oneByte = new byte[1];


            byte[] packetBytes = null;

            boolean newData = true;


            while (true) {
                if (inputStream.available() == 0) {
                    if (!newData) {
                        break;
                    }
                    newData = false;

                    try {
                        timeUnit.sleep(maxTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }


                inputStream.read(oneByte);
                newData = true;

                if (Packet.B_MAGIC.equals(oneByte[0])) {
                    bMagicIndexes.add(receivedBytes.size());
                }
                receivedBytes.add(oneByte[0]);

                //check header
                if (receivedBytes.size() == Packet.HEADER_LENGTH) {
                    final short wCrc16_1 = (short) ((receivedBytes.get(receivedBytes.size() - 2) << 8) |
                            receivedBytes.get(receivedBytes.size() - 1));

                    final short crc1Evaluated =
                            CRC16.evaluateCrc(toPrimitiveByteArr(receivedBytes.toArray(new Byte[0])), 0, 14);

                    if (wCrc16_1 == crc1Evaluated) {
                        wLen = (receivedBytes.get(10) << 8 * 3) | (receivedBytes.get(11) << 8 * 2) |
                                (receivedBytes.get(12) << 8) | receivedBytes.get(13);

                    } else {
                        resetToFirstBMagic();
                    }

                    //check message if no errors in header
                } else if (receivedBytes.size() == Packet.HEADER_LENGTH + wLen + Packet.CRC16_LENGTH) {
                    final int wCrc16_2 = (receivedBytes.get(receivedBytes.size() - 2) << 8) |
                            receivedBytes.get(receivedBytes.size() - 1);

                    packetBytes = toPrimitiveByteArr(receivedBytes.toArray(new Byte[0]));
                    final short crc2Evaluated = CRC16.evaluateCrc(packetBytes, Packet.HEADER_LENGTH,
                            receivedBytes.size() - Packet.CRC16_LENGTH);

                    if (wCrc16_2 == crc2Evaluated) {
                        receivedBytes.clear();
                        bMagicIndexes.clear();

                    } else {
                        wLen = 0;
                        packetBytes = null;
                        resetToFirstBMagic();
                    }
                }


                if (packetBytes != null) {
                    handlePacketBytes(Arrays.copyOf(packetBytes, packetBytes.length));
                }
            }
        } catch (IOException e) {
            if (e.getMessage().equals("Stream closed.")) {
                //todo notify client
            } else {
                e.printStackTrace();
            }
        } finally {
            shutdown();
        }
    }

    private void handlePacketBytes(byte[] packetBytes) {
        CompletableFuture.supplyAsync(() -> {
            //to encode in parallel thread
            Packet packet = null;
            try {
                packet = new Packet(packetBytes);
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            return packet;
        }, executor)

                .thenAcceptAsync((inputPacket -> {
                    Packet answerPacket = null;
                    try {
                        answerPacket = Processor.process(inputPacket);
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }

                    try {
                        send(answerPacket.toPacket());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }), executor);
    }


    private void resetToFirstBMagic() {
        //todo notify client
        try {
            send("!!!bad packet".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //reset to first bMagic if exists
        if (!bMagicIndexes.isEmpty()) {
            int firstMagicByteIndex = bMagicIndexes.poll();

            ArrayList<Byte> tmp = new ArrayList<>(receivedBytes.size());

            for (int i = firstMagicByteIndex; i < receivedBytes.size(); ++i) {
                tmp.add(receivedBytes.get(i));
            }

            receivedBytes = tmp;

        } else {
            receivedBytes.clear();
        }
    }


    //todo create class Utils
    private byte[] toPrimitiveByteArr(Byte[] objArr) {
        byte[] primitiveArr = new byte[objArr.length];

        for (int i = 0; i < objArr.length; ++i) {
            primitiveArr[i] = objArr[i];
        }

        return primitiveArr;
    }


    public void send(byte[] msg) throws IOException {
        synchronized (outputStreamLock) {
            outputStream.write(msg);
        }
    }

    public void shutdown() {
        //todo shutdown
        System.out.println(Thread.currentThread().getName() + " shutdown");

        //close inputStream
        try {
            if (inputStream.available() > 0) {
                Thread.sleep(5000);
            }
            inputStream.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        } finally {
            if (executor.getActiveCount() > 0) {
                try {
                    executor.awaitTermination(2, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();

            try {
                try {
                    outputStream.close();
                } finally {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
