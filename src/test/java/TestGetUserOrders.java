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

@RunWith(Parameterized.class)
public class TestGetUserOrders {

    private String accessToken; // To store the access token of the created user
    private final boolean isAuthorized; // To determine if the request is authorized
    private final int expectedStatusCode; // Expected status code for the test
    private String expectedSuccess; // Expected value of the "success" key in the response

    // Create an instance of UserApiClient
    UserApiClient userApiClient = new UserApiClient();

    // Constructor for parameterized test
    public TestGetUserOrders(boolean isAuthorized, int expectedStatusCode, String expectedSuccess) {
        this.isAuthorized = isAuthorized;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedSuccess = expectedSuccess;
    }

    // Parameters for the tests
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {true, 200, "true"}, // Test with authorization
                {false, 401, "false"} // Test without authorization
        });
    }

    // Create a user and an order before running the tests
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

        // Create an order for the user
        OrderRequest orderRequest = new OrderRequest(
                Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72", "61c0c5a71d1f82001bdaaa6f") // Valid ingredients
        );

        // Send a request to create the order
        Response orderResponse = userApiClient.createOrder(orderRequest, this.accessToken);

        // Verify that the order was successfully created
        userApiClient.assertResponse(orderResponse, 200);
    }

    // Test for retrieving user orders
    @Test
    public void testGetUserOrders() {
        // Send a request to retrieve user orders
        Response response;
        if (isAuthorized) {
            // With authorization
            response = userApiClient.getUserOrders(this.accessToken);
        } else {
            // Without authorization
            response = userApiClient.getUserOrders(null);
        }

        // Verify the status code
        userApiClient.assertResponse(response, this.expectedStatusCode);

        // Verify the value of the "success" key in the response body
        userApiClient.assertResponseMessage(response, "success", this.expectedSuccess);
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