import api.UserApiClient;
import io.restassured.response.Response;
import json.UserRequest;
import json.UserUpdateRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestUserUpdate {

    private String accessToken; // To store the access token of the created user
    private final boolean isAuthorized; // To determine if the request is authorized
    private final int expectedStatusCode; // Expected status code for the test
    private final String expectedSuccess; // Expected value of the "success" key in the response

    UserApiClient userApiClient = new UserApiClient();

    // Constructor for parameterized test
    public TestUserUpdate(boolean isAuthorized, int expectedStatusCode, String expectedSuccess) {
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

        // Save the access token for cleanup
        this.accessToken = userApiClient.extractAccessToken(response);
    }

    // Test for updating user data
    @Test
    public void testUserUpdate() {
        // Create the request body for updating user data
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                "new-email546464@example.com",
                "New Name",
                "new-password");

        // Send a request to update user data
        Response response;

        if (isAuthorized) {
            // With authorization
            response = userApiClient.updateUser(userUpdateRequest, this.accessToken);
        } else {
            // Without authorization
            response = userApiClient.updateUser(userUpdateRequest, null);
        }

        // Verify the status code
        userApiClient.assertResponse(response, this.expectedStatusCode);

        // Verify the value of the "success" key in the response body
        userApiClient.assertResponseMessage(response, "success", expectedSuccess);
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