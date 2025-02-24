package uri;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class RequestSpec {

    // Base URL of the API
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site";

    // Method to create and return a RequestSpecification
    public static RequestSpecification getRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL) // Set the base URL
                .setContentType(io.restassured.http.ContentType.JSON) // Set content type to JSON
                .addHeader("Accept", "application/json") // Add any common headers
                .build();
    }

    // Method to set the Authorization header with a Bearer token
    public static RequestSpecification setAuth(RequestSpecification requestSpec, String token) {
        return requestSpec.header("Authorization", token);
    }
}