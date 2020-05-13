import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws DecoderException {
        final String myMessage = "my plain text 11";
        final byte[] messageBytes = myMessage.getBytes(StandardCharsets.UTF_8);
        final String inputMessage = ("13 01 0000000000000001 00000010 5E2C"
                + Hex.encodeHexString(messageBytes)).replace(" ", "") + "4FC7";
        //decode(Hex.decodeHex(inputMessage));
        decode(encode());

    }

    private static void decode(final byte[] inputMessage){
        if(inputMessage[0] != 0x13){
            throw new IllegalArgumentException("Invalid magic byte");
        }

        final int clientId = inputMessage[1] & 0xFF;
        System.out.println("Client ID : " + clientId);

        final long packetId = ByteBuffer.wrap(inputMessage,2,8)
                .order(ByteOrder.BIG_ENDIAN)
                .getLong();
        System.out.println("Packet Id : " + packetId);

        final int messageLength = ByteBuffer.wrap(inputMessage,10,4)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
        System.out.println("Message length : " + messageLength);

        final short crc1 = ByteBuffer.wrap(inputMessage,14,2)
                .order(ByteOrder.BIG_ENDIAN)
                .getShort();
        System.out.println("CRC1 : " + crc1);

        final short crc1Evaluated = CRC16.evaluateCrc(inputMessage,0,14);
        if(crc1Evaluated != crc1){
            throw new IllegalArgumentException("CRC1 expected : " + crc1Evaluated + ", but was : " + crc1);
        }

        byte[] message = new byte[messageLength];
        System.arraycopy(inputMessage, 16, message,0, messageLength);
        System.out.println("Message from client : " + new String(message));

        final short crc2 = ByteBuffer.wrap(inputMessage,16 + messageLength,2)
                .order(ByteOrder.BIG_ENDIAN)
                .getShort();
        System.out.println("CRC2 : " + crc2);


        final short crc2Evaluated = CRC16.evaluateCrc(message,0,messageLength);
        if(crc2Evaluated != crc2){
            throw new IllegalArgumentException("CRC2 expected : " + crc2Evaluated + ", but was : " + crc2);
        }

    }

    public static byte[] encode(){
        final byte[] myMessage = "server response".getBytes(StandardCharsets.UTF_8);
        final byte[] header = new byte[] {
                0x13,
                0,
                0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0A,
                0x0,0x0,0x0,(byte)myMessage.length
        };

        return ByteBuffer.allocate(10000)
                .put(header)
                .putShort(CRC16.evaluateCrc(header,0,header.length))
                .put(myMessage)
                .putShort(CRC16.evaluateCrc(myMessage,0,myMessage.length))
                .array();

    }
}
