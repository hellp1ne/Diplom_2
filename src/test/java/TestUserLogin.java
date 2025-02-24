import api.UserApiClient;
import io.restassured.response.Response;
import json.UserLoginRequest;
import json.UserRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestUserLogin {

    private String email;
    private final String password;
    private final int expectedStatusCode;
    private final String expectedMessage;
    private String accessToken; // To store the access token of the created user

    // Create an instance of UserApiClient
    UserApiClient userApiClient = new UserApiClient();

    // Constructor for parameterized test
    public TestUserLogin(String email, String password, int expectedStatusCode, String expectedMessage) {
        this.email = email;
        this.password = password;
        this.expectedStatusCode = expectedStatusCode;
        this.expectedMessage = expectedMessage;
    }

    // Parameters for the tests
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, "password", 200, "true"}, // Successful login
                {"nonexistent@example.com", "wrong-password", 401, "false"} // Unsuccessful login
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

        // Replace the email in the first parameterized test case with the random email
        if (email == null) {
            email = randomEmail;
        }
    }

    // Test for user login
    @Test
    public void testUserLogin() {
        // Create the request body for login
        UserLoginRequest userLoginRequest = new UserLoginRequest(
                this.email,
                this.password
        );

        // Send a request to log in
        Response response = userApiClient.loginUser(userLoginRequest);

        // Verify the status code
        userApiClient.assertResponse(response, this.expectedStatusCode);

        // If an error message is expected, verify it
        userApiClient.assertResponseMessage(response, "success", this.expectedMessage);

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