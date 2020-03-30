package edu.lehigh.cse216.tad222.backend;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;

class Util {

    static final String SITE = "https://limitless-ocean-62391.herokuappp.com";
    private static final String cid = "http://131496045117-k91913gk3j5li0i9k4ov52vg187j56hu.apps.googleusercontent.com/";
    private static final String cls = "";
    private static GoogleClientSecrets gcSecrets = null;

    static String getClientId() {
        return cid;
    }

    static String getClientSecret() {
        return cls;
    }

    static GoogleClientSecrets getClientCredential() throws IOException {
        if (gcSecrets == null) {
            gcSecrets = GoogleClientSecrets.load(new JacksonFactory(),
              new InputStreamReader(Util.class.getResourceAsStream("/client_secrets.json")));
          Preconditions.checkArgument(!gcSecrets.getDetails().getClientId().startsWith("Enter ")
              && !gcSecrets.getDetails().getClientSecret().startsWith("Enter "),
              "Download client_secrets.json file from https://code.google.com/apis/console/"
              + "?api=calendar into calendar-appengine-sample/src/main/resources/client_secrets.json");
        }
        return gcSecrets;
      }
}