package com.cloudbase.vdi;

import org.json.JSONObject;

public class DesktopSession {
    public String app_id;
    public String host;
    public String id;
    public String password;
    public String port;
    public String username;

    public DesktopSession(String username, String password, String host, String port, String id, String app_id) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.id = id;
        this.app_id = app_id;
    }

    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        json.put("host", host);
        json.put("port", port);
        json.put("id", id);
        json.put("app_id", app_id);
        return json;
    }
}
