package Final;

import Final.entities.Category;
import Final.entities.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import Final.database.Database;
import Final.entities.User;
import Final.HTTP.Endpoint;
import Final.Service.JwtService;
import Final.domain.ErrorResponse;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerAPI {


    private final Database db = Database.getInstance();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final HttpPrincipal ANONYMOUS_USER = new HttpPrincipal("anonymous", "anonymous");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<Endpoint> endpoints = new ArrayList<>();

    private final HttpServer server;

    private ThreadPoolExecutor processPool;

    public ServerAPI(int port, int maxProcessThreads) throws IOException {

        processPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcessThreads);
        this.server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 30);
        server.createContext("/", this::rootHandler)
                .setAuthenticator(new MyAuthenticator());

//        db.insertUser(User.builder()
//                .login("admin")
//                .password(DigestUtils.md5Hex("password"))
//                .role("admin")
//                .build());
//        db.insertCategory(Category.builder().title("food").description("smth to eat").build());
//
//        for (int i = 0; i < 7; i++) {
//            db.insertProduct(Product.builder()
//                    .title("product" + i)
//                    .description("description")
//                    .producer("producer" + i)
//                    .price(Math.random() * 100)
//                    .quantity(i * i + 1)
//                    .category("food")
//                    .build());
//        }
        server.start();
    }


    public void addEndpoint(Endpoint endpoint){
        endpoints.add(endpoint);
    }

    public void stop() {
        this.server.stop(1);
    }


    private void rootHandler(final HttpExchange exchange) throws IOException {

//        exchange.getResponseHeaders()
//                .add("Content-Type", "application/json");

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
            //System.out.println("404");
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

        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-auth");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        headers.add("Access-Control-Max-Age", "-1");
        headers.add("Content-Type", "application/json");
        headers.add("Connection", "close");

        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public Database db() {
        return db;
    }

}
