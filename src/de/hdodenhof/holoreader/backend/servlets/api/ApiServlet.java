package de.hdodenhof.holoreader.backend.servlets.api;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import de.hdodenhof.holoreader.backend.persistence.UserEntityService;

@Path("/")
public class ApiServlet {

    @Path("/register")
    @PUT
    public void register(String content) {
        JSONObject request;
        try {
            request = new JSONObject(content);
            String eMail = request.getString("eMail");
            String regId = request.getString("regId");

            UserEntityService userService = new UserEntityService();
            userService.register(eMail, regId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Path("/unregister")
    @PUT
    public void unregister(String content) {
        JSONObject request;
        try {
            request = new JSONObject(content);
            String regId = request.getString("regId");

            UserEntityService userService = new UserEntityService();
            userService.unregister(regId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
