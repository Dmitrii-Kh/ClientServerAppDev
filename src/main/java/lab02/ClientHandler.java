package lab02;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {

    private Socket       clientSocket;
    private OutputStream output;
    private InputStream  input;

    private int maxTimeout;
    private TimeUnit timeUnit;

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

    ArrayList<Byte>     inputStreamBytes = new ArrayList<>(Packet.HEADER_LENGTH * 3);
    LinkedList<Integer> bMagicIndexes    = new LinkedList<>();


    public ClientHandler(Socket clientSocket, int maxTimeout, TimeUnit timeUnit) throws IOException {
        if(maxTimeout <= 0)
            throw new IllegalArgumentException("maxTimeout should be > 0");

        this.clientSocket = clientSocket;
        input = clientSocket.getInputStream();
        output = clientSocket.getOutputStream();

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
                if(input.available() == 0){
                    if(!newData){
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


                input.read(oneByte);
                newData = true;

                if (Packet.B_MAGIC.equals(oneByte[0])) {
                    bMagicIndexes.add(inputStreamBytes.size());
                }
                inputStreamBytes.add(oneByte[0]);

                //check header
                if (inputStreamBytes.size() == Packet.HEADER_LENGTH) {
                    final short wCrc16_1 = (short) ((inputStreamBytes.get(inputStreamBytes.size() - 2) << 8) |
                            inputStreamBytes.get(inputStreamBytes.size() - 1));

                    final short crc1Evaluated =
                            CRC16.evaluateCrc(toPrimitiveByteArr(inputStreamBytes.toArray(new Byte[0])), 0, 14);

                    if (wCrc16_1 == crc1Evaluated) {
                        wLen = (inputStreamBytes.get(10) << 8 * 3) | (inputStreamBytes.get(11) << 8 * 2) |
                                (inputStreamBytes.get(12) << 8) | inputStreamBytes.get(13);

                    } else {
                        resetToFirstBMagic();
                    }

                    //check message if no errors in header
                } else if (inputStreamBytes.size() == Packet.HEADER_LENGTH + wLen + Packet.CRC16_LENGTH) {
                    final int wCrc16_2 = (inputStreamBytes.get(inputStreamBytes.size() - 2) << 8) |
                            inputStreamBytes.get(inputStreamBytes.size() - 1);

                    packetBytes = toPrimitiveByteArr(inputStreamBytes.toArray(new Byte[0]));
                    final short crc2Evaluated = CRC16.evaluateCrc(packetBytes, Packet.HEADER_LENGTH,
                            inputStreamBytes.size() - Packet.CRC16_LENGTH);

                    if (wCrc16_2 == crc2Evaluated) {
                        inputStreamBytes.clear();
                        bMagicIndexes.clear();

                    } else {
                        wLen = 0;
                        packetBytes = null;
                        resetToFirstBMagic();
                    }
                }


                if(packetBytes != null){
                    //todo Process(new Packet(packetBytes));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                send(toPrimitiveByteArr(inputStreamBytes.toArray(new Byte[0])));
            } catch (IOException e) {
                e.printStackTrace();
            }
            shutdown();
        }
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

            ArrayList<Byte> tmp = new ArrayList<>(inputStreamBytes.size());

            for (int i = firstMagicByteIndex; i < inputStreamBytes.size(); ++i) {
                tmp.add(inputStreamBytes.get(i));
            }

            inputStreamBytes = tmp;

        } else {
            inputStreamBytes.clear();
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


    public void shutdown() {
        //todo shutdown
        System.out.println(Thread.currentThread().getName() + " shutdown");
        if(executor.getActiveCount() > 0) {
            try {
                executor.awaitTermination(2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            executor.shutdown();
        }

        try {
            try {
                input.close();
                output.close();
            } finally {
                clientSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] msg) throws IOException {
        output.write(msg);
    }


}
