package edu.lehigh.cse216.tad222.backend;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

public class AuthCodeServlet extends AbstractAuthorizationCodeServlet {

    /**
     *
     */
    private static final long serialVersionUID = 2372819854088979956L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // redirect to google for authorization
        StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
                .append("?client_id=").append(request.getPart("client_id")) // the client id from the api console
                                                                           // registration
                .append("&response_type=code").append("&scope=openid%20email") // scope is the api permissions we are
                                                                               // requesting
                .append("&redirect_uri=" + Util.SITE + "/users/login/callback") // the servlet that google redirects to after
                                                               // authorization
                .append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
                .append("&access_type=offline") // here we are asking to access to user's data while they are not signed
                                                // in
                .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are
                                                   // already signed in

        response.sendRedirect(oauthUrl.toString());
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
        GenericUrl url = new GenericUrl(req.getRequestURL().toString());
        url.setRawPath(Util.SITE + "/messages");
        return url.build();
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws IOException {
        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), new NetHttpTransport(),
                new JacksonFactory(),
                //token server URL
                new GenericUrl(Util.SITE + "/messages"),
                new BasicAuthentication(Util.getClientId(), Util.getClientSecret()), Util.getClientId(),
                Util.SITE + "users/login").setCredentialDataStore(StoredCredential
                        .getDefaultDataStore(new FileDataStoreFactory(new File("datastoredir"))))
                .build();
    }

    @Override
    protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
        return req.getParameter("name");
    }
  }