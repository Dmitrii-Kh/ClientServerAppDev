package Final;

import Final.entities.Category;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import Final.database.Database;
import Final.entities.Product;
import Final.entities.User;
import Final.HTTP.Endpoint;
import Final.Service.JwtService;
import Final.domain.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Endpoints {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Database db = Database.getInstance();

    private final ArrayList<Endpoint> endpoints = new ArrayList<>();


    public Endpoints() {
        endpoints.add(Endpoint.of("POST", "\\/login", this::loginHandler, (a, b) -> new HashMap<>()));

        endpoints.add(Endpoint
                .of("GET", "^\\/api\\/product\\/(\\d+)$", this::GetProductByIdHandler, this::getProductParamId));
        endpoints.add(Endpoint
                .of("DELETE", "^\\/api\\/product\\/(\\d+)$", this::DeleteProductByIdHandler, this::getProductParamId));
        endpoints.add(Endpoint.of("POST", "\\/api\\/product", this::PostProductByIdHandler, (a, b) -> new HashMap<>()));
        endpoints.add(Endpoint.of("PUT", "\\/api\\/product", this::PutProductHandler, (a, b) -> new HashMap<>()));

        endpoints.add(Endpoint
                .of("GET", "\\/api\\/category", this::GetCategoryByTitleHandler, (a, b) -> new HashMap<>()));
        endpoints.add(Endpoint
                .of("DELETE", "\\/api\\/category", this::DeleteCategoryHandler, (a, b) -> new HashMap<>()));
        endpoints.add(Endpoint.of("POST", "\\/api\\/category", this::PostCategoryHandler, (a, b) -> new HashMap<>()));
        endpoints.add(Endpoint.of("PUT", "\\/api\\/category", this::PutCategoryHandler, (a, b) -> new HashMap<>()));

        endpoints.add(Endpoint
                .of("GET", "\\/api\\/product/search", this::SearchForProductHandler, (a, b) -> new HashMap<>()));

        endpoints.add(Endpoint.of("GET", "\\/api\\/category/all", this::GetAllCategoriesHandler, (a, b) -> new HashMap<>()));

    }


    public ArrayList<Endpoint> getAllEndpoints() {
        return endpoints;
    }


    private void GetProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            final int productId = Integer.parseInt(pathParams.get("productId"));
            final Product product = db.getProduct(productId);

            if (product != null) {
                ServerAPI.writeResponse(exchange, 200, product);
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void DeleteProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try {
            final int productId = Integer.parseInt(pathParams.get("productId"));
            Product productToDelete = db.getProduct(productId);

            if (productToDelete != null) {
                db.deleteProduct(productToDelete.getTitle());
                if (db.getProduct(productId) == null)                               //? assert that product is deleted
                {
                    exchange.sendResponseHeaders(204, -1);
                }
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void PostProductByIdHandler(final HttpExchange exchange, final Map<String, String> pathParams)
            throws IOException {

        try (final InputStream requestBody = exchange.getRequestBody()) {

            final UpdateProductCredentials updateProductCredentials =
                    OBJECT_MAPPER.readValue(requestBody, UpdateProductCredentials.class);

            if (db.getProduct(updateProductCredentials.getId()) != null) {
                String id = String.valueOf(updateProductCredentials.getId());

                if (updateProductCredentials.getTitle() != null) {
                    db.updateProduct("title", updateProductCredentials.getTitle(), "id", id);
                }

                if (updateProductCredentials.getDescription() != null) {
                    db.updateProduct("description", updateProductCredentials.getDescription(), "id", id);
                }

                if (updateProductCredentials.getProducer() != null) {
                    db.updateProduct("producer", updateProductCredentials.getProducer(), "id", id);
                }

                if (updateProductCredentials.getPrice() != null) {
                    if (updateProductCredentials.getPrice() > 0) {
                        db.updateProduct("price", String.valueOf(updateProductCredentials.getPrice()), "id", id);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                if (updateProductCredentials.getQuantity() != null) {
                    if (updateProductCredentials.getQuantity() > 0) {
                        db.updateProduct("quantity", String.valueOf(updateProductCredentials.getQuantity()), "id", id);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                if (updateProductCredentials.getCategory() != null) {
                    db.updateProduct("category", updateProductCredentials.getCategory(), "id", id);
                }

                exchange.sendResponseHeaders(204, -1);

            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such product"));
            }

        } catch (IllegalArgumentException e) {
            ServerAPI.writeResponse(exchange, 409, ErrorResponse.of("Conflict"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void PutProductHandler(final HttpExchange exchange, final Map<String, String> pathParams) {

        try (final InputStream requestBody = exchange.getRequestBody()) {

            final ProductCredentials productCredentials =
                    OBJECT_MAPPER.readValue(requestBody, ProductCredentials.class);
            final Product product = Product.builder().title(productCredentials.getTitle())
                    .description(productCredentials.getDescription()).producer(productCredentials.getProducer())
                    .price(productCredentials.getPrice()).quantity(productCredentials.getQuantity())
                    .category(productCredentials.getCategory()).build();

            final int productId = db.insertProduct(product);

            if (productId != -1) {
                ServerAPI.writeResponse(exchange, 201, "Created " + productId);
            } else {
                ServerAPI.writeResponse(exchange, 409, ErrorResponse.of("Conflict"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void PutCategoryHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try (final InputStream requestBody = exchange.getRequestBody()) {
            final CategoryCredentials categoryCredentials =
                    OBJECT_MAPPER.readValue(requestBody, CategoryCredentials.class);
            final Category categoryToInsert = Category.builder().title(categoryCredentials.getTitle())
                    .description(categoryCredentials.getDescription()).build();

            int row = -1;
            row = db.insertCategory(categoryToInsert);

            if (row > 0) ServerAPI.writeResponse(exchange, 201, "Created " + categoryCredentials.getTitle());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DeleteCategoryHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try (final InputStream requestBody = exchange.getRequestBody()) {
            final CategoryCredentials categoryCredentials =
                    OBJECT_MAPPER.readValue(requestBody, CategoryCredentials.class);
            final String categoryToDeleteTitle = categoryCredentials.getTitle();
            final Category categoryToDelete = db.getCategory(categoryToDeleteTitle);

            if (categoryToDelete != null) {
                db.deleteCategory(categoryToDeleteTitle);
                exchange.sendResponseHeaders(204, -1);
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such category"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PostCategoryHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try (final InputStream requestBody = exchange.getRequestBody()) {
            final CategoryCredentials categoryCredentials =
                    OBJECT_MAPPER.readValue(requestBody, CategoryCredentials.class);
            final String categoryToUpdateTitle = categoryCredentials.getTitle();
            final Category categoryToUpdate = db.getCategory(categoryToUpdateTitle);

            if (categoryToUpdate != null) {

                if (categoryCredentials.getNewTitle() != null) {
                    db.updateCategory("title", categoryCredentials.getNewTitle(), "title", categoryToUpdateTitle);
                }

                if (categoryCredentials.getDescription() != null) {
                    db.updateCategory("description", categoryCredentials.getDescription(), "title",
                            categoryToUpdateTitle);
                }
                exchange.sendResponseHeaders(204, -1);
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such category"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void GetCategoryByTitleHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try (final InputStream requestBody = exchange.getRequestBody()) {
            final CategoryCredentials categoryCredentials =
                    OBJECT_MAPPER.readValue(requestBody, CategoryCredentials.class);
            final Category category = db.getCategory(categoryCredentials.getTitle());

            if (category != null) {
                ServerAPI.writeResponse(exchange, 200, category);
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such category"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SearchForProductHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try (final InputStream requestBody = exchange.getRequestBody()) {
            final SearchQuery searchQuery = OBJECT_MAPPER.readValue(requestBody, SearchQuery.class);

            ProductFilter filter = new ProductFilter();
            filter.setQuery(searchQuery.getQuery());
            List<Product> resList = db.getProductList(0, 10, filter);

            if (resList.size() != 0) {
                ServerAPI.writeResponse(exchange, 200, resList);
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No such products"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void GetAllCategoriesHandler(final HttpExchange exchange, final Map<String, String> pathParams) {
        try {
            List<Category> resList = db.getCategoryList(0, 20);

            if (resList.size() != 0) {
                ServerAPI.writeResponse(exchange, 200, resList);
            } else {
                ServerAPI.writeResponse(exchange, 404, ErrorResponse.of("No categories yet!"));
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
                    final LoginResponse loginResponse =
                            LoginResponse.of(JwtService.generateToken(user), user.getLogin(), user.getRole());
                    ServerAPI.writeResponse(exchange, 200, loginResponse);
                } else {
                    ServerAPI.writeResponse(exchange, 401, ErrorResponse.of("invalid password"));
                }
            } else {
                ServerAPI.writeResponse(exchange, 401, ErrorResponse.of("unknown user"));
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
