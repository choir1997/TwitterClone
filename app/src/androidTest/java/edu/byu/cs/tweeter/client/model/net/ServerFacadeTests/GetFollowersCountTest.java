package edu.byu.cs.tweeter.client.model.net.ServerFacadeTests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;

public class GetFollowersCountTest {
    private ServerFacade serverFacadeSpy;
    private FollowersCountRequest followersCountRequest;
    private AuthToken authToken;
    private final String URL_PATH = "/getfollowerscount";

    @Before
    public void setup() {
        serverFacadeSpy = Mockito.spy(new ServerFacade());
        authToken = new AuthToken();

    }

    @Test
    public void getFollowersCountPass() throws IOException, TweeterRemoteException {

        followersCountRequest = new FollowersCountRequest(authToken, "@allen");
        FollowersCountResponse followersCountResponse = serverFacadeSpy.getFollowersCount(followersCountRequest, URL_PATH);

        Assert.assertTrue(followersCountResponse.isSuccess());
        Assert.assertEquals(21, followersCountResponse.getFollowersCount());
    }

    @Test
    public void getFollowersCountFail() throws IOException, TweeterRemoteException {
        followersCountRequest = new FollowersCountRequest(authToken, null);

        try
        {
            serverFacadeSpy.getFollowersCount(followersCountRequest, URL_PATH);
            Assert.fail("threw exception");
        }
        catch(RuntimeException e)
        {
            //failed successfully
        }

    }
}
