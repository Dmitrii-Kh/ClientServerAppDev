import org.apache.commons.codec.DecoderException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;

public class PacketEncoder {



    public static byte[] encode(byte source, long packetId, int commandType, int userId, String message) {

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

        byte[] encryptedMessage = new byte[0];
        try {
            encryptedMessage = Cryptor.encryptMessage(messageBytes);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        final byte[] myMessage = ByteBuffer.allocate(8+encryptedMessage.length)
                .putInt(commandType).putInt(userId).put(encryptedMessage).array();

        byte[] header = ByteBuffer.allocate(14).put((byte)0x13).put(source).putLong(packetId).putInt(myMessage.length).array();

        return ByteBuffer.allocate(18 + myMessage.length).put(header).putShort(CRC16.evaluateCrc(header, 0, header.length))
                .put(myMessage).putShort(CRC16.evaluateCrc(myMessage, 0, myMessage.length)).array();

    }


}
