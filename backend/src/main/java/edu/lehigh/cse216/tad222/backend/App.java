package edu.lehigh.cse216.tad222.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;

import com.google.common.collect.ImmutableMap;
//Import Google's JSON library
import com.google.gson.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {

    private static Database db;

    public static void main(String[] args) {

        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        //create a modified version of Spark's embedded Jetty server
        /*JettyServerFactory jf = new JettyServerFactory(){
            @Override
            public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
                Server s = new Server(getIntFromEnv("PORT", 4567));
                return s;
            }
        };
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(Util.SITE);
        context.addServlet(new ServletHolder(new AuthCodeServlet()), "/users/login");        
        context.addServlet(new ServletHolder(new AuthCodeCallbackServlet()), "/users/login/callback");
        
        EmbeddedJettyServer server = new EmbeddedJettyServer(jf, context);*/

        // Database URL
        Map<String, String> env = System.getenv();
        String db_url = env.get("DATABASE_URL");   
        System.out.println("database is: " + db_url);    
        
        /*Properties prop = new Properties();
        String config = "backend.properties";
        try {
            InputStream input = App.class.getClassLoader().getResourceAsStream(config);
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String db_url = prop.getProperty("DATABASE_URL");*/

        //String db_url = "postgres://azexrkxulzlqss:b12fcddc21a71c8cc0b04de34d8ab4bc99a726bdb0b2e455b63865e0cdbb3442@ec2-3-234-109-123.compute-1.amazonaws.com:5432/d9aki869as2d5b";
        //String cors_enabled = env.get("CORS_ENABLED");
        String cors_enabled = "TRUE";
        if (cors_enabled.equals("TRUE")) { 
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

        db = Database.getDatabase(db_url); //remote Postgres database object

        System.out.println(db_url);
        // dataStore holds all of the data that has been provided via HTTP 
        // requests
        //
        // NB: every time we shut down the server, we will lose all data, and 
        //     every time we start the server, we'll have an empty dataStore,
        //     with IDs starting over from 0. 
        //final DataStore dataStore = new DataStore(); //local database object

        //Spark.staticFileLocation("/web");
       /* Spark.get(" / ", (request, response) -> {
            res.redirect("/index.html");
            return "";
        });*/

        Spark.get("/", (request, response) -> {
            response.redirect("/messages");
            return "Redirect to /messages";
        });

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
        // request should have a session_id, validate it against a stored session_id in the user_table
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
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            System.out.println("idx is: " + idx);

            /*try {
                likes = Integer.parseInt(req.mMessage);
                //System.out.println("mMessages is: " + mMessage);
                System.out.println("likes is: " + likes);
            } catch(Throwable e) {
                System.out.println("not working!!!!!!!!!!!");
                return gson.toJson(new StructuredResponse("error", "unable to parse body: " + req.mMessage, null));
            }*/
            int result = db.updateLikes(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        Spark.post("/users/register", (request, response) -> {
            String name = request.params("name");
            String email = "";
            String uid = "";
            String secret = "";
            boolean success = db.registerUser(name, email, uid, secret);
            if(success) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " was registered", uid));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + name + " was already registered or had an invalid uid", null));
            }
        });

        Spark.post("/users/login", (request, response) -> {
            response.status(200);
            response.type("application/json");
            StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
                .append("?client_id=").append(request.queryParams("client_id")) // the client id from the api console
                                                                           // registration
                .append("&idToken=" + request.queryParams("idToken"))
                .append("&response_type=code").append("&scope=https://www.googleapis.com/auth/userinfo.profile") // scope is the api permissions we are
                                                                               // requesting
                .append("&redirect_uri=" + Util.SITE + "/users/login/callback") // the servlet that google redirects to after
                                                               // authorization
                //.append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
                .append("&access_type=offline") // here we are asking to access to user's data while they are not signed
                                                // in
                .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are
                                                   // already signed in
            response.redirect(oauthUrl.toString());
            return "temp";
        });

        Spark.get("/users/login", (request, response) -> {
            response.status(200);
            response.type("application/json");
            return "hello world";
        });

        //callback for login route
        Spark.post("/users/login/callback", (request, response) -> {
            response.status(200);
            response.type("application/json");
            if(request.queryParams("error") != null) {
                return gson.toJson(new StructuredResponse("error", "User had invalid credentials", null));
            }

            String code = request.params("code");
            // get the access token by post to Google
            String body = post("https://accounts.google.com/o/oauth2/token", ImmutableMap.<String,String>builder()
            .put("code", code)
            .put("client_id", Util.getClientId())
            .put("client_secret", Util.getClientSecret())
            .put("redirect_uri", Util.SITE + "/users/login/callback")
            .put("grant_type", "authorization_code").build());
    
            // get the access token from json and request info from Google
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
                
            // google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
            String accessToken = jsonObject.get("access_token").getAsString();
            JsonObject json = gson.fromJson((new StringBuilder("https://www.googleapis.com/auth/userinfo.profile?access_token=").append(accessToken).toString()), JsonObject.class);
            String name = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            String cid = request.params("client_id");
            String uid = json.get("id").getAsString();

            if(email.contains("lehigh.edu")){
                return new StructuredResponse("error", "User " + name + " is not part of lehigh.edu", null);
            }
            
            if(db.setUserActive(new User(name, email, cid, uid))) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " was logged in", accessToken));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + name + " was already logged in", null));
            }
        });

        Spark.delete("/users/logoff", (request, response) -> {
            response.status(200);
            response.type("application/json");
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
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllActiveUsers()));
        });

        Spark.get("/users/:user_id", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null){
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllActiveUsers()));
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

    static Database getDatabase() {
        return db;
    }

    public static String post(String url, Map<String,String> formParameters) throws ClientProtocolException, IOException { 
        HttpPost request = new HttpPost(url);
          
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         
        for (String key : formParameters.keySet()) {
         nvps.add(new BasicNameValuePair(key, formParameters.get(key))); 
        }
       
        request.setEntity(new UrlEncodedFormEntity(nvps));
         
        return execute(request);
    }

     // makes a GET request to url and returns body as a string
    public static String get(String url) throws ClientProtocolException, IOException {
        return execute(new HttpGet(url));
    }

       // makes request and checks response code for 200
    private static String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(request);
            
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
    
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
        }
        
        return body;
    }
}
