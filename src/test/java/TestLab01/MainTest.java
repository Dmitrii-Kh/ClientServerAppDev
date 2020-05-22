package TestLab01;

import lab01.Cryptor;
import lab01.PacketDecoder;
import lab01.PacketEncoder;
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

    private static byte[] packet;
    private static String message;
    private static ByteBuffer buffer;

    @BeforeAll
    static void init() {
        message = "Hello World!";
        packet = PacketEncoder.encode((byte) 0, 1, 777, 21, message);
        buffer = ByteBuffer.wrap(packet);
    }

    @Test
        //check for validity of CRC1 & CRC2
    void shouldPass_whenValidInputMessage() {
        PacketDecoder.decodePacket(packet);
    }

    @Test
    void shouldThrowException_whenInvalidMagicByte() {
        assertThrows(IllegalArgumentException.class, () -> PacketDecoder.decodePacket(Hex.decodeHex("12")));
    }

    @Test
    void shouldPass_whenValidPacketSource() {
        assertEquals(0, buffer.get(1), "Invalid packet source!");
    }

    @Test
    void shouldPass_whenValidPacketId() {
        assertEquals(1, buffer.getLong(2), "Invalid packet id!");
    }

    @Test
    void shouldPass_whenValidCommandType() {
        assertEquals(777, buffer.getInt(16), "Invalid command type!");
    }

    @Test
    void shouldPass_whenValidUserId() {
        assertEquals(21, buffer.getInt(20), "Invalid user id!");
    }

    @Test
    void shouldPass_ifCryptorWorksNice() throws UnsupportedEncodingException {
        String message = "message";

        try {
            assertEquals(message, new String(Cryptor.decryptMessage(Cryptor.encryptMessage(message.getBytes("UTF-8")))),
                    "lab01.Cryptor doesn't work");
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
}
