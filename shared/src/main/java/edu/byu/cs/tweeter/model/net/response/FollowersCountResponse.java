package edu.byu.cs.tweeter.model.net.response;

import java.util.Objects;

public class FollowersCountResponse extends Response {
    private int followersCount;

    //fail
    public FollowersCountResponse(String message) {
        super(false, message);
    }

    //success
    public FollowersCountResponse(int count) {
        super(true, null);
        this.followersCount = count;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowersCountResponse)) return false;
        FollowersCountResponse that = (FollowersCountResponse) o;
        return followersCount == that.followersCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(followersCount);
    }
}
