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
        endpoints.add(Endpoint.of("^\\/api\\/product\\/(\\d+)$", this::GetOrDeleteProductByIdHandler, this::getProductParamId));
        endpoints.add(Endpoint.of("\\/api\\/product", this::PutOrPostProductByIdHandler, (a, b) -> new HashMap<>()));


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

        final Optional<Endpoint> endpoint = endpoints.stream()  //todo multithreading
                .filter(anEndpoint -> anEndpoint.matches(uri))
                .findFirst();


        if (endpoint.isPresent()) {
            endpoint.get().handler()
                    .handle(exchange);
        } else {
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


    private void GetOrDeleteProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            exchange.getResponseHeaders()
                .add("Content-Type", "application/json");

            if (!exchange.getPrincipal().getRealm().equals("admin")) {
                writeResponse(exchange, 403, ErrorResponse.of("No permission"));
                return;
            }

            if (exchange.getRequestMethod().equals("GET")) {
                getProductByIdHandler(exchange, pathParams);
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                deleteProductByIdHandler(exchange, pathParams);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void PutOrPostProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            exchange.getResponseHeaders()
                    .add("Content-Type", "application/json");

            if (!exchange.getPrincipal().getRealm().equals("admin")) {
                writeResponse(exchange, 403, ErrorResponse.of("No permission"));
                return;
            }

            if (exchange.getRequestMethod().equals("PUT")) {
                addProductHandler(exchange, pathParams);
            } else if (exchange.getRequestMethod().equals("POST")) {
                modifyProductHandler(exchange, pathParams);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
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


    private void deleteProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            final int productId = Integer.parseInt(pathParams.get("productId"));
            Product productToDelete = db.getProduct(productId);

            if (productToDelete != null) {
                db.deleteProduct(productToDelete.getTitle());
                if (db.getProduct(productId) == null)                               //assert that product is deleted
                    exchange.sendResponseHeaders(204, -1);
            } else {
                writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void modifyProductHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try (final InputStream requestBody = exchange.getRequestBody()) {

            final UpdateProductCredentials updateProductCredentials = OBJECT_MAPPER.readValue(requestBody, UpdateProductCredentials.class);

            if (db.getProduct(updateProductCredentials.getId()) != null) {
                String id = String.valueOf(updateProductCredentials.getId());

                if (updateProductCredentials.getTitle() != null)
                    db.updateProduct("title", updateProductCredentials.getTitle(), "id", id);

                if (updateProductCredentials.getDescription() != null)
                    db.updateProduct("description", updateProductCredentials.getDescription(), "id", id);

                if (updateProductCredentials.getProducer() != null)
                    db.updateProduct("producer", updateProductCredentials.getProducer(), "id", id);

                if (updateProductCredentials.getPrice() != null)
                    db.updateProduct("price", String.valueOf(updateProductCredentials.getPrice()), "id", id);

                if (updateProductCredentials.getQuantity() != null)
                    db.updateProduct("quantity", String.valueOf(updateProductCredentials.getQuantity()), "id", id);

                if (updateProductCredentials.getCategory() != null)
                    db.updateProduct("category", updateProductCredentials.getCategory(), "id", id);

                //todo handle 409
                exchange.sendResponseHeaders(204, -1);

            } else {
                writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addProductHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try (final InputStream requestBody = exchange.getRequestBody()) {

            final ProductCredentials productCredentials = OBJECT_MAPPER.readValue(requestBody, ProductCredentials.class);
            final Product product = Product.builder()
                    .title(productCredentials.getTitle())
                    .description(productCredentials.getDescription())
                    .producer(productCredentials.getProducer())
                    .price(productCredentials.getPrice())
                    .quantity(productCredentials.getQuantity())
                    .category(productCredentials.getCategory())
                    .build();

            final int productId = db.insertProduct(product);

            if (productId != -1) {
                writeResponse(exchange, 201, "Created! id : " + productId);
            } else {
                writeResponse(exchange, 409, ErrorResponse.of("Conflict!"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private Map<String, String> getProductParamId(final String uri, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(uri);
        matcher.find();

        return new HashMap<String, String>() {{
            put("productId", matcher.group(1));
        }};
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
