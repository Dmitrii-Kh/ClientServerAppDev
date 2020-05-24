package lab02;

import com.google.common.primitives.UnsignedLong;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


public class Processor extends Thread {

    Message message;

    public Processor(Message message) {
        this.message = message;
        this.start();
    }


    public void process(Message message) throws Exception {
        Message answerMessage = null;
        String answer;

        if (message.getCType() == 1) {
            answer = message.getMessage() + " added successfully!";
            answerMessage = new Message(0, 0, answer);

        } else if (message.getCType() == 2) {
            answer = message.getMessage() + " deleted successfully!";
            answerMessage = new Message(0, 0, answer);
        }

        Packet answerPacket = new Packet((byte) 2, UnsignedLong.ONE, answerMessage);
        Sender.sendMessage(answerPacket.toPacket());
    }

    @Override
    public void run() {
        try {
            process(message);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //todo packet transmitted through network -->
        Message mes = new Message(1, 1, "Josh");
        Packet pac = new Packet((byte) 2, UnsignedLong.ONE, mes);

        //Message mes2 = new Message(2,1,"Tom");
        //Packet pac2 = new Packet((byte)2, UnsignedLong.ONE, mes2);

        //todo this block runs in ClientHandler:
        //todo incoming byte array -->

        Packet newPac = new Packet(pac.toPacket());
        //Packet newPac2 = new Packet(pac2.toPacket());

        Processor pr = new Processor(newPac.getBMsq());
        //Processor pr2 = new Processor(newPac2.getBMsq());

        pr.join();
        //pr2.join();
    }
}
