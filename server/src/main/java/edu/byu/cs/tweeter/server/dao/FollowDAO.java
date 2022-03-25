package edu.byu.cs.tweeter.server.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.server.Calculations.UserCalculations;
import edu.byu.cs.tweeter.util.FakeData;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;

/**
 * A DAO for accessing 'following' data from the database.
 */
public class FollowDAO implements IFollowDAO {
    private final Table followsTable;
    private final Table followCountTable;

    public FollowDAO() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        followsTable = dynamoDB.getTable("Follows");
        followCountTable = dynamoDB.getTable("FollowCount");

    }

    @Override
    public FollowingResponse getFollowees(FollowingRequest request) {
        // TODO: Generates dummy data. Replace with a real implementation.
        assert request.getLimit() > 0;
        assert request.getFollowerAlias() != null;

        try {
            QuerySpec spec;
            //this is the first page, so we just query the first 10 items
            if (request.getLastFolloweeAlias() == null) {
                spec = new QuerySpec()
                        .withKeyConditionExpression("follower = :follower")
                        .withValueMap(new ValueMap()
                                .withString(":follower", request.getFollowerAlias()))
                        .withScanIndexForward(true)
                        .withMaxResultSize(request.getLimit());
            }

            //this is every subsequent page, so we query based on last primary key
            else {
                spec = new QuerySpec()
                        .withKeyConditionExpression("follower = :follower")
                        .withValueMap(new ValueMap()
                                .withString(":follower", request.getFollowerAlias()))
                        .withScanIndexForward(true)
                        .withExclusiveStartKey("follower", request.getFollowerAlias(), "followee", request.getLastFolloweeAlias())
                        .withMaxResultSize(request.getLimit());
            }

            ItemCollection<QueryOutcome> items = followsTable.query(spec);

            List<User> responseFollowees = new ArrayList<>();

            Iterator<Item> iterator = items.iterator();
            Item item = null;
            while (iterator.hasNext()) {
                item = iterator.next();
                String followeeAlias = item.getString("followee");
                UserDAO userDAO = new UserDAO();
                User user = userDAO.getUserFromTable(followeeAlias);
                responseFollowees.add(user);
            }

            boolean hasMorePages = false;

            if (items.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey() != null) {
                hasMorePages = true;
            }

            return new FollowingResponse(responseFollowees, hasMorePages);
        } catch (Exception e) {
            e.printStackTrace();
            return new FollowingResponse("failed to get following from DB");
        }
    }

    public List<String> getAllFollowers(String alias) {
        List<String> followers = new ArrayList<>();

        try {

            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression("followee = :followee")
                    .withValueMap(new ValueMap().withString(":followee", alias));

            Index index = followsTable.getIndex("follows_index");

            ItemCollection<QueryOutcome> items = index.query(spec);

            Iterator<Item> iterator = items.iterator();
            Item item = null;
            while (iterator.hasNext()) {
                item = iterator.next();
                String followerAlias = item.getString("follower");
                followers.add(followerAlias);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return followers;
    }

    @Override
    public FollowersResponse getFollowers(FollowersRequest request) {
        assert request.getLimit() > 0;
        assert request.getFolloweeAlias() != null;

        try {
            QuerySpec spec;
            Index index = followsTable.getIndex("follows_index");
            //this is the first page, so we just query the first 10 items
            if (request.getLastFollowerAlias() == null) {
                spec = new QuerySpec()
                        .withKeyConditionExpression("followee = :followee")
                        .withValueMap(new ValueMap()
                                .withString(":followee", request.getFolloweeAlias()))
                        .withScanIndexForward(true)
                        .withMaxResultSize(request.getLimit());
            }

            //this is every subsequent page, so we query based on last primary key
            else {
                spec = new QuerySpec()
                        .withKeyConditionExpression("followee = :followee")
                        .withValueMap(new ValueMap()
                                .withString(":followee", request.getFolloweeAlias()))
                        .withScanIndexForward(true)
                        .withExclusiveStartKey("followee", request.getFolloweeAlias(), "follower", request.getLastFollowerAlias())
                        .withMaxResultSize(request.getLimit());
            }

            ItemCollection<QueryOutcome> items = index.query(spec);

            List<User> responseFollowers = new ArrayList<>();

            Iterator<Item> iterator = items.iterator();
            Item item = null;
            while (iterator.hasNext()) {
                item = iterator.next();
                String followerAlias = item.getString("follower");
                UserDAO userDAO = new UserDAO();
                User user = userDAO.getUserFromTable(followerAlias);
                responseFollowers.add(user);
            }

            boolean hasMorePages = false;

            if (items.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey() != null) {
                hasMorePages = true;
            }
            return new FollowersResponse(responseFollowers, hasMorePages);
        } catch (Exception e) {
            e.printStackTrace();
            return new FollowersResponse("failed to get followers from DB");
        }
    }

    @Override
    public FollowersCountResponse getFollowersCount(FollowersCountRequest request) {
        assert request.getFolloweeAlias() != null;

        try {
            GetItemSpec followerCountSpec = new GetItemSpec()
                    .withPrimaryKey("alias", request.getFolloweeAlias())
                    .withProjectionExpression("followerCount");

            Item followerCountItem = followCountTable.getItem(followerCountSpec);
            int followerCount = followerCountItem.getInt("followerCount");
            return new FollowersCountResponse(followerCount);
        } catch (Exception e) {
            e.printStackTrace();
            return new FollowersCountResponse("failed to get followers count from DB");
        }

    }

    @Override
    public FollowingCountResponse getFolloweeCount(FollowingCountRequest request) {
        // TODO: uses the dummy data.  Replace with a real implementation.
        assert request.getFollowerAlias() != null;

        try {
            GetItemSpec followeeCountSpec = new GetItemSpec()
                    .withPrimaryKey("alias", request.getFollowerAlias())
                    .withProjectionExpression("followeeCount");

            Item followeeCountItem = followCountTable.getItem(followeeCountSpec);
            int followeeCount = followeeCountItem.getInt("followeeCount");
            return new FollowingCountResponse(followeeCount);
        } catch (Exception e) {
            e.printStackTrace();
            return new FollowingCountResponse("failed to get followee count from DB");
        }
    }

    @Override
    public FollowResponse follow(FollowRequest request) {
        //request = followee alias and authToken object
        assert request.getFolloweeAlias() != null;

        try {
            AuthTokenDAO authTokenDAO = new AuthTokenDAO();
            String alias = authTokenDAO.getAliasFromToken(request.getAuthToken().getToken());

            Item followItem = followsTable.getItem("follower", alias, "followee", request.getFolloweeAlias());

            if (followItem != null) {
                throw new Exception("already following user");
            }

            //need current user and followee aliases
            PutItemOutcome putItemOutcome = followsTable
                    .putItem(new Item().withPrimaryKey("follower", alias, "followee", request.getFolloweeAlias()));

            //update followee count with 1
            GetItemSpec followeeCountSpec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("followeeCount");

            Item followeeCountItem = followCountTable.getItem(followeeCountSpec);
            int followeeCount = followeeCountItem.getInt("followeeCount");

            UpdateItemSpec updateFolloweeCountSpec = new UpdateItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withUpdateExpression("set followeeCount = :count")
                    .withValueMap(new ValueMap().withNumber(":count", followeeCount + 1));

            UpdateItemOutcome followeeCountOutcome = followCountTable.updateItem(updateFolloweeCountSpec);

            //update followee's follower count
            GetItemSpec followerCountSpec = new GetItemSpec()
                    .withPrimaryKey("alias", request.getFolloweeAlias())
                    .withProjectionExpression("followerCount");

            Item followerCountItem = followCountTable.getItem(followerCountSpec);

            int followerCount = followerCountItem.getInt("followerCount");

            UpdateItemSpec updateFollowerCountSpec = new UpdateItemSpec()
                    .withPrimaryKey("alias", request.getFolloweeAlias())
                    .withUpdateExpression("set followerCount = :count")
                    .withValueMap(new ValueMap().withNumber(":count", followerCount + 1));

            UpdateItemOutcome followerCountOutcome = followCountTable.updateItem(updateFollowerCountSpec);

            return new FollowResponse();
        } catch(Exception e) {
            e.printStackTrace();
            return new FollowResponse("Failed to follow from DB");
        }
    }

    @Override
    public UnfollowResponse unfollow(UnfollowRequest request) {
        assert request.getFolloweeAlias() != null;
        //request = authToken and followee alias

        //delete followee follower item
        try {
            AuthTokenDAO authTokenDAO = new AuthTokenDAO();

            String alias = authTokenDAO.getAliasFromToken(request.getAuthToken().getToken());

            DeleteItemOutcome deleteItemOutcome = followsTable.deleteItem("follower", alias, "followee", request.getFolloweeAlias());

            //update follower's followee count
            GetItemSpec followeeCountSpec = new GetItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withProjectionExpression("followeeCount");

            Item followeeCountItem = followCountTable.getItem(followeeCountSpec);
            int followeeCount = followeeCountItem.getInt("followeeCount");

            UpdateItemSpec updateFolloweeCountSpec = new UpdateItemSpec()
                    .withPrimaryKey("alias", alias)
                    .withUpdateExpression("set followeeCount = :count")
                    .withValueMap(new ValueMap().withNumber(":count", followeeCount - 1));

            UpdateItemOutcome followeeCountOutcome = followCountTable.updateItem(updateFolloweeCountSpec);

            //update followee's follower count
            GetItemSpec followerCountSpec = new GetItemSpec()
                    .withPrimaryKey("alias", request.getFolloweeAlias())
                    .withProjectionExpression("followerCount");

            Item followerCountItem = followCountTable.getItem(followerCountSpec);

            int followerCount = followerCountItem.getInt("followerCount");

            UpdateItemSpec updateFollowerCountSpec = new UpdateItemSpec()
                    .withPrimaryKey("alias", request.getFolloweeAlias())
                    .withUpdateExpression("set followerCount = :count")
                    .withValueMap(new ValueMap().withNumber(":count", followerCount - 1));

            UpdateItemOutcome followerCountOutcome = followCountTable.updateItem(updateFollowerCountSpec);

            //need to remove status from feed
            return new UnfollowResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return new UnfollowResponse("failed to unfollow from DB");
        }
    }

    @Override
    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        assert request.getFollowerAlias() != null;
        assert request.getFolloweeAlias() != null;

        try {
            boolean value = true;
            Item followerItem = followsTable.getItem("follower", request.getFollowerAlias(), "followee", request.getFolloweeAlias());

            if (followerItem == null) {
                value = false;
            }
            return new IsFollowerResponse(value);
        } catch (Exception e) {
            e.printStackTrace();
            return new IsFollowerResponse("error getting data from db for is follower");
        }
    }
}
