package com.cloudbase.vdi;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class AppServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        String token = request.getHeader("X-Auth-Token");
        if (token == null) {
            response.sendError(401);
            return;
        }
        String command = request.getPathInfo();
        if (command.equals("/pools")) {
            String vdiUrl = Openstack.getVdiEndpoint(Config.getAuthUrl(), token);
            if (vdiUrl == null) {
                System.out.println("Error, vdibroker endpoint could not be retrieved");
                response.sendError(500);
                return;
            }
            ClientResponse queryResponse = Client.create()
                                                 .resource(vdiUrl + "/applications")
                                                 .header("X-Auth-Token", token)
                                                 .type("application/json")
                                                 .get(ClientResponse.class);
            JSONObject jsonApplication = new JSONObject(queryResponse.getEntity(String.class));
            JSONArray applications = jsonApplication.getJSONArray("applications");
            JSONArray appsDescriptions = new JSONArray();
            for (int i = 0; i < applications.length(); i++) {
                JSONObject app = applications.getJSONObject(i);
                JSONObject description = new JSONObject();
                description.put("id", app.getString("id"));
                description.put("name", app.getString("name"));
                description.put("description", app.getString("description"));
                appsDescriptions.put(description);
            }
            jsonResponse.put("pools", appsDescriptions);
            out.println(jsonResponse);
            return;
        } else if (command.equals("/session")) {
            String pool_id = request.getHeader("Pool-Id");
            if (pool_id != null) {
                String vdiUrl = Openstack.getVdiEndpoint(Config.getAuthUrl(), token);
                ClientResponse queryResponse = Client.create()
                                                     .resource(vdiUrl + "/applications/" + pool_id + "/sessions")
                                                     .header("X-Auth-Token", token)
                                                     .type("application/json")
                                                     .get(ClientResponse.class);
                JSONObject jsonSessionReply = new JSONObject(queryResponse.getEntity(String.class));
                JSONArray responseArray = new JSONArray();
                if (jsonSessionReply.has("sessions")) {
                    JSONArray array = jsonSessionReply.getJSONArray("sessions");
                    for (int i = 0; i < array.length(); i++) {
                        //add each session id
                        responseArray.put(array.getJSONObject(i).getString("id"));
                    }
                    jsonResponse.put("sessions", responseArray);
                    out.println(jsonResponse);
                    return;
                } else {
                    response.sendError(449);
                    return;
                }
            } else {
                response.sendError(400);
                return;
            }
        }
        response.sendError(404);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String body = Utility.getBody(request);
        JSONObject json = null;
        if (!body.isEmpty()) {
            json = new JSONObject(body);
        }
        JSONObject jsonResponse = new JSONObject();
        String token = request.getHeader("X-Auth-Token");
        if (token == null) {
            response.sendError(401);
            return;
        }

        String command = request.getPathInfo();
        if (command.equals("/session")) {
            String pool_id = request.getHeader("Pool-Id");
            if (pool_id != null) {
                String vdiUrl = Openstack.getVdiEndpoint(Config.getAuthUrl(), token);
                if (vdiUrl == null) {
                    System.out.println("Error, vdibroker endpoint could not be retrieved");
                    response.sendError(500);
                    return;
                }
                ClientResponse queryResponse = Client.create()
                                                     .resource(vdiUrl + "/applications/" + pool_id + "/sessions")
                                                     .header("X-Auth-Token", token)
                                                     .type("application/json")
                                                     .post(ClientResponse.class);
                JSONObject jsonSessionReply = new JSONObject(queryResponse.getEntity(String.class));
                if (jsonSessionReply.has("session")) {
                    jsonResponse.put("session_id", jsonSessionReply.getJSONObject("session").getString("id"));
                    response.setHeader("Content-Type", "application/json");
                    out.println(jsonResponse);
                    return;
                } else {
                    response.sendError(449);
                    return;
                }
            } else {
                response.sendError(449);
                return;
            }
        } /* else if (command.equals("/desktop")) {
              UserSessions sessions = UserSessionStorage.getUser(Sessions.getSession(sessionId).username);
              JSONArray desktopArray = new JSONArray();
              ArrayList<DesktopSession> desktops = sessions.getSessions();
              for (DesktopSession desk : desktops) {
                  desktopArray.put(desk.getJSON());
              }
              jsonResponse.put("desktops", desktopArray);
           } else if (command.equals("/set")) {
              String desktopSessionId = json.getString("desktop_session_id");
              ArrayList<DesktopSession> desktops = UserSessionStorage.getUser(Sessions.getSession(sessionId).username)
                                                                     .getSessions();
              for (DesktopSession desk : desktops) {
                  if (desk.id.equals(desktopSessionId)) {
                      VDIGuacamoleTunnelServlet.desktop = desk;
                      System.out.println("the desktop that will be used: " + desk.getJSON());
                  }
              }
           }*/
        response.sendError(404);
    }
}
