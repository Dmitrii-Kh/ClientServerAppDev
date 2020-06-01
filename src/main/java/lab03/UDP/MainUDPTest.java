package lab03.UDP;

import com.google.common.primitives.UnsignedLong;
import lab03.Message;
import lab03.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;


public class MainUDPTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("main start\n");

        int port = 5432;

        Packet pac0 = null;
        Packet pac1 = null;
        Packet pac2 = null;

        try {
            pac0 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT_GROUP, 0, "test client 0"));
            pac1 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT, 1, "test client 1"));
            pac2 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.GET_PRODUCT_AMOUNT, 2, "test client 2"));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreServerUDP ss = new StoreServerUDP(port);

        StoreClientUDP sc0 = new StoreClientUDP(port, pac0);
        StoreClientUDP sc1 = new StoreClientUDP(port, pac1);
        StoreClientUDP sc2 = new StoreClientUDP(port, pac2);


        ss.join();


        System.out.println("\nmain end");
    }

}
