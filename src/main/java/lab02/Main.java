package lab02;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        System.out.println("main start\n");


        Server server = new Server(54321);

        server.setDaemon(true);

        server.start();



        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.shutdown();

        System.out.println("\nmain end");
    }

}
