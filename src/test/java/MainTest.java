import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MainTest {


    ByteBuffer buffer;
    private static byte[] byteArr;


    @BeforeAll
    static void init() {
        byteArr = PacketEncoder.encode((byte) 0, 1, 777, 21, "Hello World!");
    }

    @Test
        //check for validity of CRC1 & CRC2
    void shouldPass_whenValidInputMessage() {
        PacketDecoder.decodePacket(byteArr);
    }

    @Test
        //check for validity of CRC1 & CRC2
    void shouldPass_whenValidInputMessag() {
        PacketDecoder.decodePacket(byteArr);
    }

    @Test
    void shouldThrowException_whenInvalidMagicByte() {
        assertThrows(IllegalArgumentException.class, () -> PacketDecoder.decodePacket(Hex.decodeHex("12")));
    }

    @Test
    void shouldPass_whenValidPacketSource() {
        buffer = ByteBuffer.wrap(byteArr);
        assertEquals(0, buffer.get(1), "Invalid packet source!");
    }

    @Test
    void shouldPass_ifCryptorWorksNice() throws UnsupportedEncodingException {
        String message = "message";

        try {
            assertEquals(message, new String(Cryptor.decryptMessage(Cryptor.encryptMessage(message.getBytes("UTF-8")))),
                    "Cryptor doesn't work");
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    @Test
    void shouldThrowException_whenInvalid_first_CRC16() throws DecoderException {
        byte[] packet = PacketEncoder.encode((byte) 0, 1, 777, 21, "Hello World!");

        //break header
        packet[10] = (byte) (packet[10] - 1);

        assertThrows(IllegalArgumentException.class, () -> PacketDecoder.decodePacket(packet));
    }

    @Test
    void shouldThrowException_whenInvalid_second_CRC16() throws DecoderException {
        byte[] packet = PacketEncoder.encode((byte) 0, 1, 777, 21, "message");

        //break message
        packet[packet.length - 3] = (byte) (packet[packet.length - 3] - 1);


        assertThrows(IllegalArgumentException.class, () -> PacketDecoder.decodePacket(packet));
    }


/*
    private boolean areArraysEqual(final byte[] arr1, final byte[] arr2){
      boolean res = true;
        for (int i = 0; i < arr1.length; ++i) {
            res &= arr1[i] == arr2[i];
        }
        return res;
    }

    @Test
    void shouldPass_whenEncryptedDecryptedCorrectly(){
        Main.initCipher();  //todo make private
        Main.initKey();

        final String usefulData   = "Useful data";
        final byte[] messageBytes = usefulData.getBytes(StandardCharsets.UTF_8);
        final byte[] messageEncryptedDecrypted = Main.decryptMessage(Main.encryptMessage(messageBytes));
        assertTrue(areArraysEqual(messageBytes, messageEncryptedDecrypted));
    }
    */

}
