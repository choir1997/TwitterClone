package edu.byu.cs.tweeter.client.model.service;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.BackgroundTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.BackgroundTaskUtils;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.BackgroundTaskHandler;
import edu.byu.cs.tweeter.client.model.service.observer.LoginRegisterObserver;
import edu.byu.cs.tweeter.client.model.service.observer.MainObserver;
import edu.byu.cs.tweeter.client.model.service.observer.ServiceObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;

/**
 * Contains the business logic to support the login operation.
 */
public class UserService {
    private static final String URL_PATH = "/login";

    private ServerFacade serverFacade;

    public interface GetUserObserver extends ServiceObserver {
        void handleSuccess(User user);
    }

    public void getUser(AuthToken currUserAuthToken, String userAlias, GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(currUserAuthToken, userAlias, new GetUserHandler(getUserObserver));
        BackgroundTaskUtils.runTask(getUserTask);
    }

    private class GetUserHandler extends BackgroundTaskHandler<GetUserObserver> {
        public GetUserHandler(GetUserObserver observer) {
            super(observer);
        }

        @Override
        protected void handleSuccessMessage(GetUserObserver observer, Bundle data) {
            User user = (User) data.getSerializable(GetUserTask.USER_KEY);
            observer.handleSuccess(user);
        }
    }

    //Login
    public void runLogin(String alias, String password, LoginRegisterObserver getLoginObserver) {
        LoginTask loginTask = getLoginTask(alias, password, getLoginObserver);
        BackgroundTaskUtils.runTask(loginTask);
    }

    private static class LoginHandler extends BackgroundTaskHandler<LoginRegisterObserver>{
        public LoginHandler(LoginRegisterObserver observer) {
            super(observer);
        }
        @Override
        protected void handleSuccessMessage(LoginRegisterObserver observer, Bundle data) {
            User loggedInUser = (User) data.getSerializable(LoginTask.USER_KEY);
            AuthToken authToken = (AuthToken) data.getSerializable(LoginTask.AUTH_TOKEN_KEY);
            observer.handleSuccess(loggedInUser, authToken);
        }
    }

    //Register
    public void runRegister(String firstName, String lastName, String alias, String password, String imageBytesBase64, LoginRegisterObserver getRegisterObserver) {
        RegisterTask registerTask = getRegisterTask(firstName, lastName,
                alias, password, imageBytesBase64, getRegisterObserver);
        BackgroundTaskUtils.runTask(registerTask);
    }

    private static class RegisterHandler extends BackgroundTaskHandler<LoginRegisterObserver> {
        public RegisterHandler(LoginRegisterObserver observer) {
            super(observer);
        }
        @Override
        protected void handleSuccessMessage(LoginRegisterObserver observer, Bundle data) {
            User registeredUser = (User) data.getSerializable(RegisterTask.USER_KEY);
            AuthToken authToken = (AuthToken) data.getSerializable(RegisterTask.AUTH_TOKEN_KEY);
            observer.handleSuccess(registeredUser, authToken);
        }
    }

    //Logout
    public void runLogout(AuthToken currUserAuthToken, MainObserver getLogoutObserver) {
        LogoutTask logoutTask = new LogoutTask(currUserAuthToken, new LogoutHandler(getLogoutObserver));
        BackgroundTaskUtils.runTask(logoutTask);
    }

    private static class LogoutHandler extends BackgroundTaskHandler<MainObserver> {
        public LogoutHandler(MainObserver observer) {
            super(observer);
        }
        @Override
        protected void handleSuccessMessage(MainObserver observer, Bundle data) {
            observer.handleSuccess();
        }
    }

    /**
     * Creates an instance.
     *
     */
     public UserService() {
     }

    /**
     * Returns an instance of {@link ServerFacadeTests}. Allows mocking of the ServerFacade class for
     * testing purposes. All usages of ServerFacade should get their instance from this method to
     * allow for proper mocking.
     *
     * @return the instance.
     */
    ServerFacade getServerFacade() {
        if(serverFacade == null) {
            serverFacade = new ServerFacade();
        }

        return serverFacade;
    }


    /**
     * Returns an instance of {@link LoginTask}. Allows mocking of the LoginTask class for
     * testing purposes. All usages of LoginTask should get their instance from this method to
     * allow for proper mocking.
     *
     * @return the instance.
     */
    LoginTask getLoginTask(String username, String password, LoginRegisterObserver observer) {
        return new LoginTask(username, password, new LoginHandler(observer));
    }

    RegisterTask getRegisterTask(String firstName, String lastName, String alias, String password,
                                 String imageBytesBase64, LoginRegisterObserver observer) {
        return new RegisterTask(firstName, lastName, alias, password, imageBytesBase64, new RegisterHandler(observer));
    }


    /**
     * Background task that logs in a user (i.e., starts a session).
     */
    private class LoginTask extends BackgroundTask {

        private static final String LOG_TAG = "LoginTask";

        public static final String USER_KEY = "user";
        public static final String AUTH_TOKEN_KEY = "auth-token";

        /**
         * The user's username (or "alias" or "handle"). E.g., "@susan".
         */
        private String username;
        /**
         * The user's password.
         */
        private String password;

        /**
         * The logged-in user returned by the server.
         */
        protected User user;

        /**
         * The auth token returned by the server.
         */
        protected AuthToken authToken;

        public LoginTask(String username, String password, Handler messageHandler) {
            super(messageHandler);

            this.username = username;
            this.password = password;
        }

        @Override
        protected void runTask() {
            try {
                LoginRequest request = new LoginRequest(username, password);
                LoginResponse response = getServerFacade().login(request, URL_PATH);

                if(response.isSuccess()) {
                    this.user = response.getUser();
                    this.authToken = response.getAuthToken();
                    sendSuccessMessage();
                }
                else {
                    sendFailedMessage(response.getMessage());
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.getMessage(), ex);
                sendExceptionMessage(ex);
            }
        }

        protected void loadSuccessBundle(Bundle msgBundle) {
            msgBundle.putSerializable(USER_KEY, this.user);
            msgBundle.putSerializable(AUTH_TOKEN_KEY, this.authToken);
        }
    }
}
