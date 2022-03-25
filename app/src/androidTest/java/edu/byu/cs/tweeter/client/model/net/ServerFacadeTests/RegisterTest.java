package edu.byu.cs.tweeter.client.model.net.ServerFacadeTests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.TweeterRemoteException;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;

public class RegisterTest {

    private ServerFacade serverFacadeSpy;
    private RegisterRequest registerRequest;
    private final String URL_PATH = "/register";

    @Before
    public void setup() {
        serverFacadeSpy = Mockito.spy(new ServerFacade());
        registerRequest = new RegisterRequest("allen", "sdf", "111", "123", "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png");
    }

    @Test
    public void registerPassTest() throws IOException, TweeterRemoteException {
        RegisterResponse registerResponse = serverFacadeSpy.register(registerRequest, URL_PATH);
        Assert.assertTrue(registerResponse.isSuccess());
        Assert.assertEquals("Allen", registerResponse.getUser().getFirstName());
    }

    @Test
    public void registerFailTest() throws IOException, TweeterRemoteException {
        registerRequest = new RegisterRequest(null, "sdf", "111", "123", "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png");

        try
        {
            serverFacadeSpy.register(registerRequest, URL_PATH);
            Assert.fail("threw exception");
        }
        catch(RuntimeException e)
        {
            //failed successfully
        }

    }



}
