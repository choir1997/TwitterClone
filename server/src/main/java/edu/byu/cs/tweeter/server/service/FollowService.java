package edu.byu.cs.tweeter.server.service;

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
import edu.byu.cs.tweeter.server.dao.DAOFactory;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.IAuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {
    IFollowDAO followDAO;
    IAuthTokenDAO authTokenDAO;

    public FollowService(DAOFactory factory) {
        followDAO = factory.createFollowDAO();
        authTokenDAO = factory.createAuthTokenDAO();
    }

    //Get Followees
    public FollowingResponse getFollowees(FollowingRequest request) {
        if(request.getFollowerAlias() == null) {
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
        return followDAO.getFollowees(request);
    }

    //Get Followers
    public FollowersResponse getFollowers(FollowersRequest request) {
        if(request.getFolloweeAlias() == null) {
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
        return followDAO.getFollowers(request);
    }

    public FollowersCountResponse getFollowersCount(FollowersCountRequest request) {
        if(request.getFolloweeAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee alias");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }
        return followDAO.getFollowersCount(request);
    }

    public FollowingCountResponse getFollowingCount(FollowingCountRequest request) {
        if(request.getFollowerAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }
        return followDAO.getFolloweeCount(request);
    }

    public FollowResponse follow(FollowRequest request) {
        if(request.getFolloweeAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }
        return followDAO.follow(request);
    }

    public UnfollowResponse unfollow(UnfollowRequest request) {
        if(request.getFolloweeAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }
        return followDAO.unfollow(request);
    }

    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        if (request.getFolloweeAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee alias");
        }

        else if (request.getFollowerAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        }

        else if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else if (!authTokenDAO.isValidToken(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }

        return followDAO.isFollower(request);
    }
}
