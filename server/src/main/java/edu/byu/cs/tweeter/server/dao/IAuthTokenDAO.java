package edu.byu.cs.tweeter.server.dao;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public interface IAuthTokenDAO {
    boolean isValidToken(AuthToken authToken);
    void updateAuthToken(String token);
    void deleteUserToken(String token);
}
