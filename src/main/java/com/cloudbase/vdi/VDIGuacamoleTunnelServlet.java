package com.cloudbase.vdi;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class VDIGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {
    public static DesktopSession desktop = null;

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
        JSONObject json = null;
        try {
            json = new JSONObject(Utility.getBody(request));
        } catch (JSONException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (json == null)
            return null;
        if (!json.has("token") || !json.has("session_id")) {
            return null;
        }
        String token = json.getString("token");
        String pool_id = json.getString("pool_id");
        String session_id = json.getString("session_id");

        String vdiUrl = Openstack.getVdiEndpoint(Config.getAuthUrl(), token);
        ClientResponse queryResponse = Client.create()
                                             .resource(vdiUrl + "/applications/" + pool_id + "/sessions/" + session_id)
                                             .header("X-Auth-Token", token)
                                             .type("application/json")
                                             .get(ClientResponse.class);

        JSONObject session = new JSONObject(queryResponse.getEntity(String.class));
        JSONObject connection_data = session.getJSONObject("session").getJSONObject("connection_data");

        String host = connection_data.getString("host");
        String port = Integer.toString(connection_data.getInt("port"));
        String username = connection_data.getString("username");
        String password = connection_data.getString("password");

        // Create our configuration
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol("rdp");
        config.setParameter("hostname", host);
        config.setParameter("port", port);
        config.setParameter("username", username);
        config.setParameter("password", password);
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "nla");

        // Connect to guacd - everything is hard-coded here.
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(new InetGuacamoleSocket("localhost", 4822), config);

        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}
