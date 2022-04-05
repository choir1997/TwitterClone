package edu.byu.cs.tweeter.server.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;

public class StatusDAO implements IStatusDAO {
    private final Table storyTable;
    private final Table feedTable;
    private final DynamoDB dynamoDB;

    public StatusDAO() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        dynamoDB = new DynamoDB(client);
        storyTable = dynamoDB.getTable("Story");
        feedTable = dynamoDB.getTable("Feed");

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
                    .withScanIndexForward(false)
                    .withValueMap(new ValueMap()
                            .withString(":alias", request.getUserAlias()))

                    .withMaxResultSize(request.getLimit());
        }

        //this is every subsequent page, so we query based on last primary key
        else {
            spec = new QuerySpec()
                    .withKeyConditionExpression("owner_alias = :alias")
                    .withValueMap(new ValueMap()
                            .withString(":alias", request.getUserAlias()))
                    .withScanIndexForward(false)
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
            String post = request.getStatus().getPost();
            String date = request.getStatus().getDate();
            List<String> mentions = request.getStatus().getMentions();
            List<String> urls = request.getStatus().getUrls();


            //need current user and followee aliases
            PutItemOutcome putItemOutcome = storyTable
                    .putItem(new Item().withPrimaryKey("alias", alias, "timeStamp", request.getStatus().getDate())
                            .withString("post", post)
                            .withList("mentions", mentions)
                            .withList("links", urls));

            //todo: update SQS with status request

            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserFromTable(alias);
            Status status = new Status(post, user, date, urls, mentions);
            String messageBody = new Gson().toJson(status);
            String queueUrl = "https://sqs.us-east-1.amazonaws.com/386643631519/PostStatusQueue";

            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(messageBody);

            AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
            SendMessageResult send_msg_result = sqs.sendMessage(send_msg_request);

            String msgId = send_msg_result.getMessageId();
            System.out.println("Message ID: " + msgId);

            return new PostStatusResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return new PostStatusResponse("failed to post status in DB");
        }
    }

    @Override
    public void updateFeedsBatch(Status status, List<String> owners) {
        // Constructor for TableWriteItems takes the name of the table, which I have stored in TABLE_USER
        TableWriteItems items = new TableWriteItems("Feed");

        // Add each user into the TableWriteItems object
        for (String ownerAlias : owners) {
            Item item = new Item()
                    .withPrimaryKey("owner_alias", ownerAlias, "timeStamp", status.getDate())
                    .withString("author_alias", status.getUser().getAlias())
                    .withString("post", status.getPost())
                    .withList("mentions", status.getMentions())
                    .withList("links", status.getUrls());

            items.addItemToPut(item);

            // 25 is the maximum number of items allowed in a single batch write.
            // Attempting to write more than 25 items will result in an exception being thrown
            if (items.getItemsToPut() != null && items.getItemsToPut().size() == 25) {
                loopBatchWrite(items);
                items = new TableWriteItems("Feed");
            }
        }

        // Write any leftover items
        if (items.getItemsToPut() != null && items.getItemsToPut().size() > 0) {
            loopBatchWrite(items);
        }
    }

    private void loopBatchWrite(TableWriteItems items) {
        // The 'dynamoDB' object is of type DynamoDB and is declared statically in this example
        BatchWriteItemOutcome outcome = dynamoDB.batchWriteItem(items);

        Logger logger = Logger.getLogger(FollowDAO.class.getName());
        logger.log(Level.INFO, "Updated Feed Batch");

        // Check the outcome for items that didn't make it onto the table
        // If any were not added to the table, try again to write the batch
        while (outcome.getUnprocessedItems().size() > 0) {
            Map<String, List<WriteRequest>> unprocessedItems = outcome.getUnprocessedItems();
            outcome = dynamoDB.batchWriteItemUnprocessed(unprocessedItems);
            logger.log(Level.INFO, "Updated more Feeds");
        }
    }
}
