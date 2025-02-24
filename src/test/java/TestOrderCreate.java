import api.UserApiClient;
import io.restassured.response.Response;
import json.OrderRequest;
import json.UserRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class TestOrderCreate {

    private String accessToken; // To store the access token of the created user
    private final boolean isAuthorized; // To determine if the request is authorized
    private final List<String> ingredients; // List of ingredients for the order
    private final int expectedStatusCode; // Expected status code for the test
    private final String expectedSuccess; // Expected value of the "success" key in the response

    // Create an instance of UserApiClient
    UserApiClient userApiClient = new UserApiClient();

    // Constructor for parameterized test
    public TestOrderCreate(boolean isAuthorized, List<String> ingredients, int expectedStatusCode, String expectedSuccess) {
        this.isAuthorized = isAuthorized;
        this.ingredients = ingredients;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedSuccess = expectedSuccess;
    }

    // Parameters for the tests
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true, Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72", "61c0c5a71d1f82001bdaaa6f"), 200, "true"}, // With authorization and valid ingredients
                {false, Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72", "61c0c5a71d1f82001bdaaa6f"), 200, "true"}, // Without authorization but with valid ingredients
                {true, List.of(), 400, "false"}, // With authorization but without ingredients
                {true, Arrays.asList("invalidIngredient1", "invalidIngredient2"), 500, "false"} // With authorization and invalid ingredients
        });
    }

    // Create a user before running the tests
    @Before
    public void setUp() {
        // Generate a random email
        String randomEmail = UserApiClient.generateRandomEmail();

        // Create a request to register a user
        UserRequest userRequest = new UserRequest(
                randomEmail,
                "password",
                "Username"
        );

        // Send a request to create the user
        Response response = userApiClient.createUser(userRequest);

        // Verify that the user was successfully created
        userApiClient.assertResponse(response, 200);

        // Save the email and access token for cleanup
        this.accessToken = userApiClient.extractAccessToken(response);
    }

    // Test for creating an order
    @Test
    public void testOrderCreate() {
        // Create the request body for creating an order
        OrderRequest orderRequest = new OrderRequest(
                this.ingredients
        );

        // Send a request to create an order
        Response response;
        if (isAuthorized) {
            // With authorization
            response = userApiClient.createOrder(orderRequest, this.accessToken);
        } else {
            // Without authorization
            response = userApiClient.createOrder(orderRequest, null);
        }

        // Verify the status code
        userApiClient.assertResponse(response, this.expectedStatusCode);

        // Verify the value of the "success" key in the response body (if applicable)
        if (this.expectedStatusCode != 500) { // Skip for 500 status code
            userApiClient.assertResponseMessage(response, "success", this.expectedSuccess);
        }
    }

    // Delete the user after running the tests
    @After
    public void tearDown() {
        if (this.accessToken != null) {
            // Delete the user
            Response response = userApiClient.deleteUser(this.accessToken);

            // Verify that the user was successfully deleted
            userApiClient.assertResponse(response, 202);
        }
    }
}