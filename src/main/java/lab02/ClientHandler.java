package lab02;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
//    private OutputStream clientOutputStream;
//    private InputStream  clientInputStream;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {

        //todo packetBytes.get(..) << 8 * ..) -> ByteBuffer (big-endian)
        try {

            Integer wLen = 0;
//            Boolean packetIncomplete = true;

            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            byte[] oneByte = new byte[1];

            ArrayList<Byte> inputStreamBytes = new ArrayList<>(Packet.HEADER_LENGTH * 3);

            LinkedList<Integer> bMagicIndexes = new LinkedList<>();

            byte[] packetBytes = null;


            while (packetBytes == null && (input.read(oneByte)) != -1) {

                if (Packet.B_MAGIC.equals(oneByte[0])) {
                    bMagicIndexes.add(inputStreamBytes.size());
                }
                inputStreamBytes.add(oneByte[0]);

                //check header
                if (inputStreamBytes.size() == Packet.HEADER_LENGTH) {
                    Integer wCrc16_1 =
                            (inputStreamBytes.get(inputStreamBytes.size() - 2) << 8) | inputStreamBytes.get(inputStreamBytes.size() - 1);


                    final short crc1Evaluated =
                            CRC16.evaluateCrc(toPrimitiveByteArr(inputStreamBytes.toArray(new Byte[0])), 0, 14);

                    if (wCrc16_1 == crc1Evaluated) {

                        wLen = (inputStreamBytes.get(10) << 8 * 3) | (inputStreamBytes.get(11) << 8 * 2) |
                                (inputStreamBytes.get(12) << 8) | inputStreamBytes.get(13);

                    } else {
                        resetToFirstBMagic(inputStreamBytes, bMagicIndexes);
                    }


                    //check message if no errors in header
                } else if (inputStreamBytes.size() == Packet.HEADER_LENGTH + wLen + Packet.CRC16_LENGTH) {
                    final Integer wCrc16_2 =
                            (inputStreamBytes.get(inputStreamBytes.size() - 2) << 8) | inputStreamBytes.get(inputStreamBytes.size() - 1);

                    packetBytes = toPrimitiveByteArr(inputStreamBytes.toArray(new Byte[0]));
                    final short crc2Evaluated = CRC16.evaluateCrc(packetBytes,
                            Packet.HEADER_LENGTH, inputStreamBytes.size() - Packet.CRC16_LENGTH);

                    if (wCrc16_2 == crc2Evaluated) {
//                        packetIncomplete = false;
                        break;

                    } else {
                        wLen = 0;
                        packetBytes = null;
                        resetToFirstBMagic(inputStreamBytes, bMagicIndexes);
                    }
                }
            }

            //todo Process(new Packet(packetBytes));
            //new Processor(new Packet(packetBytes).getBMsq());


            //todo try-finally closing
            clientSocket.close();
            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void resetToFirstBMagic(ArrayList<Byte> inputStreamBytes, LinkedList<Integer> bMagicIndexes) {
        //todo notify client

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


}
