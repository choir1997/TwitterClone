package edu.byu.cs.tweeter.model.net.response;

import java.util.Objects;

public class FollowingCountResponse extends Response {
    private int followingCount;

    //fail
    public FollowingCountResponse(String message) {
        super(false, message);
    }

    //success
    public FollowingCountResponse(int count) {
        super(true, null);
        this.followingCount = count;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowingCountResponse)) return false;
        FollowingCountResponse that = (FollowingCountResponse) o;
        return followingCount == that.followingCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(followingCount);
    }
}
