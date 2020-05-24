package lab02;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client extends Thread {

    Socket       socket;
    InputStream  input;
    OutputStream output;

    private int port;

    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(Thread.currentThread().getId() + " - Client");

        try {
            try {
                socket = new Socket("localhost", port);
                input = socket.getInputStream();
                output = socket.getOutputStream();
                System.out.println(Thread.currentThread().getName() +  " - client starts");

//                output.write(Thread.currentThread().getName().getBytes());
                output.write((Thread.currentThread().getId() + "").getBytes());

//                output.flush();
//                System.out.println("client flushed");

                byte[] bytes = new byte[400];
                input.read(bytes);


                System.out.println(Thread.currentThread().getName() +  " - answer from server: " + new String(bytes));

                input.close();
                output.close();

            } finally {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void shutdown(){
        try{

            try {
                input.close();
                output.close();
            }finally {
                socket.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
