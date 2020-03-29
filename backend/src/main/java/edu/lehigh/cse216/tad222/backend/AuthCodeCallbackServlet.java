package edu.lehigh.cse216.tad222.backend;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

public class AuthCodeCallbackServlet extends AbstractAuthorizationCodeCallbackServlet {

    /**
     *
     */
    private static final long serialVersionUID = -2663253438853631359L; 

    @Override
    protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
            throws ServletException, IOException {
        
        // req.getSession().setAttribute("access_token", accessToken);
        
        //.App.getDatabase().setUserActive(response.get, uid, secret);
        resp.sendRedirect(Util.SITE + "/messages");
    }

    @Override
    protected void onError(HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
            throws ServletException, IOException {
        System.out.println(errorResponse.getError());
        System.out.println(errorResponse.getErrorDescription());
    }

    @Override
    protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
        GenericUrl url = new GenericUrl(req.getRequestURL().toString());
        url.setRawPath(Util.SITE + "/messages");
        return url.build();
    }

    @Override
    protected AuthorizationCodeFlow initializeFlow() throws IOException {
        String clientId = Util.getClientId();
        String clientSecret = Util.getClientSecret();
        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), new NetHttpTransport(),
                new JacksonFactory(), new GenericUrl(Util.SITE + "/users/login"),
                new ClientParametersAuthentication(clientId, clientSecret),
                clientId,
                // authServer
                Util.SITE + "users/login").setCredentialDataStore(
                        StoredCredential.getDefaultDataStore(new FileDataStoreFactory(new File("datastoredir"))))
                        .build();
    }

    @Override
    protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
        // return user ID
        return req.getParameter("name");
    }
  }