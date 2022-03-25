package edu.byu.cs.tweeter.model.net.response;

import edu.byu.cs.tweeter.model.domain.User;

public class FollowResponse extends Response {

    public FollowResponse(String message) {
        super(false, message);
    }

    public FollowResponse() {
        super(true, null);
    }

}
