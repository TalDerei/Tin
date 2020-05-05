package edu.lehigh.cse216.tad222.backend;

// Import the Spark package, so that we can make use of the "get" function to 
// create an HTTP GET route
import spark.Spark;

import com.google.common.collect.ImmutableMap;
//Import Google's JSON library
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.Drive;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.*;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;


/**
 * For now, our app creates an HTTP server that can only get and add data.
 */
public class App {

    private static Database db;
    private static int lastId = 0;
    private static final Gson gson = new Gson();
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "./target/ServiceAccountKeyP12.p12";
    public static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    public static final String APPLICATION_NAME = "Naked Mole Rat backend";
    protected static String slang = "fuck|shit|bitch|asshole|damn";




    /**
     * set up file folder, file object, and put file parameters into database
     *
     * @param UPLOAD_FILE uploaded file
     * @param name random name based on current time
     * @param mime file type (e.g. pdf, png)
     * @param mid correspond message ID
     * @param fname upload filename
     * @param size uploaded file size
     */
    public static String uploadFile(java.io.File UPLOAD_FILE, String name, String mime, int mid, String fname, long size) throws IOException, GeneralSecurityException {
        // boolean useDirectUpload = true;
        String ret = null;

        /* debug */
        System.out.println("UPLOAD_FILE");
        System.out.println(UPLOAD_FILE.getName());
        System.out.println("fname");
        System.out.println(fname);
        System.out.println("size");
        System.out.println(size);

        /* set file folder */
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File folderMade = setup().files().create(fileMetadata)
                .setFields("id")
                .execute();
        String folderId = folderMade.getId();

        /* set file content */
        com.google.api.services.drive.model.File fmeta = new com.google.api.services.drive.model.File();
        fmeta.setName(name);
        FileContent mediaContent = new FileContent(mime, UPLOAD_FILE);
        fmeta.setParents(Collections.singletonList(folderId));
        File file = setup().files().create(fmeta, mediaContent).setFields("id, parents").execute();

        /* set File ID */
        System.out.println("File ID executed: " + file.getId());

        /* save file into DB */
        try {
            int dbReturned = db.insertFileRow(file.getId(), mid, mime, name, fname, size);
            if(dbReturned == -1) {
                System.out.println("Error when inserting file information in database!");
            }
            return file.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    /**
     * Subroutine used in uploadFile()
     */
    public static Drive setup() throws IOException, GeneralSecurityException {

        String emailAddress = "internaldriveuser@the-buzz-1584144072767.iam.gserviceaccount.com";
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY).setServiceAccountId(emailAddress)
                .setServiceAccountPrivateKeyFromP12File(new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
                .setServiceAccountScopes(SCOPES).build();

        if (!credential.refreshToken()) {
            throw new RuntimeException("Failed OAuth to refresh the token");
        }

        Drive service = new Drive.Builder(App.HTTP_TRANSPORT, App.JSON_FACTORY, credential)
                .setApplicationName(App.APPLICATION_NAME).build();

        return service;
    }

    /**
     * Subroutine used in download (GET)
     * Return file type
     *
     * @param fileId fileId that user want to download
     */

    public static String getMimeType(String fileId) {
        try {
            File file = setup().files().get(fileId).execute();
            String mime = file.getMimeType();
            return mime;
        } catch (Exception e) {
            System.out.println("an error occured: " + e);
        }
        return null;
    }

    /**
     * Subroutine used in download (GET)
     * Return Byte array from file object
     *
     * @param fileId fileId that user want to download
     */
    public static ByteArrayOutputStream downloadFromDrive(String fileId) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            setup().files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            return new ByteArrayOutputStream();
        }
    }

