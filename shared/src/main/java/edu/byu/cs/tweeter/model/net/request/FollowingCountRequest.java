package edu.byu.cs.tweeter.model.net.request;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public class FollowingCountRequest {
    private AuthToken authToken;
    private String followerAlias;

    private FollowingCountRequest() {}

    public FollowingCountRequest(AuthToken authToken, String userAlias) {
        this.authToken = authToken;
        this.followerAlias = userAlias;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getFollowerAlias() {
        return followerAlias;
    }

    public void setFollowerAlias(String userAlias) {
        this.followerAlias = userAlias;
    }
}
