package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.server.dao.DynamoDBDAOFactory;
import edu.byu.cs.tweeter.server.service.StatusService;

public class UpdateFeedHandler implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        StatusService statusService = new StatusService(new DynamoDBDAOFactory());
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            MessageBody messageObject = (MessageBody) new Gson().fromJson(msg.getBody(), MessageBody.class);
            Status status = messageObject.getStatus();
            List<String> followers = messageObject.getFollowers();

            List<String> tempFollowers = new ArrayList<>();
            int counter = 0;

            for (int i = 0; i < followers.size(); i++) {
                tempFollowers.add(followers.get(i));
                counter++;

                if (counter == 25) {
                    statusService.updateFeeds(status, tempFollowers);
                    tempFollowers = new ArrayList<>();
                    counter = 0;
                }
            }
        }
        return null;
    }
}
