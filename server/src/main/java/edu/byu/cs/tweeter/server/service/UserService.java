package edu.byu.cs.tweeter.server.service;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.request.UserRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;
import edu.byu.cs.tweeter.model.net.response.UserResponse;
import edu.byu.cs.tweeter.server.dao.DAOFactory;
import edu.byu.cs.tweeter.server.dao.DynamoDBDAOFactory;
import edu.byu.cs.tweeter.server.dao.IAuthTokenDAO;
import edu.byu.cs.tweeter.server.dao.IUserDAO;
import edu.byu.cs.tweeter.server.dao.StatusDAO;
import edu.byu.cs.tweeter.server.dao.UserDAO;
import edu.byu.cs.tweeter.util.FakeData;

public class UserService {
    IUserDAO userDAO;
    IAuthTokenDAO authTokenDAO;

    public UserService(DAOFactory factory) {
        userDAO = factory.createUserDAO();
        authTokenDAO = factory.createAuthTokenDAO();
    }

    public LoginResponse login(LoginRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }
        return userDAO.login(request);
    }

    public RegisterResponse register(RegisterRequest request) {
        if (request.getFirstName() == null) {
            throw new RuntimeException(("[BadRequest] Missing a first name"));
        }
        else if (request.getLastName() == null) {
            throw new RuntimeException("[BadRequest] Missing a last name");
        }
        else if(request.getUsername() == null){
            throw new RuntimeException("[BadRequest] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[BadRequest] Missing a password");
        }

        return userDAO.register(request);
    }

    public UserResponse getUser(UserRequest request) {
        if(request.getUserAlias() == null){
            throw new RuntimeException("[BadRequest] Missing an alias");
        }

        //need to check authToken
        if (authTokenDAO.isValidToken(request.getAuthToken())) {
            //if authToken is valid, update current authToken with new timestamp
            authTokenDAO.updateAuthToken(request.getAuthToken().getToken());
        }

        else {
            throw new RuntimeException("[BadRequest] Invalid authtoken");
        }

        return userDAO.getUser(request);
    }

    public LogoutResponse logout(LogoutRequest request) {
        //logout functionality here
        return userDAO.logout(request);
    }
}
