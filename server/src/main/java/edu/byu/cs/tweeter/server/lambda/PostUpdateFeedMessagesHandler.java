package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.server.dao.FollowDAO;

public class PostUpdateFeedMessagesHandler implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        FollowDAO followDao = new FollowDAO();
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            Status status = (Status) new Gson().fromJson(msg.getBody(), Status.class);

            ItemCollection<QueryOutcome> items = followDao.getAllFollowers(status.getUser().getAlias());

            Iterator<Item> iterator = items.iterator();

            List<String> followers = new ArrayList<>();

            int counter = 0;

            Item item = null;
            while (iterator.hasNext()) {
                item = iterator.next();
                String followerAlias = item.getString("follower");

                followers.add(followerAlias);
                counter++;

                if (counter == 500) {
                    counter = 0;

                    //need to make one object containing followers and status
                    MessageBody messageObject = new MessageBody(status, followers);
                    String messageBody = new Gson().toJson(messageObject);

                    String queueUrl = "https://sqs.us-east-1.amazonaws.com/386643631519/UpdateFeedQueue";

                    SendMessageRequest send_msg_request = new SendMessageRequest()
                            .withQueueUrl(queueUrl)
                            .withMessageBody(messageBody);

                    AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
                    SendMessageResult send_msg_result = sqs.sendMessage(send_msg_request);

                    String msgId = send_msg_result.getMessageId();
                    System.out.println("Message ID: " + msgId);

                    followers = new ArrayList<>();
                }
            }
        }
        return null;
    }


}
