
public class Main {

    public static void main(String[] args) {
        PacketDecoder.decodePacket(PacketEncoder.encode((byte)0,1, 777,21, "Hello World!"));
    }

}
