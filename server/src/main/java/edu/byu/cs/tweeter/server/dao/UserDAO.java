package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.Update;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;
import edu.byu.cs.tweeter.server.Calculations.UserCalculations;


public class UserDAO implements IUserDAO {
    private final Table userTable;
    private final Table followCountTable;

    public UserDAO() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        userTable = dynamoDB.getTable("Users");
        followCountTable = dynamoDB.getTable("FollowCount");

    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        try {
            String alias = request.getUsername();

            //if alias already exists, don't let them register again
            Item item = userTable.getItem("alias", alias);
            if (item != null) {
                throw new Exception("username already exists!");
            }

            String firstName = request.getFirstName();
            String lastName = request.getLastName();
            String salt = UserCalculations.removeNonAlphanumeric(UserCalculations.getSalt());

            String password = UserCalculations.getSecurePassword(request.getPassword(), salt);

            String imageKey = firstName + lastName;
            String imageURL = UserCalculations.uploadImageToS3(request.getImage(), imageKey);

            //add user to table after uploading image to s3
            PutItemOutcome outcome = userTable
                    .putItem(new Item().withPrimaryKey("alias", alias)
                            .withString("firstName", firstName)
                            .withString("lastName", lastName)
                            .withString("password", password)
                            .withString("image", imageURL)
                            .withString("salt", salt));

            //update follow count table with the user and initialize it with 0
            PutItemOutcome putItemOutcome = followCountTable
                    .putItem(new Item().withPrimaryKey("alias", alias)
                    .withInt("followerCount", 0)
                    .withInt("followeeCount", 0));

            User user = new User(firstName, lastName, alias, imageURL);

            AuthTokenDAO authTokenDAO = new AuthTokenDAO();
            AuthToken newAuthToken = authTokenDAO.setAuthToken(alias);

            return new RegisterResponse(user, newAuthToken);
        }
        catch (Exception e) {
            return new RegisterResponse(e.getMessage());
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {

            String alias = request.getUsername();
            String password = request.getPassword();

            //get original salt from DB
            GetItemSpec spec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("salt");

            Item salt = userTable.getItem(spec);

            //make new hash password with request
            String hashedPassword = UserCalculations.getSecurePassword(password, UserCalculations.removeNonAlphanumeric(salt.getString("salt")));

            //get expected password from DB
            GetItemSpec passwordSpec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("password");

            Item expectedPasswordItem = userTable.getItem(passwordSpec);

            String expectedPassword = UserCalculations.removeNonAlphanumeric(expectedPasswordItem.getString("password"));

            //compare expected with actual
            if (!expectedPassword.equals(hashedPassword)) {
                throw new Exception("incorrect username or password");
            }

            //set authToken and timeStamp values
            AuthTokenDAO authTokenDAO = new AuthTokenDAO();
            AuthToken newAuthToken = authTokenDAO.setAuthToken(alias);

            //set new logged in user and return
            User loggedInUser = getUserFromTable(alias);

            return new LoginResponse(loggedInUser, newAuthToken);
        } catch (Exception e) {
            return new LoginResponse(e.getMessage());
        }
    }

    @Override
    public User getUserFromTable(String alias) {

        try {
            GetItemSpec firstNameSpec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("firstName");

            String firstName = userTable.getItem(firstNameSpec).getString("firstName");

            GetItemSpec lastNameSpec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("lastName");

            String lastName = userTable.getItem(lastNameSpec).getString("lastName");

            GetItemSpec imageSpec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("image");

            String image = userTable.getItem(imageSpec).getString("image");
            return new User(firstName, lastName, alias, image);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserResponse getUser(UserRequest request) {
        try {
            String alias = request.getUserAlias();
            User user = getUserFromTable(alias);

            return new UserResponse(user);
        } catch (Exception e) {
            return new UserResponse("Error getting user: " + e.getMessage());
        }
    }

    @Override
    public LogoutResponse logout(LogoutRequest request) {
        //delete authTokens
        return new LogoutResponse();
    }
}
