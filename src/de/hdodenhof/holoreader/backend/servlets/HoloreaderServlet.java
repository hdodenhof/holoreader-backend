package de.hdodenhof.holoreader.backend.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.api.client.http.GenericUrl;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import de.hdodenhof.holoreader.backend.exception.GCMException;
import de.hdodenhof.holoreader.backend.exception.InvalidFeedException;
import de.hdodenhof.holoreader.backend.gcm.GCMService;
import de.hdodenhof.holoreader.backend.parser.FeedValidator;
import de.hdodenhof.holoreader.backend.persistence.UserEntity;
import de.hdodenhof.holoreader.backend.persistence.UserEntityService;

public class HoloreaderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            GenericUrl url = new GenericUrl(request.getRequestURL().toString());
            url.setRawPath("/");
            String redirectAfterLogout = url.build();

            String eMail = userService.getCurrentUser().getEmail();
            de.hdodenhof.holoreader.backend.persistence.UserEntityService us = new de.hdodenhof.holoreader.backend.persistence.UserEntityService();
            UserEntity user = us.get(eMail);

            request.setAttribute("loggedIn", true);
            request.setAttribute("name", request.getUserPrincipal().getName());
            request.setAttribute("logoutLink", userService.createLogoutURL(redirectAfterLogout));
            request.setAttribute("devicePresent", user != null);
        } else {
            String loginUrl = userService.createLoginURL("/");

            request.setAttribute("loggedIn", false);
            request.setAttribute("loginLink", loginUrl);
        }
        RequestDispatcher rd = request.getRequestDispatcher("WEB-INF/jsp/holoreader.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            // TODO use JSON
            String[] feeds = request.getParameterValues("feeds[]");
            if (feeds != null) {
                HashMap<String, Boolean> resultMap = new HashMap<String, Boolean>();

                StringBuilder json = new StringBuilder();
                json.append("[");
                for (int i = 0; i < feeds.length; i++) {
                    if (feeds[i].length() > 0) {
                        // TODO maxlength?
                        String url = feeds[i];
                        if (!resultMap.containsKey(url)) {
                            try {
                                String title = FeedValidator.validateFeedAndGetTitle(feeds[i]);
                                json.append("{");
                                json.append("\"title\":\"" + title + "\"");
                                json.append(",");
                                json.append("\"url\":\"" + url + "\"");
                                json.append("}");
                                json.append(",");
                                resultMap.put(title, true);
                            } catch (InvalidFeedException e) {
                                resultMap.put(url, false);
                            }
                        }
                    }
                }
                if (json.length() > 1) {
                    json.setLength(json.length() - 1);
                }
                json.append("]");

                String eMail = userService.getCurrentUser().getEmail();

                UserEntityService us = new UserEntityService();
                UserEntity user = us.get(eMail);

                GCMService gcmService = new GCMService();
                String regId = user.getRegId();

                try {
                    gcmService.sendMessage(regId, json.toString());
                    try {
                        JSONObject responseEntity = new JSONObject();
                        for (Map.Entry<String, Boolean> entry : resultMap.entrySet()) {
                            responseEntity.put(entry.getKey(), entry.getValue());
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("application/json");
                        response.getWriter().print(responseEntity);
                    } catch (JSONException e) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (GCMException e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void extractAndParseMultipart(HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                ServletFileUpload upload = new ServletFileUpload();

                FileItemIterator iterator = upload.getItemIterator(request);

                String json = "";

                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();

                    if (!item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fileName = item.getName();
                        String contentType = item.getContentType();

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = null;

                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line + "\n");
                        }

                        bufferedReader.close();
                        json = stringBuilder.toString();
                        break;
                    }
                }

                UserService userService = UserServiceFactory.getUserService();
                String eMail = userService.getCurrentUser().getEmail();

                UserEntityService us = new UserEntityService();
                UserEntity user = us.get(eMail);

                // GCMService gcmService = new GCMService();
                // gcmService.sendMessage(user.getRegId(), json);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
