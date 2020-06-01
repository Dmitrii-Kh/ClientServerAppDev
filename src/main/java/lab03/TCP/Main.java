package lab03.TCP;

import com.google.common.primitives.UnsignedLong;
import lab03.Message;
import lab03.Packet;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.ServerSocket;


public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("main start\n");

        int port = 54321;
//        ServerSocket socket = new ServerSocket(port);

        StoreServerTCP server = null;
        try {
            server = new StoreServerTCP(port, 40, 10, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        server.setDaemon(true);
        server.start();
//        server.shutdown();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Packet packet1 = null;

        Packet packet2 = null;
        try {
            packet1 =
                    new Packet((byte) 1, UnsignedLong.ONE, new Message(Message.cTypes.ADD_PRODUCT_GROUP, 1, "client1"));
            packet2 = new Packet((byte) 1, UnsignedLong.ONE, new Message(Message.cTypes.ADD_PRODUCT, 1, "client2"));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreClientTCP client1 = new StoreClientTCP(port, packet1);
//        client.setDaemon(true);

        StoreClientTCP client2 = new StoreClientTCP(port, packet2);
//        client2.setDaemon(true);

        client1.start();
        client2.start();


        try {
            Thread.sleep(3_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.shutdown();
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        System.out.println("\nmain end");
    }

}
