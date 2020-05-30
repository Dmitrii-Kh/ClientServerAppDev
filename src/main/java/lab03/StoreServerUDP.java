package lab03;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class StoreServerUDP {
    private DatagramSocket datagramSocket = null;
    private DatagramPacket datagramPacket = null;
    private byte[] buffer = null;
    //private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    StoreServerUDP(int listenPort) {
        try {
            datagramSocket = new DatagramSocket(listenPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        buffer = new byte[1024];
        datagramPacket = new DatagramPacket(buffer, buffer.length);

//        try {
//            run();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void run() throws IOException {
        System.out.println("Server running on port: " + datagramSocket.getPort());

        while (true) /* <-- todo change it ?*/ {

            //datagramSocket.receive(datagramPacket); <-- todo inside ClientHandler run() ?
            //todo --> handle DatagramPacket, e.g. executor.execute(new ClientHandler(datagramSocket, datagramPacket));
            buffer = new byte[1024]; //clear the buffer after every message

        }
    }

}
