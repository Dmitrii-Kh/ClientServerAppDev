import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    private static String algorithm = "DESede";
    private static Key key;
    private static Cipher cipher;

    ByteBuffer buffer;
    private static byte[] byteArr;



    private static void initCipher() {
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private static void initKey() {
        try {
            key = KeyGenerator.getInstance(algorithm).generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    static void init() throws DecoderException{
        initKey();
        initCipher();
        PacketEncoder.setEncryption(key, cipher);
        PacketDecoder.setEncryption(key, cipher);
        byteArr = PacketEncoder.encode((byte)0,1, 777,21, "Hello World!");
    }

    @Test    //check for validity of CRC1 & CRC2
    void shouldPass_whenValidInputMessage() {
        PacketDecoder.decodePacket(byteArr);
    }

    @Test
    void shouldThrowException_whenInvalidMagicByte() {
        assertThrows(IllegalArgumentException.class,
                () -> PacketDecoder.decodePacket(Hex.decodeHex("12")
        ));
    }

    @Test
    void shouldPass_whenValidPacketSource() throws DecoderException {
        buffer = ByteBuffer.wrap(byteArr);
        assertEquals(0, buffer.get(1), "Invalid packet source!");
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
