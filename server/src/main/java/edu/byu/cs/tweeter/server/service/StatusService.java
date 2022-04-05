package edu.byu.cs.tweeter.server.service;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.server.dao.DAOFactory;
import edu.byu.cs.tweeter.server.dao.DynamoDBDAOFactory;
import edu.byu.cs.tweeter.server.dao.IAuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.server.dao.StatusDAO;

public class StatusService {
    IStatusDAO statusDAO;
    IAuthTokenDAO authTokenDAO;
    IUserDAO userDAO;

    public StatusService(DAOFactory factory) {
        statusDAO = factory.createStatusDAO();
        authTokenDAO = factory.createAuthTokenDAO();
        userDAO = factory.createUserDAO();
    }

    public FeedResponse getFeed(FeedRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }

        return statusDAO.getFeed(request);
    }

    public StoryResponse getStory(StoryRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }

        return statusDAO.getStory(request);
    }

    public PostStatusResponse postStatus(PostStatusRequest request) {

        List<String> mentions = request.getStatus().getMentions();

        for (String mention : mentions) {
            if (userDAO.getUserFromTable(mention) == null) {
                throw new RuntimeException("[BadRequest] user doesn't exist");
            }
        }

        if (request.getStatus().getPost() == null) {
            throw new RuntimeException("[BadRequest] post cannot be null");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }



        return statusDAO.postStatus(request);
    }

    public void updateFeeds(Status status, List<String> owners) {
        statusDAO.updateFeedsBatch(status, owners);
    }
}
