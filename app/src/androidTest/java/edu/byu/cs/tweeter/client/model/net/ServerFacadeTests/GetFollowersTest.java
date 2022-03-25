package edu.byu.cs.tweeter.client.model.net.ServerFacadeTests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;

public class GetFollowersTest {
    private AuthToken authToken;
    private ServerFacade serverFacadeSpy;
    private FollowersRequest followersRequest;
    private final String URL_PATH = "/getfollowers";

    @Before
    public void setup() {
        serverFacadeSpy = Mockito.spy(new ServerFacade());
        authToken = new AuthToken();

    }

    @Test
    public void getFollowersPass() throws IOException, TweeterRemoteException {
        followersRequest = new FollowersRequest(authToken, "@allen", 25, "@ze");
        FollowersResponse followersResponse = serverFacadeSpy.getFollowers(followersRequest, URL_PATH);
        Assert.assertTrue(followersResponse.isSuccess());
        Assert.assertEquals(21, followersResponse.getFollowers().size());
    }

    @Test
    public void getFollowersFail() {

        followersRequest = new FollowersRequest(authToken, null, 20, "@amy");

        try
        {
            serverFacadeSpy.getFollowers(followersRequest, URL_PATH);
            Assert.fail("threw exception");
        }
        catch(RuntimeException | IOException | TweeterRemoteException e)
        {
            //failed successfully
        }
    }
}

