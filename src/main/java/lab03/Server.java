package lab03;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {

    public ServerSocket server;

    //    private ThreadPoolExecutor executor  = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
    private int             port;

    public Server(int port) throws IOException {
        super("Server");
        this.port = port;
        server = new ServerSocket(port);
    }


    @Override
    public void run() {

        try {
            System.out.println("Server running on port: " + port);

            while (true) {
                executor.execute(new ClientHandler(server.accept(), 2, TimeUnit.SECONDS));
            }

        } catch (SocketException e) {
            System.out.println("ok, I will close server");

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            executor.shutdown();
            System.out.println("Server has closed");
        }

    }

    public void shutdown() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
