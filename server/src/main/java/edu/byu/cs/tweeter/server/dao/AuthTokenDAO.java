package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
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
import java.util.concurrent.TimeUnit;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.Calculations.UserCalculations;

public class AuthTokenDAO implements IAuthTokenDAO{
    private Table authTokenTable;

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
            GetItemSpec authTokenSpec = new GetItemSpec()
                    .withPrimaryKey("authToken", authToken)
                    .withProjectionExpression("timeStamp");

            String originalTimeStamp = authTokenTable.getItem(authTokenSpec).getString("timeStamp");

            String currentTimeStamp = UserCalculations.getTimeStamp();

            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

            Date time1 = null;
            Date time2 = null;

            time1 = format.parse(originalTimeStamp);
            time2 = format.parse(currentTimeStamp);

            long difference = time2.getTime() - time1.getTime();
            long minutesElapsed = TimeUnit.MILLISECONDS.toMinutes(difference);

            if (minutesElapsed <= 2) {
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
            UpdateItemSpec updateAuthTokenSpec = new UpdateItemSpec()
                    .withPrimaryKey("authToken", token)
                    .withUpdateExpression("set timeStamp = :ts")
                    .withValueMap(new ValueMap().withString(":ts", UserCalculations.getTimeStamp()));

            UpdateItemOutcome updateAuthTokenOutcome = authTokenTable.updateItem(updateAuthTokenSpec);

            System.out.println("updated authtoken date successfully:\n" + updateAuthTokenOutcome.getUpdateItemResult());
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

        System.out.println("PutItem succeeded for AuthToken:\n" + outcome.getPutItemResult());

        return new AuthToken(authToken, timeStamp);
    }
}
