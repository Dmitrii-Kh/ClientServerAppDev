import org.apache.commons.codec.DecoderException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;

public class PacketEncoder {

    private static Key key;
    private static Cipher cipher;

    public static void setEncryption(Key _key, Cipher _cipher){
        key = _key;
        cipher = _cipher;
    }


    public static byte[] encode(byte source, long packetId, int commandType, int userId, String message) throws DecoderException {

//              *imitation of incoming json object*
//
// JSON -> String from JSON -> byte array -> encrypted byte array -> message ready to transmit
//
//        String jsonText = "{'text':'lorem ipsum'}";
//        Gson gson = new Gson();
//        final String json = gson.toJson(jsonText);    //serialization
//        // System.out.println("json here : " + json);
//
//      //deserialization
        //final String usefulData   = gson.fromJson(json, String.class);


        final byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        //encryption
        byte[] encryptedMessage = encryptMessage(messageBytes);

        final byte[] myMessage = ByteBuffer.allocate(8+encryptedMessage.length)
                .putInt(commandType).putInt(userId).put(encryptedMessage).array();

        byte[] header = ByteBuffer.allocate(14).put((byte)0x13).put(source).putLong(packetId).putInt(myMessage.length).array();

        return ByteBuffer.allocate(10000).put(header).putShort(CRC16.evaluateCrc(header, 0, header.length))
                .put(myMessage).putShort(CRC16.evaluateCrc(myMessage, 0, myMessage.length)).array();

    }


    private static byte[] encryptMessage(final byte[] message) {
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

}
