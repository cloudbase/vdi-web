package com.cloudbase.vdi;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class AuthServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        String command = request.getPathInfo();
        if (command.equals("/logout")) {
            // is there any token present
            String token = request.getHeader("X-Auth-Token");
            if (token != null) {
                Openstack.revokeToken(Config.getAuthUrl(), token);
                return;
            } else {
                System.out.println("Missing X-Auth-Token");
                out.println("Missing X-Auth-Token");
                response.setHeader("Content-Type", "text/plain");
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
        JSONObject json = new JSONObject(body);
        JSONObject jsonResponse = new JSONObject();
        String command = request.getPathInfo();
        // do login
        // needs a json body that contains
        // username, password pair
        // token from openstack
        if (command.equals("/login")) {
            String token;
            // is there a username/password present
            if (json.has("username") && json.has("password")) {
                String project = Config.getProjectName();
                if(json.has("project")){
                    project = json.getString("project");
                }
                token = Openstack.getToken(Config.getAuthUrl(),
                                           json.getString("username"),
                                           json.getString("password"),
                                           project,
                                           Config.getUserDomainName(),
                                           Config.getProjectDomainName());
                if(token == null){
                    out.println("Invalid Credentials");
                    response.setHeader("Content-Type", "text/plain");
                    response.sendError(400);
                    return;
                }
            } else {
                System.out.println("Invalid JSON");
                out.println("Invalid JSON");
                response.setHeader("Content-Type", "text/plain");
                response.sendError(400);
                return;
            }
            response.setHeader("X-Subject-Token", token);
            return;
        }
        response.sendError(404);
    }

    @Override
    public void init() {

    }

}
