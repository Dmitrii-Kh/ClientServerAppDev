package TestLab03;

import com.google.common.primitives.UnsignedLong;
import lab03.Message;
import lab03.Packet;
import lab03.UDP.StoreClientUDP;
import lab03.UDP.StoreServerUDP;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {


    @Test
    void shouldPass_whenAllPacketsProcessedCorrectly() throws UnknownHostException, InterruptedException {
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


    @Test
    void shouldPass_whenBelatedPacketIsProcessedCorrectly() throws InterruptedException, UnknownHostException {

        System.out.println("main start\n");

        int port = 5433;

        Packet pac0 = null;
        Packet pac1 = null;

        try {
            pac0 = new Packet((byte) 1, UnsignedLong.ONE.plus(UnsignedLong.ONE),
                    new Message(Message.cTypes.ADD_PRODUCT_GROUP, 1, "test packet 2"));
            pac1 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT, 1, "test packet 1"));

        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreServerUDP ss = new StoreServerUDP(port);

        StoreClientUDP sc0 = new StoreClientUDP(port, pac0);
        Thread.sleep(100);  //in final project client will be waiting for answer from server,
        //and only after receiving answer packet client will send next packet

        StoreClientUDP sc1 = new StoreClientUDP(port, pac1);

        assertEquals("This packet has been processed yet!", sc1.getAnswerPacket().getBMsq().getMessage());

        ss.join();


        System.out.println("\nmain end");
    }


    @Test
    void shouldPass_whenLastPacketIdTrackerResetToZero() throws InterruptedException, UnknownHostException {
        //Last packet id tracker resets every 5 seconds in order not to store clients
        //that haven`t sent anything to server for this period.

        System.out.println("main start\n");

        int port = 5434;

        Packet pac0 = null;
        Packet pac1 = null;

        try {
            pac0 = new Packet((byte) 1, UnsignedLong.ONE.plus(UnsignedLong.ONE),
                    new Message(Message.cTypes.ADD_PRODUCT_GROUP, 1, "test packet 2"));
            pac1 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT, 1, "test packet 1"));

        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreServerUDP ss = new StoreServerUDP(port);

        StoreClientUDP sc0 = new StoreClientUDP(port, pac0);
        Thread.sleep(6000);                              //wait for ClientMapCleaner
        StoreClientUDP sc1 = new StoreClientUDP(port, pac1);

        assertEquals("test packet 1 ADD_PRODUCT!", sc1.getAnswerPacket().getBMsq().getMessage());

        ss.join();


        System.out.println("\nmain end");
    }


    @Test
    void shouldPass_whenAlreadyProcessedPacketIsNotProcessedAgain() throws InterruptedException, UnknownHostException {

        //this situation may happen if server correctly processed client`s packet,
        //but client did not receive confirmation packet from server and consequently resent this packet

        System.out.println("main start\n");

        int port = 5435;

        Packet pac0 = null;

        try {
            pac0 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT_GROUP, 1, "test packet 1"));

        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreServerUDP ss = new StoreServerUDP(port);

        StoreClientUDP sc0 = new StoreClientUDP(port, pac0);
        Thread.sleep(100);                 //in final project client will be waiting for answer from server,
                                                //and only after receiving answer packet client will send next packet
        StoreClientUDP sc1 = new StoreClientUDP(port, pac0);

        assertEquals("This packet has been processed yet!", sc1.getAnswerPacket().getBMsq().getMessage());

        ss.join();


        System.out.println("\nmain end");
    }

    @Test
    void shouldPass_whenClientResentPacketAndReceivedConfirmation() throws UnknownHostException, InterruptedException {
        System.out.println("main start\n");

        int port = 5436;

        Packet pac0 = null;


        try {
            pac0 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT, 0, "test"));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        StoreClientUDP sc0 = new StoreClientUDP(port, pac0);

        Thread.sleep(4000);
        //start server after sending a packet
        StoreServerUDP ss = new StoreServerUDP(port);

        assertEquals("test ADD_PRODUCT!", sc0.getAnswerPacket().getBMsq().getMessage());

        ss.join();


        System.out.println("\nmain end");
    }


}
