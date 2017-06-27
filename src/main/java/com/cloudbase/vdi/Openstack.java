package com.cloudbase.vdi;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Openstack {
    private static String cachedVdiEndpoint = null;

    public static boolean checkToken(String authUrl, String token) {
        ClientResponse response = Client.create()
                                        .resource(authUrl + "/auth/tokens")
                                        .header("X-Auth-Token", token)
                                        .header("X-Subject-Token", token)
                                        .type("application/json")
                                        .head();

        if (response.getStatus() != 200) {
            System.out.println("Token is not valid");
            return false;
        }
        return true;
    }

    public static String getToken(String authUrl,
                                  String username,
                                  String password,
                                  String projectName,
                                  String userDomainName,
                                  String projectDomainName) {
        // construct the request JSON
        JSONObject user = new JSONObject();
        user.put("name", username);
        JSONObject domainUser = new JSONObject();
        domainUser.put("name", userDomainName);
        user.put("domain", domainUser);
        user.put("password", password);
        JSONObject pass = new JSONObject();
        pass.put("user", user);
        JSONObject identity = new JSONObject();
        identity.put("password", pass);
        JSONArray methods = new JSONArray();
        methods.put("password");
        identity.put("methods", methods);
        JSONObject auth = new JSONObject();
        auth.put("identity", identity);
        // is the auth scoped?
        if (projectName != null && userDomainName != null && projectDomainName != null) {
            JSONObject domain = new JSONObject();
            domain.put("name", projectDomainName);
            JSONObject project = new JSONObject();
            project.put("domain", domain);
            project.put("name", projectName);
            JSONObject scope = new JSONObject();
            scope.put("project", project);
            auth.put("scope", scope);
        }

        JSONObject json = new JSONObject();
        json.put("auth", auth);
        String jsonString = json.toString();
        ClientResponse response = Client.create()
                                        .resource(authUrl + "/auth/tokens")
                                        .type("application/json")
                                        .post(ClientResponse.class, jsonString);

        if (response.getStatus() != 201) {
            System.out.println("Error " + response.getStatus() + " when creating token for username:" + username);
            return null;
        }
        String token = response.getHeaders().getFirst("X-Subject-Token");
        System.out.println("Successful authentication for:\n" + "User:" + username + "; " + "Token:" + token);
        return token;
    }

    public static String getVdiEndpoint(String authUrl, String token) {
        if (Openstack.cachedVdiEndpoint != null) {
            return Openstack.cachedVdiEndpoint;
        }
        ClientResponse response = Client.create()
                                        .resource(authUrl + "/auth/catalog")
                                        .header("X-Auth-Token", token)
                                        .type("application/json")
                                        .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            System.out.println("Error " + response.getStatus() + " when getting the catalog for token:" + token);
            return null;
        }

        String vdiUrl = null;
        JSONObject json = new JSONObject(response.getEntity(String.class));
        JSONArray catalog = json.getJSONArray("catalog");
        for (int i = 0; i < catalog.length(); i++) {
            JSONArray endpoints = catalog.getJSONObject(i).getJSONArray("endpoints");
            if (catalog.getJSONObject(i).getString("type").equals("vdibroker"))
                for (int j = 0; j < endpoints.length(); j++) {
                    if (endpoints.getJSONObject(j).getString("interface").equals("public")) {
                        vdiUrl = endpoints.getJSONObject(j).getString("url");
                    }
                }
        }
        Openstack.cachedVdiEndpoint = vdiUrl;
        return vdiUrl;
    }

    public static void revokeToken(String authUrl, String token) {
        ClientResponse response = Client.create()
                                        .resource(authUrl + "/auth/tokens")
                                        .header("X-Auth-Token", token)
                                        .header("X-Subject-Token", token)
                                        .type("application/json")
                                        .delete(ClientResponse.class);

        if (response.getStatus() != 204) {
            System.out.println("Error " + response.getStatus() + " when deleting token: " + token);
            return;
        }
        System.out.println("Deleted token " + token);
    }
}
