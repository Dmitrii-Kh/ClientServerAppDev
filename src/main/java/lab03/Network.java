package lab03;

import com.google.common.primitives.UnsignedLong;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Network {

    private BufferedOutputStream outputStream;
    private InputStream          inputStream;
    private int          maxTimeout;
    private TimeUnit     timeUnit;

    private ArrayList<Byte>     receivedBytes = new ArrayList<>(Packet.HEADER_LENGTH * 3);
    private LinkedList<Integer> bMagicIndexes = new LinkedList<>();

    private Object outputStreamLock = new Object();
    private Object inputStreamLock  = new Object();

    public Network(InputStream inputStream, OutputStream outputStream, int maxTimeout, TimeUnit timeUnit) {
        this.inputStream = inputStream;
        this.outputStream = new BufferedOutputStream(outputStream);

        this.maxTimeout = Math.max(maxTimeout, 0);
        this.timeUnit = timeUnit;
    }


    /**
     * @return {@code packetBytes} if packet received successfully
     * @throws IOException
     * @throws TimeoutException if no data received after {@code maxTimeout}
     */
    public byte[] receive() throws IOException, TimeoutException, BadPaddingException, IllegalBlockSizeException {
        synchronized (inputStreamLock) {
            int    wLen    = 0;
            byte[] oneByte = new byte[1];

            byte[] packetBytes = null;

            boolean newData = true;

            while (true) {
                if (inputStream.available() == 0) {
                    if (!newData) {
                        throw new TimeoutException();
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

                if (Packet.B_MAGIC.equals(oneByte[0]) && receivedBytes.size() > 0) {
                    bMagicIndexes.add(receivedBytes.size());
                }
                receivedBytes.add(oneByte[0]);

                //check message if no errors in header
                if (receivedBytes.size() == Packet.HEADER_LENGTH + wLen + Packet.CRC16_LENGTH) {
                    final short wCrc16_2 = ByteBuffer.allocate(2).put(receivedBytes.get(receivedBytes.size() - 2))
                            .put(receivedBytes.get(receivedBytes.size() - 1)).rewind().getShort();

                    packetBytes = toPrimitiveByteArr(receivedBytes.toArray(new Byte[0]));
                    final short crc2Evaluated = CRC16.evaluateCrc(packetBytes, Packet.HEADER_LENGTH,
                            receivedBytes.size() - 2);

                    if (wCrc16_2 == crc2Evaluated) {
                        receivedBytes.clear();
                        bMagicIndexes.clear();
                        return packetBytes;

                    } else {
//                        System.out.println("message reset");
                        wLen = 0;
                        resetToFirstBMagic();
                    }

                    //check header
                } else if (receivedBytes.size() >= Packet.HEADER_LENGTH) {

                    final short wCrc16_1 = ByteBuffer.allocate(2).put(receivedBytes.get(Packet.HEADER_LENGTH - 2))
                                .put(receivedBytes.get(Packet.HEADER_LENGTH - 1)).rewind().getShort();

                    final short crc1Evaluated =
                            CRC16.evaluateCrc(toPrimitiveByteArr(receivedBytes.toArray(new Byte[0])), 0, 14);

                    if (wCrc16_1 == crc1Evaluated) {
                        wLen = ByteBuffer.allocate(4).put(receivedBytes.get(10)).put(receivedBytes.get(11))
                                .put(receivedBytes.get(12)).put(receivedBytes.get(13)).rewind().getInt();

                    } else {
//                      System.out.println("header reset");
                        resetToFirstBMagic();
                        Packet ansPac = new Packet((byte) 0, UnsignedLong.ONE, new Message(Message.cTypes.EXCEPTION_FROM_SERVER,0, "Corrupted header!"));
//                        return ansPac.toPacket();
                        send(ansPac.toPacket());
                    }
                }
            }
        }
    }

    public void send(byte[] msg) throws IOException {
        synchronized (outputStreamLock) {
            outputStream.write(msg);
            outputStream.flush();
        }
    }

    //todo check
    private void resetToFirstBMagic() {
//        System.out.println(receivedBytes.toString());
        //todo notify client???
//        try {
//            send("!!!bad packet".getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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

}
