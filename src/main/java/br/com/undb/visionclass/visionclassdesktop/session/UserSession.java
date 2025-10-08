package br.com.undb.visionclass.visionclassdesktop.session;

import br.com.undb.visionclass.visionclassdesktop.model.User;

public class UserSession {

    private static UserSession instance;
    private User loggedInUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void clearSession() {
        loggedInUser = null;
    }
}