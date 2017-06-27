package com.cloudbase.vdi;

import java.util.ArrayList;

public class UserSessionStorage {
    private static ArrayList<UserSessions> sessions = new ArrayList<UserSessions>();

    private static boolean contains(String username) {
        for (UserSessions s : sessions) {
            if (s.username.equals(username))
                return true;
        }
        return false;
    }

    private static UserSessions get(String username) {
        for (UserSessions s : sessions) {
            if (s.username.equals(username))
                return s;
        }
        return null;
    }

    public static UserSessions getUser(String username) {
        if (!contains(username)) {
            sessions.add(new UserSessions(username));
        }
        return get(username);
    }
}
