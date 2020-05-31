package lab03.TCP;

import lab03.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class StoreClientTCP extends Thread {

    private Network network;


    private Packet packet;



    public StoreClientTCP(int port, Packet packet) {
        this.packet = packet;
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);
            network = new Network(socket, 4000);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {//todo reconnect
        Thread.currentThread().setName(Thread.currentThread().getId() + " - Client");

        try {
            network.send(packet.toPacket());

            System.out.println(Thread.currentThread().getName() + " - client starts");

            try {
                byte[] packetBytes = network.receive();
                if (packetBytes == null) {
                    System.out.println("server timeout");
                    return;
                }
                Packet packet = new Packet(packetBytes);
                System.out.println(
                        Thread.currentThread().getName() + " - answer from server: " + packet.getBMsq().getMessage());
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            network.shutdown();
        }
    }

    public void shutdown() {
        //todo
    }


}
