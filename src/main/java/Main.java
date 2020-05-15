//import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import org.apache.commons.codec.DecoderException;
//import org.apache.commons.codec.binary.Hex;
//import com.google.gson.Gson;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;


public class Main {

    public static void main(String[] args) {

        PacketDecoder.decodePacket(PacketEncoder.encode((byte)0,1, 777,21, "Hello World!"));

        //decodePacket(encode());
    }

/*
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

        decodeMessage(message, messageLength);
    }

    public static byte[] encode() throws DecoderException {

        //JSON -> String from JSON -> byte array -> encrypted byte array -> message ready to transmit

        //imitation of incoming json object
        String jsonText = "{'text':'lorem ipsum'}";
        Gson gson = new Gson();
        final String json = gson.toJson(jsonText);    //serialization
        // System.out.println("json here : " + json);

        //deserialization
        final String usefulData   = gson.fromJson(json, String.class);

        final byte[] messageBytes = usefulData.getBytes(StandardCharsets.UTF_8);

        //encryption
        byte[] encryptedMessage = encryptMessage(messageBytes);

        final String inputMessage = ("00000001 00000010" + Hex.encodeHexString(encryptedMessage)).replace(" ", "");

        final byte[] myMessage = Hex.decodeHex(inputMessage);

        final byte[] header =
        new byte[]{0x13, 0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0A, 0x0, 0x0, 0x0, (byte) myMessage.length};

        return ByteBuffer.allocate(10000).put(header).putShort(CRC16.evaluateCrc(header, 0, header.length))
                .put(myMessage).putShort(CRC16.evaluateCrc(myMessage, 0, myMessage.length)).array();
    }


    private static void decodeMessage(final byte[] inputMessage, int messageLength) {
        final int commandType = ByteBuffer.wrap(inputMessage, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("Command type : " + commandType);

        final int userId = ByteBuffer.wrap(inputMessage, 4, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("User ID : " + userId);

        byte[] message = new byte[messageLength - 8];
        System.arraycopy(inputMessage, 8, message, 0, messageLength - 8);

        //decrypt
        byte[] decryptedMessage = decryptMessage(message);

        //imitation of return of transmitted json
//        Gson gson = new Gson();
//        final String outputJsonObj = gson.toJson(new String(decryptedMessage));
//        System.out.println("Output json obj here : " + outputJsonObj);

        System.out.println("Useful Data from client : " + new String(decryptedMessage));
    }
*/



/*
    public static byte[] encryptMessage(final byte[] message) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        try {
            return cipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[] decryptMessage(final byte[] message) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        try {
            return cipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    } */

}
