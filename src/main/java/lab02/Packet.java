package lab02;
import java.nio.ByteBuffer;
import com.google.common.primitives.UnsignedLong;

public class Packet {

    public final static Byte bMagic = 0x13;

    UnsignedLong bPktId;

    Byte bSrc;

    Integer wLen;

    Short wCrc16_1;

    Message bMsq;

    Short wCrc16_2;


    public Message getBMsq() {
        return bMsq;
    }

    public void setBMsq(Message bMsq) {
        this.bMsq = bMsq;
    }


    public final static Integer packetPartFirstLengthWithoutwLen = bMagic.BYTES + Byte.BYTES + Long.BYTES;

    public final static Integer packetPartFirstLength = packetPartFirstLengthWithoutwLen + Integer.BYTES;

    public final static Integer packetPartFirstLengthWithCRC16 = packetPartFirstLength + Short.BYTES;



    public Packet(Byte bSrc, UnsignedLong bPktId, Message bMsq) {

        this.bSrc = bSrc;

        this.bPktId = bPktId;



        this.bMsq = bMsq;

        wLen = bMsq.getMessageBytesLength();

    }



    public Packet(byte[] encodedPacket) throws Exception {

        ByteBuffer buffer = ByteBuffer.wrap(encodedPacket);



        Byte expectedBMagic = buffer.get();

        if (!expectedBMagic.equals(bMagic))

            throw new Exception("Unexpected bMagic");



        bSrc = buffer.get();

        bPktId = UnsignedLong.fromLongBits(buffer.getLong());

        wLen = buffer.getInt();



        wCrc16_1 = buffer.getShort();



        bMsq = new Message();

        bMsq.setCType(buffer.getInt());

        bMsq.setBUserId(buffer.getInt());

        byte[] messageBody = new byte[wLen];

        buffer.get(messageBody);

        bMsq.setMessage(new String(messageBody));

        bMsq.decode();



        wCrc16_2 = buffer.getShort();

    }



    public byte[] toPacket() {

        Message message = getBMsq();



        message.encode();





        byte[] packetPartFirst = ByteBuffer.allocate(packetPartFirstLength)

                .put(bMagic)

                .put(bSrc)

                .putLong(bPktId.longValue())

                .putInt(wLen)

                .array();

        wCrc16_1 = calculateCRC16(packetPartFirst);



        Integer packetPartSecondLength = message.getMessageBytesLength();

        byte[] packetPartSecond = ByteBuffer.allocate(packetPartSecondLength)

                .put(message.toPacketPart())

                .array();



        wCrc16_2 = calculateCRC16(packetPartSecond);



        Integer packetLength = packetPartFirstLength + wCrc16_1.BYTES + packetPartSecondLength + wCrc16_2.BYTES;



        return ByteBuffer.allocate(packetLength).put(packetPartFirst).putShort(wCrc16_1).put(packetPartSecond).putShort(wCrc16_2).array();

    }



    public static Short calculateCRC16(byte[] packetPartFirst) {

       return (short) 2; //CRC.calculateCRC(CRC.Parameters.CRC16, packetPartFirst); //todo

    }


}
