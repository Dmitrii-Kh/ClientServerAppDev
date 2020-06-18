package TestLab05;


import io.restassured.RestAssured;
import lab04.entities.Category;
import lab04.entities.Product;
import lab04.entities.User;
import lab05.Endpoints;
import lab05.HTTP.Endpoint;
import lab05.Server;
import lab05.domain.LoginResponse;
import lab05.domain.ProductCredentials;
import lab05.domain.UpdateProductCredentials;
import lab05.domain.UserCredentials;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

import java.io.IOException;

public class HTTPServerTest {

    private static Server server;
    private static Endpoints endpoints;
    private static int port = 8080;

    @BeforeAll
    static void init() throws IOException {
        server = new Server(port, 3);
        endpoints = new Endpoints();
        for (Endpoint endpoint : endpoints.getAllEndpoints()) {
            server.addEndpoint(endpoint);
        }
        RestAssured.port = port;

        server.db().insertUser(User.builder()
                .login("admin")
                .password(DigestUtils.md5Hex("password"))
                .role("admin")
                .build());


    }

    @BeforeEach
    void prepareDB() {
        server.db().insertCategory(Category.builder().title("food").description("smth to eat").build());

        for (int i = 0; i < 7; i++) {
            server.db().insertProduct(Product.builder()
                    .title("product" + i)
                    .description("description")
                    .producer("producer" + i)
                    .price(Math.random() * 100)
                    .quantity(i * i + 1)
                    .category("food")
                    .build());
        }
    }

    @AfterEach
    void cleanDB() {
        server.db().deleteAllCategories();
        server.db().deleteAllProducts();
    }

    private static LoginResponse getToken(final String login, final String password) {
        return given()
                .body(UserCredentials.of(login, password))
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()))
                .extract()
                .as(LoginResponse.class);
    }

    @Test
    void shouldLogin_whenValidCredentials() {
        getToken("admin", "password");
    }


    @Test
    void shouldReturn401_whenInvalidLogin() {
        given()
                .body(UserCredentials.of("unknown_login", "password"))
                .when()
                .post("/login")
                .then()
                .statusCode(401)
                .body("message", is("unknown user"));
    }


    @Test
    void shouldReturnProduct_whenValidToken() {
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .when()
                .get("/api/product/3")
                .then()
                .statusCode(200)
                .body("id", is(3))
                .body("title", is("product2"));
    }


    @Test
    void shouldReturn403_whenGetProductWithoutToken() {
        when()
                .post("/api/product/3")
                .then()
                .statusCode(403)
                .body("message", is("No permission"));

    }

    @Test
    void shouldReturn404_whenProductNotFound() {
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .when()
                .get("/api/product/345")
                .then()
                .statusCode(404);
    }


    @Test
    void shouldReturn201_whenProductCreated() {
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .body(ProductCredentials.of("Apple", "food", "nature", 10.0, 10, "food"))
        .when()
                .put("/api/product")
        .then()
                .statusCode(201);


    }

    @Test
    void shouldReturn409_whenWrongParamsPassed() {
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .body(ProductCredentials.of("Apple", "food", "nature", -10.0, 10, "food"))
                .when()
                .put("/api/product")
                .then()
                .statusCode(409)
                .body("message", is("Conflict"));
    }

    @Test
    void shouldReturn204_whenProductModified(){
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .body(UpdateProductCredentials.of(3,"Apple", "food", "nature", 10.0, 10, "food"))
                .when()
                .post("/api/product")
                .then()
                .statusCode(204);
    }

    @Test
    void shouldReturn409_whenWrongParamsPassedInPOST(){
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .body(UpdateProductCredentials.of(3,"Apple", "food", "nature", 10.0, -10, "food"))
                .when()
                .post("/api/product")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldReturn404_whenNoSuchProductInPOST(){
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .body(UpdateProductCredentials.of(1999,"Apple", "food", "nature", 10.0, -10, "food"))
                .when()
                .post("/api/product")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn204_whenProductDeletedSuccessfully(){
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .when()
                .delete("/api/product/4")
                .then()
                .statusCode(204);
    }

    @Test
    void shouldReturn204and404_whenProductDeletedSuccessfully(){
        final LoginResponse loginResponse = getToken("admin", "password");

        given()
                .header("Authorization", loginResponse.getToken())
                .when()
                .delete("/api/product/4")
                .then()
                .statusCode(204);

        given()
                .header("Authorization", loginResponse.getToken())
                .when()
                .get("/api/product/4")
                .then()
                .statusCode(404);
    }

    @AfterAll
    static void serverStop() {
        server.db().deleteAllUsers();
        server.db().shutdown();
        server.stop();
        server = null;
    }
}
