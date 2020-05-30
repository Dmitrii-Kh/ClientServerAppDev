package lab03;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


public class Processor /*extends Thread*/ {

//    Packet inputPacket;
//
//    public Processor(Packet inputPacket) {
//        this.inputPacket = inputPacket;
//    }


    public static Packet process(Packet inputPacket) /*throws Exception*/throws BadPaddingException, IllegalBlockSizeException {

        Message message = inputPacket.getBMsq();
        Message answerMessage = null;
        String answer;

//        System.out.println("processing");

        if (message.getCType() == Message.cTypes.ADD_PRODUCT.ordinal()) {
            answer = message.getMessage() + " ADD_PRODUCT!";
            answerMessage = new Message(Message.cTypes.ADD_PRODUCT, 0, answer);

        } else if (message.getCType() == Message.cTypes.ADD_PRODUCT_GROUP.ordinal()) {
            answer = message.getMessage() + " ADD_PRODUCT_GROUP!";
            answerMessage = new Message(Message.cTypes.ADD_PRODUCT_GROUP, 0, answer);
        } else {
            answer = message.getMessage() + " OK!";
            answerMessage = new Message(Message.cTypes.OK, 0, answer);
        }

        Packet answerPacket = new Packet(inputPacket.getbSrc(), inputPacket.getbPktId(), answerMessage);
//        System.out.println("end processing");
        return answerPacket;
    }

//    @Override
//    public void run() {
//        try {
//            process(inputPacket);
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static void main(String[] args) throws Exception {
//        //todo packet transmitted through network -->
//        Message mes = new Message(1, 1, "Josh");
//        Packet pac = new Packet((byte) 2, UnsignedLong.ONE, mes);

    //Message mes2 = new Message(2,1,"Tom");
    //Packet pac2 = new Packet((byte)2, UnsignedLong.ONE, mes2);

    //todo this block runs in ClientHandler:
    //todo incoming byte array -->

//        Packet newPac = new Packet(pac.toPacket());
    //Packet newPac2 = new Packet(pac2.toPacket());

//        Processor pr = new Processor(newPac);
    //Processor pr2 = new Processor(newPac2.getBMsq());

//        pr.join();
    //pr2.join();
//    }
}
