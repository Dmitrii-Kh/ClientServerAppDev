package lab03;

import java.net.*;

public class StoreClientUDP {
    DatagramSocket ds = null;
    DatagramPacket dp = null;
    byte[] buff = null;
    InetAddress ip = InetAddress.getLocalHost();
    private int port;
    private Packet packet;

    public StoreClientUDP(int port, Packet packet) throws UnknownHostException {
        this.port = port;
        this.packet = packet;

        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        dp = new DatagramPacket(buff, buff.length, ip, port);
    }

    public void run() {

        //todo ? Network network = new Network(ds, dp,5, TimeUnit.SECONDS); ?
        //network.send(packet.toPacket());

//        try {
//            byte[] packetBytes = network.receive();
//            Packet packet = new Packet(packetBytes);
//            System.out.println(Thread.currentThread().getName() +  " - answer from server: " + packet.getBMsq().getMessage());
//        } catch (TimeoutException e) {
//            System.out.println("server timeout");
//        }

    }

}
