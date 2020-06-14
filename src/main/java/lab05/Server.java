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
import lab05.domain.ErrorResponse;
import lab05.domain.LoginResponse;
import lab05.domain.UserCredentials;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final HttpPrincipal ANONYMOUS_USER = new HttpPrincipal("anonymous", "anonymous");


    private final Database db = Database.getInstance();

    private final List<Endpoint> endpoints;

    private final HttpServer server;

    public Server() throws IOException {
        db.insertUser(User.builder()
                .login("admin")
                .password(DigestUtils.md5Hex("password"))
                .role("admin")
                .build());

        db.insertUser(User.builder()
                .login("user")
                .password(DigestUtils.md5Hex("password"))
                .role("user")
                .build());


        db.insertCategory(Category.builder().title("food").description("smth to eat").build());

        for (int i = 0; i < 10; i++) {
            db.insertProduct(Product.builder()
                    .title("product" + i)
                    .description("description")
                    .producer("producer" + i)
                    .price(Math.random() * 100)
                    .quantity(i * i + 1)
                    .category("food")
                    .build());
        }

        this.endpoints = new ArrayList<Endpoint>();
        endpoints.add(Endpoint.of("\\/login", this::loginHandler, (a, b) -> new HashMap<>()));
        endpoints.add(Endpoint.of("^\\/api\\/product\\/(\\d+)$", this::getProductByIdHandler, this::getProductParamId));


        this.server = HttpServer.create();

        server.bind(new InetSocketAddress(8080), 0);

        server.createContext("/", this::rootHandler)
        .setAuthenticator(new MyAuthenticator());

        server.start();
    }

    public void stop() {
        this.server.stop(1);
    }


    private void rootHandler(final HttpExchange exchange) throws IOException {

        final String uri = exchange.getRequestURI().toString();

        final Optional<Endpoint> enpoint = endpoints.stream()
                .filter(endpoint -> endpoint.matches(uri))
                .findFirst();


        if (enpoint.isPresent()) {
            enpoint.get().handler()
                    .handle(exchange);
        } else {
            // default handler
            // 404
            handlerNotFound(exchange);
        }
    }

    private void handlerNotFound(final HttpExchange exchange) {
        try {
            exchange.sendResponseHeaders(404, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try (final InputStream inputStream = exchange.getRequestBody(); final OutputStream os = exchange.getResponseBody()) {
            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");


            if (!exchange.getPrincipal().getRealm().equals("admin")) {
                writeResponse(exchange, 403, ErrorResponse.of("No permission"));
                return;
            }

            final int productId = Integer.parseInt(pathParams.get("productId"));
            final Product product = db.getProduct(productId);


            if (product != null) {
                writeResponse(exchange, 200, product);
            } else {
                writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getProductParamId(final String uri, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(uri);
        matcher.find();

        return new HashMap<String, String>() {{
            put("productId", matcher.group(1));
        }};
    }


    private void loginHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try (final InputStream requestBody = exchange.getRequestBody()) {
            final UserCredentials userCredential = OBJECT_MAPPER.readValue(requestBody, UserCredentials.class);
            final User user = db.getUser(userCredential.getLogin());

            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");

            if (user != null) {
                if (user.getPassword().equals(DigestUtils.md5Hex(userCredential.getPassword()))) {
                    final LoginResponse loginResponse = LoginResponse.of(JwtService.generateToken(user), user.getLogin(), user.getRole());
                    writeResponse(exchange, 200, loginResponse);
                } else {
                    writeResponse(exchange, 401, ErrorResponse.of("invalid password"));
                }
            } else {
                writeResponse(exchange, 401, ErrorResponse.of("unknown user"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void writeResponse(final HttpExchange exchange, final int statusCode, final Object response) throws IOException {
        final byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(response);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
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


}
