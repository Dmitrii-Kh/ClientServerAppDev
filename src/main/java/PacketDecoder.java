import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.Key;

public class PacketDecoder {


    public static void decodePacket(final byte[] inputMessage) {
        if (inputMessage[0] != 0x13) {
            throw new IllegalArgumentException("Invalid magic byte");
        }

        final int clientId = inputMessage[1] & 0xFF;
        System.out.println("Client ID : " + clientId);

        final long packetId = ByteBuffer.wrap(inputMessage, 2, 8).order(ByteOrder.BIG_ENDIAN).getLong();
        System.out.println("Packet Id : " + packetId);

        final int messageLength = ByteBuffer.wrap(inputMessage, 10, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("Message length : " + messageLength);

        final short crc1 = ByteBuffer.wrap(inputMessage, 14, 2).order(ByteOrder.BIG_ENDIAN).getShort();
        System.out.println("CRC1 : " + crc1);

        final short crc1Evaluated = CRC16.evaluateCrc(inputMessage, 0, 14);
        if (crc1Evaluated != crc1) {
            throw new IllegalArgumentException("CRC1 expected : " + crc1Evaluated + ", but was : " + crc1);
        }

        byte[] message = new byte[messageLength];
        System.arraycopy(inputMessage, 16, message, 0, messageLength);
        System.out.println("Message from client (deprecated) : " + new String(message));

        final short crc2 = ByteBuffer.wrap(inputMessage, 16 + messageLength, 2).order(ByteOrder.BIG_ENDIAN).getShort();
        System.out.println("CRC2 : " + crc2);


        final short crc2Evaluated = CRC16.evaluateCrc(message, 0, messageLength);
        if (crc2Evaluated != crc2) {
            throw new IllegalArgumentException("CRC2 expected : " + crc2Evaluated + ", but was : " + crc2);
        }

        decodeMessage(message);
    }


    private static void decodeMessage(final byte[] inputMessage) {
        final int commandType = ByteBuffer.wrap(inputMessage, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("Command type : " + commandType);

        final int userId = ByteBuffer.wrap(inputMessage, 4, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("User ID : " + userId);

        byte[] message = new byte[inputMessage.length - 8];
        System.arraycopy(inputMessage, 8, message, 0, inputMessage.length - 8);

        //decrypt
        byte[] decryptedMessage = new byte[0];
        try {
            decryptedMessage = Cryptor.decryptMessage(message);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        //imitation of return of transmitted json
//        Gson gson = new Gson();
//        final String outputJsonObj = gson.toJson(new String(decryptedMessage));
//        System.out.println("Output json obj here : " + outputJsonObj);

        System.out.println("Useful Data from client : " + new String(decryptedMessage));
    }



}
