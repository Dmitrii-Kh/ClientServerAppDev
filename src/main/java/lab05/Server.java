package lab05;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import lab04.database.Database;
import lab04.entities.Category;
import lab04.entities.Product;
import lab04.entities.User;
import lab05.HTTP.Endpoint;
import lab05.Service.JwtService;
import lab05.domain.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    private final Database db = Database.getInstance();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final HttpPrincipal ANONYMOUS_USER = new HttpPrincipal("anonymous", "anonymous");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<Endpoint> endpoints = new ArrayList<>();

    private final HttpServer server;

    private ThreadPoolExecutor processPool;

    public Server(int maxProcessThreads) throws IOException {

        processPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcessThreads);

//        db.insertUser(User.builder()
//                .login("admin")
//                .password(DigestUtils.md5Hex("password"))
//                .role("admin")
//                .build());
//
//        db.insertUser(User.builder()
//                .login("user")
//                .password(DigestUtils.md5Hex("password"))
//                .role("user")
//                .build());
//
//
//        db.insertCategory(Category.builder().title("food").description("smth to eat").build());
//
//        for (int i = 0; i < 10; i++) {
//            db.insertProduct(Product.builder()
//                    .title("product" + i)
//                    .description("description")
//                    .producer("producer" + i)
//                    .price(Math.random() * 100)
//                    .quantity(i * i + 1)
//                    .category("food")
//                    .build());
//        }

//        endpoints.add(Endpoint.of("\\/login", this::loginHandler, (a, b) -> new HashMap<>()));
//        endpoints.add(Endpoint.of("^\\/api\\/product\\/(\\d+)$", this::GetOrDeleteProductByIdHandler, this::getProductParamId));
//        endpoints.add(Endpoint.of("\\/api\\/product", this::PutOrPostProductByIdHandler, (a, b) -> new HashMap<>()));


        this.server = HttpServer.create();

        server.bind(new InetSocketAddress(8080), 0);

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

        if (!exchange.getPrincipal().getRealm().equals("admin")) {
            writeResponse(exchange, 403, ErrorResponse.of("No permission"));
            return;
        }


        final String uri = exchange.getRequestURI().toString();

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
            // 404
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
                    return new Failure(403);
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

}
