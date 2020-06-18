package lab05;

import lab05.HTTP.Endpoint;

import java.io.IOException;

public class MainLab05 {
    public static void main(String[] args) throws IOException {
        Server server = new Server(3);
        Endpoints endpoints = new Endpoints();

        for(Endpoint endpoint: endpoints.getAllEndpoints()){
            server.addEndpoint(endpoint);
        }

    }
}
