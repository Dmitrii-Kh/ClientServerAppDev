package lab03;

import com.google.common.primitives.UnsignedLong;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;


public class MainUDPTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("main start\n");

        int port = 5432;

        Packet pac = null;
        Packet pac1 = null;
        Packet pac2 = null;
        try {
            pac = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT_GROUP, 0, "test"));
            pac1 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT, 1, "test1"));
            pac2 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.GET_PRODUCT_AMOUNT, 2, "test2"));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreServerUDP ss = new StoreServerUDP(port);

        StoreClientUDP sc = new StoreClientUDP(port, pac);
        StoreClientUDP sc1 = new StoreClientUDP(port, pac1);
        StoreClientUDP sc2 = new StoreClientUDP(port, pac2);


        ss.join();

        // Thread.sleep(2000);

        System.out.println("\nmain end");
    }

}
