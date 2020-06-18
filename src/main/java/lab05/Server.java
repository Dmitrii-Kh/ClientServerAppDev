package lab05;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import lab04.database.Database;
import lab04.entities.User;
import lab05.HTTP.Endpoint;
import lab05.Service.JwtService;
import lab05.domain.ErrorResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {


    private final Database db = Database.getInstance();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final HttpPrincipal ANONYMOUS_USER = new HttpPrincipal("anonymous", "anonymous");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<Endpoint> endpoints = new ArrayList<>();

    private final HttpServer server;

    private ThreadPoolExecutor processPool;

    public Server(int port, int maxProcessThreads) throws IOException {

        processPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcessThreads);

        this.server = HttpServer.create();

        server.bind(new InetSocketAddress(port), 0);

        server.createContext("/", this::rootHandler)
                .setAuthenticator(new MyAuthenticator());

        server.start();
    }


    public void addEndpoint(Endpoint endpoint){
        endpoints.add(endpoint);
    }


    public void stop() {
        this.server.stop(1);
    }


    private void rootHandler(final HttpExchange exchange) throws IOException {

        exchange.getResponseHeaders()
                .add("Content-Type", "application/json");

        final String uri = exchange.getRequestURI().toString();

        if (!exchange.getPrincipal().getRealm().equals("admin") && !uri.equals("/login")) {
            writeResponse(exchange, 403, ErrorResponse.of("No permission"));
            return;
        }



        final Optional<Endpoint> endpoint = endpoints.stream()
                .filter(anEndpoint -> anEndpoint.matches(exchange.getRequestMethod(), uri))
                .findFirst();


        if (endpoint.isPresent()) {
            processPool.execute(() -> {
                try {
                    endpoint.get().handler()
                            .handle(exchange);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } else {
            handlerNotFound(exchange);
        }
    }

    private void handlerNotFound(final HttpExchange exchange) {
        try {
            System.out.println("404");
            exchange.sendResponseHeaders(404, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private class MyAuthenticator extends Authenticator {

        @Override
        public Result authenticate(final HttpExchange httpExchange) {
            final String token = httpExchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER);


            if (token != null) {
                try {
                    final String username = JwtService.getUsernameFromToken(token);
                    final User user = db.getUser(username);

                    if (user != null) {
                        return new Success(new HttpPrincipal(username, user.getRole()));
                    } else {
                        return new Retry(401);
                    }

                } catch (Exception e) {
                    return new Failure(401);
                }
            }

            return new Success(ANONYMOUS_USER);
        }
    }

    public static void writeResponse(final HttpExchange exchange, final int statusCode, final Object response) throws
            IOException {
        final byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(response);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public Database db() {
        return db;
    }

}
