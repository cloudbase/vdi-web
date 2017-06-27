package com.cloudbase.vdi;

import java.util.ArrayList;
import java.util.HashMap;

public class UserSessions {
    public HashMap<String, DesktopSession> sessions;
    public String username;

    public UserSessions(String username) {
        this.username = username;
        sessions = new HashMap<String, DesktopSession>();
    }

    public void addSession(String id, DesktopSession session) {
        sessions.put(id, session);
    }

    public DesktopSession getSession(String id) {
        return sessions.get(id);
    }

    public ArrayList<DesktopSession> getSessions() {
        return new ArrayList<DesktopSession>(sessions.values());
    }
}
