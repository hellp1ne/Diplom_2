import api.UserApiClient;
import io.restassured.response.Response;
import json.UserRequest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestUserCreation {

    // Create a UserApiClient instance
    UserApiClient userApiClient = new UserApiClient();

    private String createdUserEmail; // Store the email of the created user
    private String accessToken; // Store the access token for cleanup

    private final UserRequest userRequest;
    private final String expectedMessage;

    // Constructor for parameterized test
    public TestUserCreation(UserRequest userRequest, String expectedMessage) {
        this.userRequest = userRequest;
        this.expectedMessage = expectedMessage;
    }

    // Data provider for parameterized test
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new UserRequest(null, "password", "Username"), "Email, password and name are required fields"},
                {new UserRequest(UserApiClient.generateRandomEmail(), null, "Username"), "Email, password and name are required fields"},
                {new UserRequest(UserApiClient.generateRandomEmail(), "password", null), "Email, password and name are required fields"}
        });
    }

    @Test
    public void testCreateUserMissingRequiredField() {

        // Step 1: Send POST request to create a user with missing required field
        Response response = userApiClient.createUser(userRequest);

        // Step 2: Assert the response status code is 403
        userApiClient.assertResponse(response, 403);

        // Step 3: Assert the value of the "message" key in the response body
        userApiClient.assertResponseMessage(response, "message", expectedMessage);
    }

    @Test
    public void testCreateUser() {

        // Step 1: Create a UserRequest object with a random email
        String randomEmail = UserApiClient.generateRandomEmail();
        UserRequest userRequest = new UserRequest(
                randomEmail,
                "password",
                "Username"
        );

        // Step 2: Send POST request to create a user
        Response response = userApiClient.createUser(userRequest);

        // Step 3: Assert the response
        userApiClient.assertResponse(response, 200);

        // Step 4: Extract the access token from the response
        this.accessToken = userApiClient.extractAccessToken(response);

        // Store the email of the created user for cleanup
        this.createdUserEmail = randomEmail;
    }

    @Test
    public void testCreateUserAlreadyExists() {

        // Step 1: Create a UserRequest object with a random email
        String randomEmail = UserApiClient.generateRandomEmail();
        UserRequest userRequest = new UserRequest(
                randomEmail,
                "password",
                "Username"
        );

        // Step 2: Send POST request to create a user (first time)
        Response response = userApiClient.createUser(userRequest);

        // Step 3: Extract the access token from the response
        this.accessToken = userApiClient.extractAccessToken(response);

        // Step 4: Send POST request to create the same user again (second time)
        response = userApiClient.createUser(userRequest);

        // Step 5: Assert the response status code is 403
        userApiClient.assertResponse(response, 403);

        // Step 6: Assert the value of the "message" key in the response body
        userApiClient.assertResponseMessage(response, "message", "User already exists");

        // Store the email of the created user for cleanup
        this.createdUserEmail = randomEmail;
    }

    @After
    public void tearDown() {
        // Cleanup: Delete the created user
        if (createdUserEmail != null && accessToken != null) {

            // Send DELETE request to delete the user
            Response response = userApiClient.deleteUser(accessToken);

            // Assert the response status code is 200 (or the expected code for successful deletion)
            userApiClient.assertResponse(response, 202);
        }
    }
}