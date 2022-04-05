package edu.byu.cs.tweeter.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.DAOFactory;
import edu.byu.cs.tweeter.server.dao.FollowDAO;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;

public class Filler {

    private final static int NUM_USERS = 10000;

    // The alias of the user to be followed by each user created
    // This example code does not add the target user, that user must be added separately.
    private final static String FOLLOW_TARGET = "@choir1997";

    public static void fillDatabase(DAOFactory factory) throws IOException {

        // Get instance of DAOs by way of the Abstract Factory Pattern

        IUserDAO userDAO = factory.createUserDAO();
        IFollowDAO followDAO = factory.createFollowDAO();

        List<String> followers = new ArrayList<>();
        List<User> users = new ArrayList<>();

        // Iterate over the number of users you will create
        for (int i = 1; i <= NUM_USERS; i++) {
            String firstName = "follower" + i;
            String lastName = "lastName" + i;
            String alias = "@follower" + i;

            String female_url = "https://faculty.cs.byu.edu/~jwilkerson/cs340/tweeter/images/daisy_duck.png";
            byte[] imageBytes = ByteArrayUtils.bytesFromUrl(female_url);
            String imageBytesBase64 = Base64.getEncoder().encodeToString(imageBytes);

            User user = new User(firstName, lastName, alias, imageBytesBase64);

            users.add(user);

            followers.add(alias);

        }

        // Call the DAOs for the database logic
        if (users.size() > 0) {
            userDAO.addUserBatch(users);
        }
        if (followers.size() > 0) {
            followDAO.addFollowersBatch(followers, FOLLOW_TARGET);
            followDAO.updateFollowCount(FOLLOW_TARGET, NUM_USERS);
        }
    }
}