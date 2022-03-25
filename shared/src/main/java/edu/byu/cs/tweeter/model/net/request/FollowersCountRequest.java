package edu.byu.cs.tweeter.model.net.request;
import edu.byu.cs.tweeter.model.domain.AuthToken;

public class FollowersCountRequest {
    private AuthToken authToken;
    private String followeeAlias;

    private FollowersCountRequest() {}

    public FollowersCountRequest(AuthToken authToken, String userAlias) {
        this.authToken = authToken;
        this.followeeAlias = userAlias;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getFolloweeAlias() {
        return followeeAlias;
    }

    public void setFolloweeAlias(String userAlias) {
        this.followeeAlias = userAlias;
    }
}
