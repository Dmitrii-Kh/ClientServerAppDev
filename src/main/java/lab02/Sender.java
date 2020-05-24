package lab02;

public class Sender {
    private static Packet packet;

    static void sendMessage(byte[] packetInBytes) throws Exception {
        packet = new Packet(packetInBytes);
        System.out.println(packet.getBMsq().getMessage());
    }

}
