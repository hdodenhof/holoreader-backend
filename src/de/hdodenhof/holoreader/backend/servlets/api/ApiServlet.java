package de.hdodenhof.holoreader.backend.servlets.api;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import de.hdodenhof.holoreader.backend.persistence.services.UserAndDeviceService;

@Path("/")
public class ApiServlet {

    @Path("/register")
    @PUT
    public void register(String content) {
        JSONObject request;
        try {
            request = new JSONObject(content);
            String eMail = request.getString("eMail");
            String model = request.getString("device");
            String regId = request.getString("regId");

            UserAndDeviceService userService = new UserAndDeviceService();
            userService.register(eMail, model, regId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