    /**
     * see if token is expired
     * return payload
     *
     * @param tokenId google ID token
     */
    public static Payload ValidToken(String tokenId) throws GeneralSecurityException, IOException {
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                //.setAudience(Collections.singletonList(Util.getClientId()))
                .build();


        System.out.println("tokenId");
        System.out.println(tokenId);
        GoogleIdToken idToken = verifier.verify(tokenId);
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            // Check client ID
            if (!Util.getClientId().contains(payload.getAuthorizedParty()) &&
                    !Util.getClientIdAndroid().contains(payload.getAuthorizedParty())) {
                System.out.println("client ID NOT authorized!");
                return null;
            }
            return payload;
        } else {
            return null;
        }
    }

    public static void main(String[] args) {

        // Get the port on which to listen for requests
        Spark.port(getIntFromEnv("PORT", 4567));

        // Database URL
        Map<String, String> env = System.getenv();
        String db_url = env.get("DATABASE_URL");
        System.out.println("database is: " + db_url);

        boolean isLocalDB = true;


        if (isLocalDB) {
            Properties prop = new Properties();
            String config = "backend.properties";
            try {
                InputStream input = App.class.getClassLoader().getResourceAsStream(config);
                prop.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            db_url = prop.getProperty("DATABASE_URL");
        }

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

            if (db == null) {
                System.out.println("error with DB!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAll()));
        });

        Spark.get("/join", (request, response) -> {
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");

            if (db == null) {
                System.out.println("error with DB!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllJoin()));
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
            System.out.println("request.attributes when GET");
            System.out.println(request.attributes());

            response.status(200);
            response.type("application/json");
            Database.RowData data = db.selectOne(idx);
            if (data == null) {
                return gson.toJson(new StructuredResponse("error", idx + " not found", null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, data));
            }
        });

        Spark.post("/messages", (request, response) -> {

            // NB: if gson.Json fails, Spark will reply with status 500 Internal
            // Server Error
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            // NB: even on error, we return 200, but with a JSON object that
            // describes the error.
            response.status(200);
            response.type("application/json");
            // NB: createEntry checks for null title and message

            System.out.println("request.attributes when POST");
            System.out.println("userID");
            System.out.println("Token!");
            System.out.println(req.tokenId);

            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            if (u == null) hasUser = false;
            if (hasUser) {
                System.out.println("has User!");
                System.out.println(req.userID);
                Payload payload = ValidToken(req.tokenId);
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User is Not enrolled in our DB", null));
            }

            System.out.println(req.tokenId);
            System.out.println("hasUser");
            System.out.println(hasUser);


            System.out.println("link");
            System.out.println(req.mLink);

            /* slang filtering */
            boolean flag = false;
            filteringSlang fs = new filteringSlang(slang);
            String match = fs.filterText(req.mMessage);
            if (fs.isSlang) {
                flag = true;
                req.mMessage = match;
            }

            System.out.println("slang?");
            System.out.println(match);

            int newId = db.insertRow(req.mTitle, req.mMessage, u.getId(), req.mLink, flag);
            if (newId == -1) {
                return gson.toJson(new StructuredResponse("error", "error performing insertion", null));
            } else {
                lastId = newId;
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

            // should allow actual author to edit the message only, so we check user's profile.
            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            Database.RowData data = db.selectOne(idx);
            if (data == null) return gson.toJson(new StructuredResponse("error", "gave wrong id, check URL", null));
            if (u == null) hasUser = false;
            if (hasUser) {
                System.out.println("has User!");
                System.out.println(req.userID);
                boolean emailVerified = false;
                System.out.println(u);
                System.out.println(u.getId());
                System.out.println(data);
                if (data.mUser_id == u.getId()) {
                    Payload payload = ValidToken(req.tokenId);
                    emailVerified = Boolean.valueOf(payload.getEmailVerified());
                }
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User mismatch!", null));
            }

            response.status(200);
            response.type("application/json");
            int result = db.updateOne(idx, req.mMessage);
            if (result == -1) {
                return gson.toJson(new StructuredResponse("error", "unable to update row " + idx, null));
            } else {
                return gson.toJson(new StructuredResponse("ok", null, result));
            }
        });

        Spark.delete("/messages/:id", (request, response) -> {
            // If we can't get an ID, Spark will send a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            // NB: we won't concern ourselves too much with the quality of the
            // message sent on a successful delete

            // should allow actual author to delete the message only, so we check user's profile.
            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            Database.RowData data = db.selectOne(idx);
            if (data == null) return gson.toJson(new StructuredResponse("error", "gave wrong id, check URL", null));
            if (u == null) hasUser = false;

            if (hasUser) {
                System.out.println("has User!");
                System.out.println(req.userID);
                boolean emailVerified = false;
                if (data.mUser_id == u.getId()) {
                    Payload payload = ValidToken(req.tokenId);
                    emailVerified = Boolean.valueOf(payload.getEmailVerified());
                }
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User mismatch!", null));
            }


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
            //String jwt = request.queryParams("jwt");
            //String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            System.out.println("idx is: " + idx);

            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            if (u == null) hasUser = false;
            if (hasUser) {
                System.out.println("has User!");
                System.out.println(req.userID);
                Payload payload = ValidToken(req.tokenId);
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User is Not enrolled in our DB", null));
            }

            String popup = "Maybe you did a vote already before!";
            int res = db.getUserMessageLikes(u.getId(), idx);
            if (res < 0) {
                db.insertOneLike(u.getId(), idx);
                popup = "Increase Like vote by 1!";
            }
            System.out.println("popup!!");
            System.out.println(popup);
            int result = db.getMessageLikes(idx);
            if (result < 0)
                return gson.toJson(new StructuredResponse("error", "function getMessageLikes() has error!", null));
            return gson.toJson(new StructuredResponse("ok", popup, result));

            //int result = db.insertOneLike(uid, idx);
            //if (result == -1) {
            //    return gson.toJson(new StructuredResponse("error", "unable to update likes for message_id " + idx, null));
            //} else if (result == -2) {
            //    return gson.toJson(new StructuredResponse("warning", "User with id " + uid + " already liked message_id " + idx, null));
            //} else {
            //    return gson.toJson(new StructuredResponse("ok", null, result));
            //}
        });

        Spark.delete("/likes/:id", (request, response) -> {
            //String jwt = request.queryParams("jwt");
            //String uid = request.queryParams("uid");
            /*String v = gson.toJson(verify(uid, jwt));
            if(v.contains("error")) {
                return v;
            }*/
            // If we can't get an ID or can't parse the JSON, Spark will send
            // a status 500
            int idx = Integer.parseInt(request.params("id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            // ensure status 200 OK, with a MIME type of JSON
            response.status(200);
            response.type("application/json");
            System.out.println("idx is: " + idx);

            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            if (u == null) hasUser = false;
            if (hasUser) {
                System.out.println("has User!");
                System.out.println(req.userID);
                Payload payload = ValidToken(req.tokenId);
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User is Not enrolled in our DB", null));
            }

            int result = db.deleteLike(u.getId(), idx);
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

        Spark.post("/users/login", (request, response) -> {
            response.status(200);
            response.type("application/json");
            String cid = env.get("CLIENT_ID");
            String cis = env.get("CLIENT_SECRET");

            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            System.out.println("request");
            System.out.println(request.body());
            System.out.println("tokenId");
            System.out.println(req.tokenId);


            Payload payload = ValidToken(req.tokenId);

            if (payload != null) {

                // Print user identifier
                String userId = payload.getSubject();
                System.out.println("User ID: " + userId);

                // Get profile information from payload
                String email = payload.getEmail();
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                System.out.println("User ID: " + userId);
                System.out.println("email: " + email);
                System.out.println("email Veryfiled: " + emailVerified);
                System.out.println("name: " + name);
                System.out.println("locale: " + locale);
                System.out.println("familyName: " + familyName);
                System.out.println("givenName: " + givenName);
                System.out.println("client ID check!");
                System.out.println(payload.getAuthorizedParty());
                System.out.println(Util.getClientId());

                if (!email.contains("lehigh.edu")) {
                    return gson.toJson(new StructuredResponse("error",
                            "e-mail is not part of lehigh.edu", null));
                }

                // user already in DB?
                User u = db.selectOneUser(userId);
                boolean hasUser = true;
                if (u == null) {
                    hasUser = false;
                    db.insertUser(email, name, userId, pictureUrl, locale);
                    //u = new User(email, name, userId, locale);
                }

                //String JWTKey = db.produceJWTKey(u);
                //String v = gson.toJson(verify(userId, JWTKey));
                //System.out.println("v: " + v);
                //return gson.toJson(new AuthResponse(JWTKey, userId));
                return gson.toJson(new AuthResponse(req.tokenId, userId));
            } else {
                System.out.println("Invalid ID token or client ID");
                return gson.toJson(new StructuredResponse("error", "invalid ID token or client ID", null));
            }
        });

        Spark.get("/history/:user_id", (request, response) -> {
            response.status(200);
            response.type("application/json");

            int user_id = Integer.parseInt(request.params("user_id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            System.out.println("History!");

            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            if (u == null) hasUser = false;
            if (hasUser) {
                Payload payload = ValidToken(req.tokenId);
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User is Not enrolled in our DB", null));
            }

            if (db == null) {
                System.out.println("error with DB!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectMessagesByUser(user_id)));
        });

        Spark.get("/profile/:user_id", (request, response) -> {
            response.status(200);
            response.type("application/json");

            int user_id = Integer.parseInt(request.params("user_id"));
            SimpleRequest req = gson.fromJson(request.body(), SimpleRequest.class);
            System.out.println("History!");

            boolean hasUser = true;
            User u = db.selectOneUser(req.userID);
            if (u == null) hasUser = false;
            if (hasUser) {
                Payload payload = ValidToken(req.tokenId);
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                if (!emailVerified)
                    return gson.toJson(new StructuredResponse("error", "Google ID Token not vaild!", null));
            } else {
                return gson.toJson(new StructuredResponse("error", "User is Not enrolled in our DB", null));
            }

            if (db == null) {
                System.out.println("error with DB!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectOneUserById(user_id)));
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


        /*
         * POST route to upload files to drive
         */
        Spark.post("/upload", (request, response) -> {
            response.status(200);
            response.type("application/json");
            int idx = lastId;
            StructuredResponse sResponse = new StructuredResponse(response);
                try {
                    String time = "" + java.lang.System.currentTimeMillis();
                    java.io.File upload = new java.io.File(time);
                    if (!upload.exists() && !upload.mkdirs()) {
                        throw new RuntimeException("Failed to create directory " + upload.getAbsolutePath());
                    }

                    // apache commons-fileupload to handle file upload
                    DiskFileItemFactory factory = new DiskFileItemFactory();
                    factory.setRepository(upload);
                    ServletFileUpload fileUpload = new ServletFileUpload(factory);
                    List<FileItem> items = fileUpload.parseRequest(request.raw());

                    Iterator<FileItem> iter = items.iterator();

                    java.io.File uploadedFile = null;
                    String mime = "unknown";
                    boolean hasMime = false;
                    boolean hasFile = false;
                    boolean hasWritten = false;
                    String ret = "null";
                    String fname = null;
                    long size = 0;


                    while (iter.hasNext()) {
                        FileItem item = iter.next();
                        System.out.println(item);
                        if (item.getFieldName().equals("upload_file")) {
                            System.out.println(item);
                            fname = item.getName();
                            size = item.getSize();
                            System.out.println("fname");
                            System.out.println("size");
                            System.out.println(fname);
                            System.out.println(size);
                            uploadedFile = new java.io.File("uploads/" + upload.getName());
                            item.write(uploadedFile);
                            hasFile = true;
                        } else if (item.getFieldName().equals("mime")) {
                            mime = item.getString();
                            hasMime = true;
                        }

                        if (!hasWritten && hasMime && hasFile) {
                            hasWritten = true;
                            //ret = uploadFile(uploadedFile, time, mime);
                            ret = uploadFile(uploadedFile, time, mime, idx, fname, size);
                        }

                    }

                    if (!ret.equals("null")) {
                        sResponse.mData = ret;
                        sResponse.setSuccessful("file uploaded!!!");
                        sResponse.setStringData(ret);
                    } else {
                        sResponse.setError("upload in google drive failed");
                    }

                } catch (RuntimeException e) {
                    response.status(200);
                    e.printStackTrace();
                    sResponse.setError(e);
                }
            return App.getGson().toJson(sResponse);
        });


        /*
         * GET route for downloading files using their specific file id
         */
        Spark.get("/file/:fileId", (request, response) -> {
                String fileId = request.params("fileId");
                String mime = getMimeType(fileId);
                ByteArrayOutputStream os = downloadFromDrive(fileId);
                response.status(200);
                response.raw().setContentType(mime);
                response.raw().setHeader("Content-Disposition", "attachment; mime=" + mime);
                OutputStream toConn = response.raw().getOutputStream();
                toConn.write(os.toByteArray());
                toConn.flush();
                return response;
        });

        /*
         * GET route to get back all the file ids for all the files that have been uploaded so far
         */
        Spark.get("/files", (request, response) -> {
            response.status(200);
            response.type("application/json");
            if (db == null) {
                System.out.println("error with DB!!");
            } else {
                System.out.println("db is NOT null");
            }
            return gson.toJson(new StructuredResponse("ok", null, db.selectAllFiles()));
        });
    }

    static Gson getGson() {
        return gson;
    }

    /**
     * Get an integer environment varible if it exists, and otherwise return the
     * default value.
     * 
     * @param envar The name of the environment variable to get.
     * @param defaultVal The integer value to use as the default if envar isn't found
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
