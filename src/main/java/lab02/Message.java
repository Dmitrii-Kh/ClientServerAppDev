package lab02;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;


public class Message {


    enum cTypes {
        GET_PRODUCT_COUNT,
        GET_PRODUCT,
        ADD_PRODUCT,
        ADD_PRODUCT_TITLE,
        SET_PRODUCT_PRICE,
        ADD_PRODUCT_TO_GROUP
    }



    Integer cType;
    Integer bUserId;
    String message;


    public void setCType(int cType) {
        this.cType = cType;
    }

    public int getCType() {
        return cType;
    }


    public void setBUserId(int bUserId) {
        this.bUserId = bUserId;
    }

    public int getBUserId() {
        return bUserId;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static final int BYTES_WITHOUT_MESSAGE = Integer.BYTES + Integer.BYTES;


    public Message() { }


    public Message(Integer cType, Integer bUserId, String message) {
        this.cType = cType;
        this.bUserId = bUserId;
        this.message = message;
    }



    public byte[] toPacketPart() {

        return ByteBuffer.allocate(getMessageBytesLength())
                .putInt(cType)
                .putInt(bUserId)
                .put(message.getBytes()).array();
    }



    public int getMessageBytesLength() {
        return BYTES_WITHOUT_MESSAGE + getMessageBytes();
    }


    public Integer getMessageBytes() {
        return message.length();
    }



    public void encode() throws BadPaddingException, IllegalBlockSizeException {

       // message = Cipher.encode(message);
        message = new String(Cryptor.encryptMessage(message.getBytes()));
    }



    public void decode() throws BadPaddingException, IllegalBlockSizeException {

        //message = Cipher.decode(message);
        message = new String(Cryptor.decryptMessage(message.getBytes()));

    }

}