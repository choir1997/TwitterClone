package edu.byu.cs.tweeter.server.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.FollowRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.request.UnfollowRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.FollowResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.model.net.response.UnfollowResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;
import edu.byu.cs.tweeter.server.Calculations.UserCalculations;
import edu.byu.cs.tweeter.server.dao.DAOFactory;
import edu.byu.cs.tweeter.server.dao.DynamoDBDAOFactory;

public class RegisterTest {
    private RegisterRequest request;
    private RegisterResponse response;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private UserService userService;
    private FollowService followService;
    private UserRequest userRequest;
    private FollowRequest followRequest;
    private UnfollowRequest unfollowRequest;
    private UnfollowResponse unfollowResponse;
    private IsFollowerRequest isFollowerRequest;
    private IsFollowerResponse isFollowerResponse;
    private FollowingRequest followingRequest;
    private FollowingResponse followingResponse;
    private FollowersRequest followersRequest;
    private FollowersResponse followersResponse;
    private FollowResponse followResponse;
    private PostStatusRequest postStatusRequest;
    private PostStatusResponse postStatusResponse;
    private UserResponse userResponse;
    private StatusService statusService;
    private StoryRequest storyRequest;
    private StoryResponse storyResponse;
    private FeedRequest feedRequest;
    private FeedResponse feedResponse;

    @Before
    public void setup() throws IOException {
        String male_url = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/donald_duck.png";
        String female_url = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png";
        byte[] imageBytes = ByteArrayUtils.bytesFromUrl(male_url);
        String imageBytesBase64 = Base64.getEncoder().encodeToString(imageBytes);
        request = new RegisterRequest("Eren", "Yeager", "@eren", "123", imageBytesBase64);

        DAOFactory daoFactory = new DynamoDBDAOFactory();
        userService = new UserService(daoFactory);
        followService = new FollowService(daoFactory);
        statusService = new StatusService(daoFactory);
    }

    @Test
    public void registerPass() {
        response = userService.register(request);
        System.out.println("User " + response.getUser().getAlias());
    }

    @Test
    public void loginPass() {
        userService.register(request);
        loginRequest = new LoginRequest("@a", "1");
        loginResponse = userService.login(loginRequest);
    }

    @Test
    public void userPass() {
        loginRequest = new LoginRequest("@choir1997", "123");

        loginResponse = userService.login(loginRequest);
        userRequest = new UserRequest(loginResponse.getAuthToken(), "@amy");

        userResponse = userService.getUser(userRequest);
    }

    @Test
    public void followPass() {
        loginRequest = new LoginRequest("@c", "123");

        loginResponse = userService.login(loginRequest);
        followRequest = new FollowRequest(loginResponse.getAuthToken(), "@choir1997");

        followResponse = followService.follow(followRequest);
    }

    @Test
    public void unfollowPass() {
        loginRequest = new LoginRequest("@n", "1");

        loginResponse = userService.login(loginRequest);
        unfollowRequest = new UnfollowRequest(loginResponse.getAuthToken(), "@m");

        unfollowResponse = followService.unfollow(unfollowRequest);
    }

    @Test
    public void isFollowerPass() {
        loginRequest = new LoginRequest("@choir1997", "123");

        loginResponse = userService.login(loginRequest);
        isFollowerRequest = new IsFollowerRequest(loginResponse.getAuthToken(), "@choir1997", "@amy");

        isFollowerResponse = followService.isFollower(isFollowerRequest);
    }

    @Test
    public void getFolloweesPass() {
        loginRequest = new LoginRequest("@n", "1");

        loginResponse = userService.login(loginRequest);
        followingRequest = new FollowingRequest(loginResponse.getAuthToken(), loginResponse.getUser().getAlias(), 12, "@a");

        followingResponse = followService.getFollowees(followingRequest);
    }

    @Test
    public void getFollowerPass() {
        loginRequest = new LoginRequest("@a", "1");

        loginResponse = userService.login(loginRequest);
        followersRequest = new FollowersRequest(loginResponse.getAuthToken(), loginResponse.getUser().getAlias(), 5, null);
        followersResponse = followService.getFollowers(followersRequest);
    }

    @Test
    public void postStatusPass() throws ParseException {
        loginRequest = new LoginRequest("@zack", "123");
        loginResponse = userService.login(loginRequest);
        SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat statusFormat = new SimpleDateFormat("MMM d yyyy HH:mm aaa");

        String dateTime = statusFormat.format(userFormat.parse(LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8)));

        List<String> mentions = new ArrayList<>();
        List<String> links = new ArrayList<>();
        Status status = new Status("connect with my on linked: ", loginResponse.getUser(), UserCalculations.getTimeStamp(), links, mentions);

        postStatusRequest = new PostStatusRequest(loginResponse.getAuthToken(), status);

        postStatusResponse = statusService.postStatus(postStatusRequest);
    }

    @Test
    public void getStoryPass() {
        loginRequest = new LoginRequest("@a", "1");
        loginResponse = userService.login(loginRequest);

        storyRequest = new StoryRequest(loginResponse.getAuthToken(), "@a", 10, null);

        storyResponse = statusService.getStory(storyRequest);
    }

    @Test
    public void getFeedPass() {
        loginRequest = new LoginRequest("@amy", "123");
        loginResponse = userService.login(loginRequest);

        feedRequest = new FeedRequest(loginResponse.getAuthToken(), "@amy", 10, null);
        
        feedResponse = statusService.getFeed(feedRequest);
    }
}
