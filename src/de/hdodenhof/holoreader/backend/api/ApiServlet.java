package de.hdodenhof.holoreader.backend.api;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import de.hdodenhof.holoreader.backend.persistence.UserEntity;
import de.hdodenhof.holoreader.backend.persistence.UserService;

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

            UserEntity user = new UserEntity();
            user.seteMail(eMail);
            user.setRegId(regId);

            UserService userService = new UserService();
            userService.register(user);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
