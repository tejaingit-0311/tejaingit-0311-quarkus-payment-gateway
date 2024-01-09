package payment.gateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testLogin() {
        given()
          .when().get("/user/saveproducts")
          .then()
             .statusCode(405)
             .body(is("Products saved"));
    }

}