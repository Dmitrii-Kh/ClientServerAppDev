package lab02;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        System.out.println("main start\n");

//        byte[] str = "hello".getBytes();
//
//        byte[] bigStr = new byte[300];
//
//        for(int i = 0; i < str.length; ++i){
//            bigStr[i] = str[i];
//        }
//
//        System.out.println(new String(bigStr));
//
//        if(true){
//            return;
//        }
        int port = 54321;

        Server server = null;
        try {
            server = new Server(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.setDaemon(true);
        server.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client client = new Client(port);
        client.setDaemon(true);

        Client client2 = new Client(port);
        client2.setDaemon(true);

        client.start();
        client2.start();


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.shutdown();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nmain end");
    }

}
