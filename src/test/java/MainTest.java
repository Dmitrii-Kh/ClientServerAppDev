import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {

    @Test    //check for validity of CRC1 & CRC2
    void shouldPass_whenValidInputMessage() throws DecoderException{
        Main.initCipher();  //todo make private?
        Main.initKey();
        Main.decodePacket(Main.encode());
    }

    @Test
    void shouldThrowException_whenInvalidMagicByte() {
        assertThrows(IllegalArgumentException.class,
                () -> Main.decodePacket(Hex.decodeHex("12")
        ));
    }


    private boolean areArraysEqual(final byte[] arr1, final byte[] arr2){
      boolean res = true;
        for (int i = 0; i < arr1.length; ++i) {
            res &= arr1[i] == arr2[i];
        }
        return res;
    }

    @Test
    void shouldPass_whenEncryptedDecryptedCorrectly(){
        Main.initCipher();  //todo make private?
        Main.initKey();

        final String usefulData   = "Useful data";
        final byte[] messageBytes = usefulData.getBytes(StandardCharsets.UTF_8);
        final byte[] messageEncryptedDecrypted = Main.decryptMessage(Main.encryptMessage(messageBytes));
        assertTrue(areArraysEqual(messageBytes, messageEncryptedDecrypted));
    }
}
