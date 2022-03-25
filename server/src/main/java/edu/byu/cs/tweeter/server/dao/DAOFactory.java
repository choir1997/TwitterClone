package edu.byu.cs.tweeter.server.dao;

public interface DAOFactory {
    IFollowDAO createFollowDAO();
    IStatusDAO createStatusDAO();
    IUserDAO createUserDAO();
    IAuthTokenDAO createAuthTokenDAO();
}
