package lab03;

import com.google.common.primitives.UnsignedLong;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.*;

public class StoreClientUDP extends Thread {
    DatagramSocket ds = null;
    DatagramPacket dp = null;
    InetAddress ip = InetAddress.getLocalHost();
    private int port;
    private Packet packet;
    private byte[] packetBytes;

    public StoreClientUDP(int port, Packet packet) throws UnknownHostException {
        this.port = port;
        this.packet = packet;

        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.start();
    }

    @Override
    public void run() {

        packetBytes = packet.toPacket();
        dp = new DatagramPacket(packetBytes, packetBytes.length, ip, port);

        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        packet.setbPktId(packet.getbPktId().plus(UnsignedLong.ONE));
        packetBytes = packet.toPacket();
        dp = new DatagramPacket(packetBytes, packetBytes.length, ip, port);

        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (true) {

            byte[] buff = new byte[1024];
            DatagramPacket incomingDatagramPacket = new DatagramPacket(buff, buff.length);
            try {
                ds.receive(incomingDatagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Packet answerPacket = null;
            try {
                answerPacket = new Packet(incomingDatagramPacket.getData());
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            System.out.println("Message from server : " + answerPacket.getBMsq().getMessage() + " ; Packet id : " + answerPacket.getbPktId());
        }

    }
}
