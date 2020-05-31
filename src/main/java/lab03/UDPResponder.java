package lab03;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class UDPResponder implements Runnable {
    private Packet answerPac = null;
    private DatagramSocket ds;
    private DatagramPacket dp;

    private Object lock = new Object();

    UDPResponder(DatagramPacket dp) {
        this.dp = dp;
    }

    @Override
    public void run() {
        // System.out.println("In run");

        synchronized (lock) {

            Packet pacToBeProcessed = null;
            try {
                // System.out.println("In get");
                pacToBeProcessed = new Packet(dp.getData());
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            try {
                //  System.out.println("In process");
                answerPac = Processor.process(pacToBeProcessed);
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            try {
                ds = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }

            try {
                ds.send(new DatagramPacket(answerPac.toPacket(), answerPac.toPacket().length, dp.getAddress(), dp.getPort()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
