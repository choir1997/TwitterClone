package edu.byu.cs.tweeter.server.dao;

public class DynamoDBDAOFactory implements DAOFactory {
    @Override
    public IFollowDAO createFollowDAO() {
        return new FollowDAO();
    }

    @Override
    public IStatusDAO createStatusDAO() {
        return new StatusDAO();
    }

    @Override
    public IUserDAO createUserDAO() {
        return new UserDAO();
    }

    @Override
    public IAuthTokenDAO createAuthTokenDAO() {
        return new AuthTokenDAO();
    }
}
