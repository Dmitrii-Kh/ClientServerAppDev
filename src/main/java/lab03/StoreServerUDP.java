package lab03;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class StoreServerUDP extends Thread {
    private DatagramSocket datagramSocket = null;
    private int listenPort;
    Boolean isRunning = true;

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);


    StoreServerUDP(int listenPort) {
        this.listenPort = listenPort;
        try {
            datagramSocket = new DatagramSocket(listenPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.start();
    }

    @Override
    public void run() {
        System.out.println("Server running on port: " + listenPort);

        while (isRunning) {

            byte[] buffer = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            executor.execute(new UDPResponder(datagramPacket));

            //todo executor.shutdown();
        }

    }

}
