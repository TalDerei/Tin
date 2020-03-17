package edu.lehigh.cse216.tad222.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;

//Import Google's JSON library
import com.google.gson.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;

import java.io.File;

import java.io.IOException;

import java.util.*;

/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {
    public static void main(String[] args) {

        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        // Database URL
        Map<String, String> env = System.getenv();
        String db_url = env.get("DATABASE_URL");   
        System.out.println("database is: " + db_url);     
        String cors_enabled = env.get("CORS_ENABLED");
        if (cors_enabled.equals("True")) { 
            final String acceptCrossOriginRequestsFrom = "*";
            final String acceptedCrossOriginRoutes = "GET,PUT,POST,DELETE,OPTIONS";
            final String supportedRequestHeaders = "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin";
            enableCORS(acceptCrossOriginRequestsFrom, acceptedCrossOriginRoutes, supportedRequestHeaders);
        }

        // gson provides us with a way to turn JSON into objects, and objects
        // into JSON.
        //
        // NB: it must be final, so that it can be accessed from our lambdas
        //
        // NB: Gson is thread-safe.  See 
        // https://stackoverflow.com/questions/10380835/is-it-ok-to-use-gson-instance-as-a-static-field-in-a-model-bean-reuse
        final Gson gson = new Gson();

        Database db =  Database.getDatabase(db_url); //remote Postgres database object

        System.out.println(db_url);
        // dataStore holds all of the data that has been provided via HTTP 
        // requests
        //
        // NB: every time we shut down the server, we will lose all data, and 
        //     every time we start the server, we'll have an empty dataStore,
        //     with IDs starting over from 0. 
        //final DataStore dataStore = new DataStore(); //local database object

        // GET route that returns all message titles and Ids.  All we do is get s
        // the data, embed it in a StructuredResponse, turn it into JSON, and 
        // return it.  If there's no data, we return "[]", so there's no need 
        // for error handling.
        Spark.get("/messages", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null){
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            }
            else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAll()));
        });

        // GET route that returns everything for a single row in the DataStore.
        // The ":id" suffix in the first parameter to get() becomes 
        // request.params("id"), so that we can get the requested row ID.  If 
        // ":id" isn't a number, Spark will reply with a status 500 Internal
        // Server Error.  Otherwise, we have an integer, and the only possible 
        // error is that it doesn't correspond to a row with data.
        Spark.get("/messages/:id", (request, response) -> { //QUERY PARAMETER
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            Database.RowData data = db.selectOne(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        // POST route for adding a new element to the DataStore.  This will read
        // JSON from the body of the request, turn it into a SimpleRequest 
        // object, extract the title and message, insert them, and return the 
        // ID of the newly created row.
        Spark.post("/messages", (request, response) -> {
            // NB: if gson.Json fails, Spark will reply with status 500 Internal 
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            //     describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message
            int newId = db.insertRow(req.mTitle, req.mMessage);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", "" + newId, null));
            }
        });

        // PUT route for updating a row in the DataStore.  This is almost 
        // exactly the same as POST
        Spark.put("/messages/:id", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            int result = db.updateOne(idx, req.mMessage);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        // DELETE route for removing a row from the DataStore
        Spark.delete("/messages/:id", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            // NB: we won't concern ourselves too much with the quality of the 
            //     message sent on a successful delete
            int result = db.deleteRow(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        Spark.put("/likes/:id", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            int likes = 0;
            try {
                likes = Integer.parseInt(req.mMessage);
            } catch(Throwable e) {
                System.out.println("not working!!!!!!!!!!!");
                return gson.toJson(new StructuredResponse("error", "unable to parse body: " + req.mMessage, null));
            }
            int result = db.updateLikes(idx, likes);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        Spark.post("/users/register", (request, response) -> {
            String name = request.params("name");
            String uid = "";
            String secret = "";
            boolean success = db.registerUser(name, uid, secret);
            if(success) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " was registered", uid));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + name + " was already registered or had an invalid uid", null));
            }
        });

        Spark.put("/users/login", (request, response) -> {
            String name = request.params("name");
            String uid = "";
            String secret = "";
            if(db.setUserActive(name, uid, secret)) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " was logged in", uid));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + name + " had invalid credentials", null));
            }
        });

        Spark.delete("/users/logoff", (request, response) -> {
            String name = request.params("name");
            String uid = "";
            if(db.setUserInactive(null)) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " logged off", uid));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + name + " was not logged in", null));
            }
        });

        //Get a list of active users
        Spark.get("/users/", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null){
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.activeUsers));
        });

        Spark.get("/users/callback", (request, response) -> {
            return "";
        });
    }
    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     * 
     * @envar      The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     * 
     * @returns The best answer we could come up with for a value for envar
     */
        /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     * 
     * @envar      The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     * 
     * @returns The best answer we could come up with for a value for envar
     */
    static int getIntFromEnv(String envar, int defaultVal) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get(envar) != null) {
            return Integer.parseInt(processBuilder.environment().get(envar));
        }
        return defaultVal;
    }
    

        /**
     * Set up CORS headers for the OPTIONS verb, and for every response that the
     * server sends.  This only needs to be called once.
     * 
     * @param origin The server that is allowed to send requests to this server
     * @param methods The allowed HTTP verbs from the above origin
     * @param headers The headers that can be sent with a request from the above
     *                origin
     */
    private static void enableCORS(String origin, String methods, String headers) {
        // Create an OPTIONS route that reports the allowed CORS headers and methods
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        // 'before' is a decorator, which will run before any 
        // get/post/put/delete.  In our case, it will put three extra CORS
        // headers into the response
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });
    }

    static boolean verifyToken() {
        String webClientid = "98587864938-kjbvh78jj6ln49k0j2s8poc8ehng9cqm.apps.googleusercontent.com";
        String databaseClientid = "98587864938-4nvcilp8lmt601orgs9eg9rgtk5gsen3.apps.googleusercontent.com";
        String androidClientid = "";
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
            new UrlFetchTransport() , new JacksonFactory())
        // Specify the CLIENT_ID of the app that accesses the backend:
        //.setAudience(Collections.singletonList(CLIENT_ID))
        // Or, if multiple clients access the backend:
        .setAudience(Arrays.asList(webClientid, databaseClientid, androidClientid))
        .build();

        // (Receive idTokenString by HTTPS POST)
        String idTokenString = "";
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (idToken != null) {
            Payload payload = idToken.getPayload();
    
            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);
    
            // Get profile information from payload
            //String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            //String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            //String familyName = (String) payload.get("family_name");
            //String givenName = (String) payload.get("given_name");
    
            // Use or store profile information
            // ...

        } else {
            System.out.println("Invalid ID token.");
        }

        return false;
    }
    
    public class Servlet extends AbstractAuthorizationCodeServlet {

        /**
		 *
		 */
		private static final long serialVersionUID = 2372819854088979956L;

		@Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
           // redirect to google for authorization
            StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
            .append("?client_id=").append(request.getPart("clientid")) // the client id from the api console registration
            .append("&response_type=code")
            .append("&scope=openid%20email") // scope is the api permissions we are requesting
            .append("&redirect_uri=http://limitless-ocean-62391.herokuappp.com/messages") // the servlet that google redirects to after authorization
            .append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
            .append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
            .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
                
            response.sendRedirect(oauthUrl.toString());
        }
      
        @Override
        protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
          GenericUrl url = new GenericUrl(req.getRequestURL().toString());
          url.setRawPath("http://limitless-ocean-62391.herokuappp.com/messages");
          return url.build();
        }
      
        @Override
        protected AuthorizationCodeFlow initializeFlow() throws IOException {
          return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
              new NetHttpTransport(),
              new JacksonFactory(),
              new GenericUrl("http://limitless-ocean-62391.herokuappp.com/messages"),
              new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
              "s6BhdRkqt3",
              "http://limitless-ocean-62391.herokuappp.com/login").setCredentialDataStore(
                  StoredCredential.getDefaultDataStore(
                      new FileDataStoreFactory(new File("datastoredir"))))
              .build();
        }
      
        @Override
        protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
          return "";
        }
      }
      
      public class ServletCallback extends AbstractAuthorizationCodeCallbackServlet {
      
        /**
		 *
		 */
		private static final long serialVersionUID = -2663253438853631359L;

		@Override
        protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential) throws ServletException, IOException {
          resp.sendRedirect("/");
        }
      
        @Override
        protected void onError(HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse) throws ServletException, IOException {
          System.out.println(errorResponse.getError());
          System.out.println(errorResponse.getErrorDescription());
        }
      
        @Override
        protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
          GenericUrl url = new GenericUrl(req.getRequestURL().toString());
          url.setRawPath("http://limitless-ocean-62391.herokuappp.com/messages");
          return url.build();
        }
      
        @Override
        protected AuthorizationCodeFlow initializeFlow() throws IOException {
          return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
              new NetHttpTransport(),
              new JacksonFactory(),
              new GenericUrl("http://limitless-ocean-62391.herokuappp.com/login"),
              //clientid, clientSecret
              new ClientParametersAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
              //clientid
              "s6BhdRkqt3",
              //authServer
              "http://limitless-ocean-62391.herokuappp.com/login").setCredentialDataStore(
                  StoredCredential.getDefaultDataStore(
                      new FileDataStoreFactory(new File("datastoredir"))))
              .build();
        }
      
        @Override
        protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
          // return user ID
          return "";
        }
      }
}
