package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.Calculations.UserCalculations;

public class AuthTokenDAO implements IAuthTokenDAO{
    private final Table authTokenTable;

    public AuthTokenDAO() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);
        authTokenTable = dynamoDB.getTable("AuthTokens");
    }

    @Override
    public boolean isValidToken(AuthToken authToken) {
        boolean isValid = false;

        try {
            Map<String, String> nameMap = new HashMap<>();
            nameMap.put("#ts", "timeStamp");

            GetItemSpec authTokenSpec = new GetItemSpec()
                    .withPrimaryKey("authToken", authToken.getToken())
                    .withProjectionExpression("#ts")
                    .withNameMap(nameMap);

            String originalTimeStamp = authTokenTable.getItem(authTokenSpec).getString("timeStamp");

            String currentTimeStamp = UserCalculations.getTimeStamp();

            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

            Date time1 = null;
            Date time2 = null;

            time1 = format.parse(originalTimeStamp);
            time2 = format.parse(currentTimeStamp);

            long difference = time2.getTime() - time1.getTime();
            long minutesElapsed = TimeUnit.MILLISECONDS.toMinutes(difference);

            if (minutesElapsed <= 3) {
                isValid = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isValid;
    }

    @Override
    public void updateAuthToken(String token) {
        try {
            Map<String, String> nameMap = new HashMap<>();
            nameMap.put("#ts", "timeStamp");

            UpdateItemSpec updateAuthTokenSpec = new UpdateItemSpec()
                    .withPrimaryKey("authToken", token)
                    .withNameMap(nameMap)
                    .withUpdateExpression("set #ts = :ts")
                    .withValueMap(new ValueMap().withString(":ts", UserCalculations.getTimeStamp()));

            UpdateItemOutcome updateAuthTokenOutcome = authTokenTable.updateItem(updateAuthTokenSpec);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAliasFromToken(String token) {
        GetItemSpec authTokenSpec = new GetItemSpec()
                .withPrimaryKey("authToken", token)
                .withProjectionExpression("alias");

        return authTokenTable.getItem(authTokenSpec).getString("alias");
    }

    public AuthToken setAuthToken(String alias) {
        String authToken = UserCalculations.getAuthToken();
        String timeStamp = UserCalculations.getTimeStamp();

        PutItemOutcome outcome = authTokenTable
                .putItem(new Item().withPrimaryKey("authToken", authToken)
                        .withString("timeStamp", timeStamp)
                        .withString("alias", alias));

        return new AuthToken(authToken, timeStamp);
    }

    @Override
    public void deleteUserToken(String token) {
        DeleteItemOutcome outcome = authTokenTable.deleteItem("authToken", token);
    }
}
