package Final;

import Final.HTTP.Endpoint;


import java.io.IOException;

public class MainFinal {
    public static void main(String[] args) throws IOException {
        ServerAPI server = new ServerAPI(4321,10);
        Endpoints endpoints = new Endpoints();

        for(Endpoint endpoint: endpoints.getAllEndpoints()){
            server.addEndpoint(endpoint);
        }
    }

}
