package lab05;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import lab04.database.Database;
import lab04.entities.Product;
import lab04.entities.User;
import lab05.HTTP.Endpoint;
import lab05.Service.JwtService;
import lab05.domain.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Endpoints {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Database db = Database.getInstance();

    private final ArrayList<Endpoint> endpoints = new ArrayList<>();


    public Endpoints() {
        endpoints.add(Endpoint.of("POST", "\\/login", this::loginHandler, (a, b) -> new HashMap<>()));

        endpoints.add(Endpoint.of("GET", "^\\/api\\/product\\/(\\d+)$", this::GetProductByIdHandler, this::getProductParamId));
        endpoints.add(Endpoint.of("DELETE", "^\\/api\\/product\\/(\\d+)$", this::DeleteProductByIdHandler, this::getProductParamId));

        endpoints.add(Endpoint.of("POST", "\\/api\\/product", this::PostProductByIdHandler, (a, b) -> new HashMap<>()));
        endpoints.add(Endpoint.of("PUT", "\\/api\\/product", this::PutProductByIdHandler, (a, b) -> new HashMap<>()));
    }


    public ArrayList<Endpoint> getAllEndpoints() {
        return endpoints;
    }


    private void GetProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {

            getProductByIdHandler(exchange, pathParams);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DeleteProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            deleteProductByIdHandler(exchange, pathParams);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            final int productId = Integer.parseInt(pathParams.get("productId"));
            final Product product = db.getProduct(productId);

            if (product != null) {
                Server.writeResponse(exchange, 200, product);
            } else {
                Server.writeResponse(exchange, 404, ErrorResponse.of("No such product"));
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
                Server.writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void PutProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try {

            addProductHandler(exchange, pathParams);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void PostProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try {

            modifyProductHandler(exchange, pathParams);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void modifyProductHandler(final HttpExchange exchange, final Map<String, String> pathParams) throws IOException {

        try (final InputStream requestBody = exchange.getRequestBody()) {

            final UpdateProductCredentials
                    updateProductCredentials = OBJECT_MAPPER.readValue(requestBody, UpdateProductCredentials.class);

            if (db.getProduct(updateProductCredentials.getId()) != null) {
                String id = String.valueOf(updateProductCredentials.getId());

                if (updateProductCredentials.getTitle() != null)
                    db.updateProduct("title", updateProductCredentials.getTitle(), "id", id);

                if (updateProductCredentials.getDescription() != null)
                    db.updateProduct("description", updateProductCredentials.getDescription(), "id", id);

                if (updateProductCredentials.getProducer() != null)
                    db.updateProduct("producer", updateProductCredentials.getProducer(), "id", id);

                if (updateProductCredentials.getPrice() != null) {
                    if (updateProductCredentials.getPrice() > 0) {
                        db.updateProduct("price", String.valueOf(updateProductCredentials.getPrice()), "id", id);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                if (updateProductCredentials.getQuantity() != null) {
                    if(updateProductCredentials.getQuantity() > 0) {
                        db.updateProduct("quantity", String.valueOf(updateProductCredentials.getQuantity()), "id", id);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                if (updateProductCredentials.getCategory() != null)
                    db.updateProduct("category", updateProductCredentials.getCategory(), "id", id);

                exchange.sendResponseHeaders(204, -1);

            } else {
                Server.writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }

        } catch (IllegalArgumentException e){
            Server.writeResponse(exchange, 409, ErrorResponse.of("Conflict"));
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
                Server.writeResponse(exchange, 201, "Created " + productId);
            } else {
                Server.writeResponse(exchange, 409, ErrorResponse.of("Conflict"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loginHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try (final InputStream requestBody = exchange.getRequestBody()) {
            final UserCredentials userCredential = OBJECT_MAPPER.readValue(requestBody, UserCredentials.class);
            final User user = db.getUser(userCredential.getLogin());

            if (user != null) {
                if (user.getPassword().equals(DigestUtils.md5Hex(userCredential.getPassword()))) {
                    final LoginResponse
                            loginResponse = LoginResponse.of(JwtService.generateToken(user), user.getLogin(), user.getRole());
                    Server.writeResponse(exchange, 200, loginResponse);
                } else {
                    Server.writeResponse(exchange, 401, ErrorResponse.of("invalid password"));
                }
            } else {
                Server.writeResponse(exchange, 401, ErrorResponse.of("unknown user"));
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


}
