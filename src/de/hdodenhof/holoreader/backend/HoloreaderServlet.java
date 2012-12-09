package de.hdodenhof.holoreader.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.http.GenericUrl;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import de.hdodenhof.holoreader.backend.persistence.UserEntity;

public class HoloreaderServlet extends AbstractAppEngineAuthorizationCodeServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        UserService userService = UserServiceFactory.getUserService();

        GenericUrl url = new GenericUrl(request.getRequestURL().toString());
        url.setRawPath("/");
        String redirectAfterLogiout = url.build();

        request.setAttribute("name", request.getUserPrincipal().getName());
        request.setAttribute("logoutLink", userService.createLogoutURL(redirectAfterLogiout));
        RequestDispatcher rd = request.getRequestDispatcher("WEB-INF/jsp/holoreader.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                ServletFileUpload upload = new ServletFileUpload();

                FileItemIterator iterator = upload.getItemIterator(request);

                response.setContentType("text/html");
                PrintWriter writer = response.getWriter();
                writer.println("<html><body>");

                String json = "";

                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();

                    if (!item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fileName = item.getName();
                        String contentType = item.getContentType();

                        writer.println(fieldName);
                        writer.println(fileName);
                        writer.println(contentType);

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

                de.hdodenhof.holoreader.backend.persistence.UserService us = new de.hdodenhof.holoreader.backend.persistence.UserService();
                UserEntity user = us.get(eMail);

                GCMService gcmService = new GCMService();
                gcmService.sendMessage(user.getRegId(), json);

                writer.println("</body></html>");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
        return Utils.getRedirectUri(req);
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
        return Utils.newFlow();
    }

}
