package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.server.Calculations.UserCalculations;
import edu.byu.cs.tweeter.util.FakeData;

public class StatusDAO implements IStatusDAO {
    private final Table storyTable;
    private final Table feedTable;

    public StatusDAO() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);
        storyTable = dynamoDB.getTable("Story");
        feedTable = dynamoDB.getTable("Feed");

        System.out.println("got users and authtoken table successfully");
    }

    @Override
    public FeedResponse getFeed(FeedRequest request) {
        assert request.getLimit() > 0;
        assert request.getUserAlias() != null;

        QuerySpec spec;
        //this is the first page, so we just query the first 10 items
        if (request.getLastStatus() == null) {
            spec = new QuerySpec()
                    .withKeyConditionExpression("owner_alias = :alias")
                    .withValueMap(new ValueMap()
                            .withString(":alias", request.getUserAlias()))
                    .withScanIndexForward(true)
                    .withMaxResultSize(request.getLimit());
        }

        //this is every subsequent page, so we query based on last primary key
        else {
            spec = new QuerySpec()
                    .withKeyConditionExpression("owner_alias = :alias")
                    .withValueMap(new ValueMap()
                            .withString(":alias", request.getUserAlias()))
                    .withScanIndexForward(true)
                    .withExclusiveStartKey("owner_alias", request.getUserAlias(), "timeStamp", request.getLastStatus().getDate())
                    .withMaxResultSize(request.getLimit());
        }

        ItemCollection<QueryOutcome> items = feedTable.query(spec);

        List<Status> responseStatuses = new ArrayList<>();

        Iterator<Item> iterator = items.iterator();
        Item item = null;
        while (iterator.hasNext()) {
            item = iterator.next();
            String post = item.getString("post");
            List<String> links = item.getList("links");
            List<String> mentions = item.getList("mentions");
            String timeStamp = item.getString("timeStamp");
            String alias = item.getString("author_alias");
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserFromTable(alias);
            Status status = new Status(post, user, timeStamp, links, mentions);
            responseStatuses.add(status);

            System.out.println(item.toJSONPretty());
        }

        boolean hasMorePages = false;

        if (items.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey() != null) {
            hasMorePages = true;
        }

        return new FeedResponse(responseStatuses, hasMorePages);
    }

    @Override
    public StoryResponse getStory(StoryRequest request) {
        assert request.getLimit() > 0;
        assert request.getUserAlias() != null;

        QuerySpec spec;
        //this is the first page, so we just query the first 10 items
        if (request.getLastStatus() == null) {
            spec = new QuerySpec()
                    .withKeyConditionExpression("alias = :alias")
                    .withValueMap(new ValueMap()
                            .withString(":alias", request.getUserAlias()))
                    .withScanIndexForward(false)
                    .withMaxResultSize(request.getLimit());
        }

        //this is every subsequent page, so we query based on last primary key
        else {
            spec = new QuerySpec()
                    .withKeyConditionExpression("alias = :alias")
                    .withValueMap(new ValueMap()
                            .withString(":alias", request.getUserAlias()))
                    .withScanIndexForward(false)
                    .withExclusiveStartKey("alias", request.getUserAlias(), "timeStamp", request.getLastStatus().getDate())
                    .withMaxResultSize(request.getLimit());
        }

        ItemCollection<QueryOutcome> items = storyTable.query(spec);

        List<Status> responseStatuses = new ArrayList<>();

        Iterator<Item> iterator = items.iterator();
        Item item = null;
        while (iterator.hasNext()) {
            item = iterator.next();
            String post = item.getString("post");
            List<String> links = item.getList("links");
            List<String> mentions = item.getList("mentions");
            String timeStamp = item.getString("timeStamp");
            String alias = item.getString("alias");
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserFromTable(alias);
            Status status = new Status(post, user, timeStamp, links, mentions);
            responseStatuses.add(status);

            System.out.println(item.toJSONPretty());
        }

        boolean hasMorePages = false;

        if (items.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey() != null) {
            hasMorePages = true;
        }

        return new StoryResponse(responseStatuses, hasMorePages);
    }

    @Override
    public PostStatusResponse postStatus(PostStatusRequest request) {

        try {
            AuthTokenDAO authTokenDAO = new AuthTokenDAO();
            String alias = authTokenDAO.getAliasFromToken(request.getAuthToken().getToken());

            //need current user and followee aliases
            PutItemOutcome putItemOutcome = storyTable
                    .putItem(new Item().withPrimaryKey("alias", alias, "timeStamp", request.getStatus().getDate())
                            .withString("post", request.getStatus().getPost())
                            .withList("mentions", request.getStatus().getMentions())
                            .withList("links", request.getStatus().getUrls()));

            System.out.println("successfully posted in story table:\n" + putItemOutcome.getPutItemResult());

            //need to post status in current follower's feeds
            FollowDAO followDAO = new FollowDAO();
            List<String> followers = followDAO.getAllFollowers(alias);

            for (String follower : followers) {
                feedTable.putItem(new Item()
                        .withPrimaryKey("owner_alias", follower, "timeStamp", request.getStatus().getDate())
                        .withString("author_alias", alias)
                        .withString("post", request.getStatus().getPost())
                        .withList("mentions", request.getStatus().getMentions())
                        .withList("links", request.getStatus().getUrls()));
            }
            return new PostStatusResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return new PostStatusResponse("failed to post status in DB");
        }
    }

}
