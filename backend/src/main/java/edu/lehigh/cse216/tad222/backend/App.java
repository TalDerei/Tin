package edu.lehigh.cse216.tad222.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;

import com.google.common.collect.ImmutableMap;
//Import Google's JSON library
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

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
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.io.StringReader;
import java.security.PublicKey;
import java.util.*;

/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {

    private static Database db;

    public static void main(String[] args) {

        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        // Database URL
        Map<String, String> env = System.getenv();
        String db_url = env.get("DATABASE_URL");
        System.out.println("database is: " + db_url);

        /*
         * Properties prop = new Properties(); String config = "backend.properties"; try
         * { InputStream input = App.class.getClassLoader().getResourceAsStream(config);
         * prop.load(input); } catch (IOException ex) { ex.printStackTrace(); } String
         * db_url = prop.getProperty("DATABASE_URL");
         */

        // String db_url =
        // "postgres://azexrkxulzlqss:b12fcddc21a71c8cc0b04de34d8ab4bc99a726bdb0b2e455b63865e0cdbb3442@ec2-3-234-109-123.compute-1.amazonaws.com:5432/d9aki869as2d5b";
        // String cors_enabled = env.get("CORS_ENABLED");
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
        // NB: Gson is thread-safe. See
        // https://stackoverflow.com/questions/10380835/is-it-ok-to-use-gson-instance-as-a-static-field-in-a-model-bean-reuse
        final Gson gson = new Gson();

        db = Database.getDatabase(db_url); // remote Postgres database object

        System.out.println(db_url);
        // dataStore holds all of the data that has been provided via HTTP
        // requests
        //
        // NB: every time we shut down the server, we will lose all data, and
        // every time we start the server, we'll have an empty dataStore,
        // with IDs starting over from 0.
        // final DataStore dataStore = new DataStore(); //local database object

        // Spark.staticFileLocation("/web");
        /*
         * Spark.get(" / ", (request, response) -> { res.redirect("/index.html"); return
         * ""; });
         */

        Spark.get("/", (request, response) -> {
            response.redirect("/messages");
            return "Redirect to /messages";
        });

        // GET route that returns all message titles and Ids. All we do is get s
        // the data, embed it in a StructuredResponse, turn it into JSON, and
        // return it. If there's no data, we return "[]", so there's no need
        // for error handling.
        Spark.get("/messages", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/

            if (db == null) {
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAll()));
        });

        // GET route that returns everything for a single row in the DataStore.
        // The ":id" suffix in the first parameter to get() becomes
        // request.params("id"), so that we can get the requested row ID. If
        // ":id" isn't a number, Spark will reply with a status 500 Internal
        // Server Error. Otherwise, we have an integer, and the only possible
        // error is that it doesn't correspond to a row with data.
        Spark.get("/messages/:id", (request, response) -> { // QUERY PARAMETER
            int idx = Integer.parseInt(request.params("id"));
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
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

        // POST route for adding a new element to the DataStore. This will read
        // JSON from the body of the request, turn it into a SimpleRequest
        // object, extract the title and message, insert them, and return the
        // ID of the newly created row.
        // request should have a session_id, validate it against a stored session_id in
        // the user_table
        Spark.post("/messages", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            // describes the error.
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

        // PUT route for updating a row in the DataStore. This is almost
        // exactly the same as POST
        Spark.put("/messages/:id", (request, response) -> {
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            Database.RowData data = db.selectOne(idx);
            if(!uid.equals(data.mUser_id)) {
                return gson.toJson(new StructuredResponse("error", "User tried to update somebody else's comment", null));
            }
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
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            // NB: we won't concern ourselves too much with the quality of the
            // message sent on a successful delete
            int result = db.deleteRow(idx);
            int likeRes = db.removeMessageLikes(idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete row " + idx, null));
            } else if(likeRes == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to delete likes for message_id " + idx, null));
            }else {
                return gson.toJson(new StructuredResponse("ok", null, null));
            }
        });

        Spark.put("/likes/:id", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            System.out.println("idx is: " + idx);

            /*
             * try { likes = Integer.parseInt(req.mMessage);
             * //System.out.println("mMessages is: " + mMessage);
             * System.out.println("likes is: " + likes); } catch(Throwable e) {
             * System.out.println("not working!!!!!!!!!!!"); return gson.toJson(new
             * StructuredResponse("error", "unable to parse body: " + req.mMessage, null));
             * }
             */
            int result = db.insertOneLike(uid, idx);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update likes for message_id " + idx, null));
            } else if (result == -2) {
                return gson.toJson(new StructuredResponse("warning", "User with id " + uid + " already liked message_id " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        Spark.delete("/likes/:id", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            System.out.println("idx is: " + idx);

            int result = db.deleteLike(uid);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update likes for message_id " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        Spark.post("/likes/:id", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            System.out.println("idx is: " + idx);
            int likes = 0; //placeholder
            int result = db.updateOneLike(uid, likes);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update likes for message_id " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        Spark.post("/users/register", (request, response) -> {
            String name = request.params("name");
            String email = "";
            String uid = "";
            String secret = "";
            String bio = "";
            boolean success = false;//db.registerUser(email, name, uid, bio);
            if (success) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " was registered", uid));
            } else {
                return gson.toJson(new StructuredResponse("error",
                        "User " + name + " was already registered or had an invalid uid", null));
            }
        });

        Spark.post("/users/login", (request, response) -> {
            response.status(200);
            response.type("application/json");
            String cid = env.get("CLIENT_ID");
            String cis = env.get("CLIENT_SECRET");
            String idToken = request.queryParams("idToken");

            if (request.queryParams("error") != null) {
                return gson.toJson(new StructuredResponse("error", "User had invalid credentials", null));
            }

            String code = request.queryParams("code");
            if (code == null) {
                return gson.toJson(new StructuredResponse("error", "Code was null", null));
            }
            // get the access token by post to Google
            String body = post("https://oauth2.googleapis.com/token",
                    ImmutableMap.<String, String>builder().put("code", code).put("client_id", cid)
                            .put("client_secret", cis)
                            .put("redirect_uri", Util.SITE + "/messages")
                            .put("grant_type", "authorization_code").build());

            // get the access token from json and request info from Google
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);

            String accessToken = jsonObject.get("access_token").getAsString();
            String jsonString = get((new StringBuilder("https://www.googleapis.com/oauth2/v3/userinfo?access_token=")
                            .append(accessToken).toString()));
            //System.out.println(jsonString);
            JsonObject json = gson.fromJson(jsonString, JsonObject.class);
            String nickname = json.get("name").getAsString();
            String email = json.get("email").getAsString();
            final String uid = json.get("sub").getAsString();
            String bio = "";

            if (!email.contains("lehigh.edu")) {
                return gson.toJson(new StructuredResponse("error", "User " + nickname + " is not part of lehigh.edu", null));
            }

            User u = new User(email, nickname, uid, bio);
            //final String jwt = "{\n\"user_id\":\"" + uid + "\"\n\"jwt\":\"" + db.produceJWTKey(u) + "\"\n}";
            Object jo = gson.toJson(new Object () {
                String mJWT = db.produceJWTKey(u);
                String mUser_id = uid;
            });
            
            if (db.setUserActive(u)) {
                return gson.toJson(new StructuredResponse("ok", "User " + nickname + " was logged in", jo));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + nickname + " was already logged in", null));
            }
        });

        Spark.get("/users/login", (request, response) -> {
            response.status(200);
            response.type("application/json");
            return request.queryParams("client_id");
        });

        Spark.delete("/users/logoff", (request, response) -> {
            response.status(200);
            response.type("application/json");
            String name = request.params("name");
            String uid = "";
            if (db.setUserInactive(null)) {
                return gson.toJson(new StructuredResponse("ok", "User " + name + " logged off", uid));
            } else {
                return gson.toJson(new StructuredResponse("error", "User " + name + " was not logged in", null));
            }
        });

        // Get a list of active users
        Spark.get("/users/", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null) {
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllUsers()));
        });

        Spark.get("/users/:user_id", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null) {
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllActiveUsers()));
        });

        Spark.post("/users/:user_id/bio", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null) {
                System.out.println("error with DB!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllActiveUsers()));
        });

        Spark.put("/users/:user_id/bio", (request, response) -> {
            String jwt = request.queryParams("jwt");
            String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            if (db == null) {
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
     * @envar The name of the environment variable to get.
     * @defaultVal The integer value to use as the default if envar isn't found
     * 
     * @returns The best answer we could come up with for a value for envar
     */
    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     * 
     * @envar The name of the environment variable to get.
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
     * server sends. This only needs to be called once.
     * 
     * @param origin  The server that is allowed to send requests to this server
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
        // get/post/put/delete. In our case, it will put three extra CORS
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

    public static String post(String url, Map<String, String> formParameters)
            throws ClientProtocolException, IOException {
        HttpPost request = new HttpPost(url);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

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
            throw new RuntimeException(
                    "Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
        }

        return body;
    }

    static Object verify(String uid, String jwt) {
        if(uid.isEmpty() || uid == null || jwt.isEmpty() || jwt == null) {
            return new StructuredResponse("error", "No uid and/or jwt given", null);
        }
        JsonWebSignature jws = new JsonWebSignature(); 
        PublicKey pk = db.getPublicKey(uid);
        boolean verified = false;
        jws.setAlgorithmConstraints(
                new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256));
        try {
            jws.setCompactSerialization(jwt);
            jws.setKey(pk);
            verified = jws.verifySignature();
        } catch (JoseException je) {
            je.printStackTrace();
        }

        if(verified) {
            return new StructuredResponse("error", "Couldn't verify user", jwt);
        }

        return "Verification successful";
    }
}
