package edu.lehigh.cse216.tad222.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import java.util.HashSet;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;

public class Database {
    /**
     * The connection to the database. When there is no connection, it should be
     * null. Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    /**
     * A prepared statement for getting all data in the database
     */
    private PreparedStatement mSelectAll;

    /**
     * A prepared statement for getting one row from the database
     */
    private PreparedStatement mSelectOne;

    /**
     * A prepared statement for deleting a row from the database
     */
    private PreparedStatement mDeleteOne;

    /**
     * A prepared statement for inserting into the database
     */
    private PreparedStatement mInsertOne;

    /**
     * A prepared statement for updating a single row in the database
     */
    private PreparedStatement mUpdateOne;

    /**
     * A prepared statement for creating the table in our database
     */
    private PreparedStatement mCreateTable;

    /**
     * A prepared statement for dropping the table in our database
     */
    private PreparedStatement mDropTable;

    private PreparedStatement mUpdateLikes;

    private PreparedStatement mDeleteUser;

    private PreparedStatement mUpdateUser;

    private PreparedStatement mInsertUser;

    private PreparedStatement mUpdateNickname;

    private PreparedStatement mSelectAllUser;

    private PreparedStatement mDeleteLike;

    private PreparedStatement mInsertOneLike;

    private PreparedStatement mUpdateOneLike;

    private PreparedStatement mIsRegistered;

    Set<User> activeUsers;
    HashMap<String, PublicKey> jwtPubKeys = new HashMap<String, PublicKey>();
    HashMap<String, String> jwtKeys;

    /**
     * RowData is like a struct in C: we use it to hold data, and we allow direct
     * access to its fields. In the context of this Database, RowData represents the
     * data we'd see in a row.
     * 
     * We make RowData a static class of Database because we don't really want to
     * encourage users to think of RowData as being anything other than an abstract
     * representation of a row of the database. RowData and the Database are tightly
     * coupled: if one changes, the other should too.
     */
    public static class RowData {
        /**
         * The ID of this row of the database
         */
        int mId;
        /**
         * The subject stored in this row
         */
        String mSubject;
        /**
         * The message stored in this row
         */
        String mMessage;

        String mUser_id;

        /**
         * Construct a RowData object by providing values for its fields
         */
        public RowData(int id, String subject, String message, String uid) {
            mId = id;
            mSubject = subject;
            mMessage = message;
            mUser_id = uid;
        }
    }

    /**
     * The Database constructor is private: we only create Database objects through
     * the getDatabase() method.
     */
    private Database() {
        activeUsers = new HashSet<User>();
        jwtKeys = new HashMap<String, String>();
    }

    /**
     * Get a fully-configured connection to the database
     * 
     * @param ip   The IP address of the database server
     * @param port The port on the database server to which connection requests
     *             should be sent
     * @param user The user ID to use when connecting
     * @param pass The password to use when connecting
     * 
     * @return A Database object, or null if we cannot connect properly
     */
    static Database getDatabase(String db_url) {
        // Create an un-configured Database object
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        /*
         * try { Connection conn = DriverManager.getConnection("jdbc:postgresql://" + ip
         * + ":" + port + "/", user, pass); if (conn == null) { System.err.
         * println("Error: DriverManager.getConnection() returned a null object");
         * return null; } db.mConnection = conn; } catch (SQLException e) { System.err.
         * println("Error: DriverManager.getConnection() threw a SQLException");
         * e.printStackTrace(); return null; }
         */

        // Give the Database object a connection, fail if we cannot get one
        try {
            System.out.println("entered getDatabase!!!!!!!!!!!!!!");
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI(db_url);
            System.out.println("dbURI is!!!!!!!!!!: " + dbUri.toString());
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                    + "?sslmode=require";
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Unable to find postgresql driver");
            return null;
        } catch (URISyntaxException s) {
            System.out.println("URI Syntax Error");
            return null;
        }

        // Attempt to create all of our prepared statements. If any of these
        // fail, the whole getDatabase() call should fail
        try {
            // tblData table:
            db.mDeleteOne = db.mConnection.prepareStatement("DELETE FROM tblData WHERE id = ?");
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO tblData VALUES (default, ?, ?)");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT id, subject, message FROM tblData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT id, subject, message from tblData WHERE id = ?");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE tblData SET message = ? WHERE id = ?");

            // UserData table:
            db.mDeleteUser = db.mConnection.prepareStatement("DELETE FROM UserData WHERE id = ?");
            db.mUpdateUser = db.mConnection.prepareStatement("UPDATE tblData SET user_id = ? WHERE id = ?");
            db.mInsertUser = db.mConnection.prepareStatement("INSERT INTO UserData VALUES (default, ?, ?, ?)");
            db.mUpdateNickname = db.mConnection.prepareStatement("UPDATE UserData SET nickname = ? WHERE id = ?");
            db.mSelectAllUser = db.mConnection.prepareStatement("SELECT id, email, nickname FROM UserData");

            // likes table:
            db.mDeleteLike = db.mConnection.prepareStatement("DELETE FROM likes WHERE user_id = ?");
            db.mInsertOneLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (?, ?, ?)");
            db.mUpdateOneLike = db.mConnection.prepareStatement("UPDATE likes SET likes = ? WHERE user_id = ?");

        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }

    /**
     * Close the current connection to the database, if one exists.
     * 
     * NB: The connection will always be null after this call, even if an error
     * occurred during the closing operation.
     * 
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect() {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    /**
     * Insert a row into the database
     * 
     * @param uid   The subject for this new row
     * @param email The message body for this new rowz
     * 
     * @return The number of rows that were inserted
     */
    int insertRow(String subject, String message) {
        int count = 0;
        try {
            mInsertOne.setString(1, subject);
            mInsertOne.setString(2, message);
            count += mInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    int updateLikes(int id) {
        int res = -1;
        try {
            mUpdateLikes.setInt(1, id);
            // mUpdateLikes.setInt(2, likes);
            res = mUpdateLikes.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Query the database for a list of all subjects and their IDs
     * 
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAll() {
        System.out.println("entered selectAll!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowData(rs.getInt("id"), rs.getString("subject"), rs.getString("message"),
                        rs.getString("user_id")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            System.out.println("caught in exception 1");
            e.printStackTrace();
            System.out.println("caught in exeception 2");
            return null;
        }
    }

    /**
     * Get all data for a specific row, by ID
     * 
     * @param id The id of the row being requested
     * 
     * @return The data for the requested row, or null if the ID was invalid
     */
    RowData selectOne(int id) {
        RowData res = null;
        try {
            mSelectOne.setInt(1, id);
            ResultSet rs = mSelectOne.executeQuery();
            if (rs.next()) {
                res = new RowData(rs.getInt("id"), rs.getString("subject"), rs.getString("message"),
                        rs.getString("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     * 
     * @param id The id of the row to delete
     * 
     * @return The number of rows that were deleted. -1 indicates an error.
     */
    int deleteRow(int id) {
        int res = -1;
        try {
            mDeleteOne.setInt(1, id);
            res = mDeleteOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the message for a row in the database
     * 
     * @param id      The id of the row to update
     * @param message The new message contents
     * 
     * @return The number of rows that were updated. -1 indicates an error.
     */
    int updateOne(int id, String message) {
        int res = -1;
        try {
            mUpdateOne.setString(1, message);
            mUpdateOne.setInt(2, id);
            res = mUpdateOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Create tblData. If it already exists, this will print an error
     */
    void createTable() {
        try {
            mCreateTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void createRegUserTable() {
        try {
            mCreateTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove tblData from the database. If it does not exist, this will print an
     * error.
     */
    void dropRegUserTable() {
        try {
            mDropTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new user to Registered Users
     * 
     */
    boolean registerUser(String email, String nickname, String uid, String bio) {
        // session_id random string for user is created and passed to front end
        User u = new User(email, nickname, uid, bio);
        return false;
    }

    boolean isRegistered(User u) {
        int res = 0;
        try {
            mIsRegistered.setString(1, u.getUserID());
            // res = mIsRegistered.executeQuery().getFetchSize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res > 0;
    }

    boolean setUserInactive(User u) {
        return activeUsers.remove(u);
    }

    boolean setUserActive(User u) {
        return activeUsers.add(u);
    }

    /**
     * Return a list of all active users
     * 
     * @return
     */
    ArrayList<String> selectAllActiveUsers() {
        ArrayList<String> res = new ArrayList<String>();
        for (User u : activeUsers) {
            res.add(u.getEmail());
        }
        return res;
    }

    ArrayList<User> selectAllUsers() {
        System.out.println("Selecting All Users");
        ArrayList<User> res = new ArrayList<User>();
        try {
            ResultSet rs = mSelectAllUser.executeQuery();
            while (rs.next()) {
                res.add(new User(rs.getString("email"), rs.getString("nickname"), rs.getString("id"),
                        rs.getString("biography")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Insert a row into the database
     * 
     * @param uid      The users' id
     * @param email    The user's email
     * @param nickname The user's nickname
     * 
     * @return The number of rows that were inserted
     */
    int insertUser(String uid, String email, String nickname) {
        int res = -1;
        try {
            mInsertUser.setString(1, uid);
            mInsertUser.setString(2, email);
            mInsertUser.setString(3, nickname);
            res = mInsertUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;

    }

    int updateUser(String uid) {
        int res = -1;
        try {
            mUpdateUser.setString(1, uid);
            mUpdateUser.setString(2, uid);
            res = mUpdateUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateNickname(String uid, String nickname) {
        int res = -1;
        try {
            mUpdateNickname.setString(1, nickname);
            mUpdateNickname.setString(2, uid);
            res = mUpdateNickname.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteUser(String uid) {
        int res = -1;
        try {
            mDeleteUser.setString(1, uid);
            res = mDeleteUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int insertOneLike(String uid, int messageId) {
        int res = -1;
        try {
            mInsertOneLike.setString(1, uid);
            mInsertOneLike.setInt(2, messageId);
            mInsertOneLike.setInt(3, 0);
            res = mInsertOneLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int updateOneLike(String uid, int likes) {
        int res = -1;
        try {
            mUpdateOneLike.setInt(1, likes);
            mUpdateOneLike.setString(2, uid);
            res = mUpdateOneLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteLike(String uid) {
        int res = -1;
        try {
            mDeleteLike.setString(1, uid);
            res = mDeleteLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String produceJWTKey(User u) throws JoseException {
        // Generate an RSA key pair, which will be used for signing and verification of
        // the JWT, wrapped in a JWK
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);

        // Give the JWK a Key ID (kid), which is just the polite thing to do
        rsaJsonWebKey.setKeyId("k" + jwtPubKeys.size());
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("BuzzServer");
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setClaim("email", u.getEmail());
        claims.setClaim("name", u.getNickName());
        claims.setClaim("biography", u.getBio());
        claims.setClaim("userID", u.getUserID());

        JsonWebSignature jws = new JsonWebSignature();

        // The payload comes in a json format
        jws.setPayload(claims.toJson());
        // The JWT is signed using the private key
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        // Set the key ID header
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        // Set the signature algorithm
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        String uid = u.getUserID();
        jwtPubKeys.put(uid, rsaJsonWebKey.getPublicKey());

        return jws.getCompactSerialization();
    }

    User consumeJWTKey(String uid, String jwt) {
        User res = null;
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setExpectedIssuer("BuzzServer") // whom the JWT needs to have been issued by
                .setVerificationKey(getPublicKey(uid)) // verify the signature with the public key
                .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                        ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256) // which is only RS256 here
                .build(); // create the JwtConsumer instance

        try {
            // Validate the JWT and process it to the Claims
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            res = new User(jwtClaims.getClaimValueAsString("email"),
                jwtClaims.getClaimValueAsString("name"), jwtClaims.getClaimValueAsString("userID"),
                jwtClaims.getClaimValueAsString("biography"));
        } catch (InvalidJwtException e) {
            // InvalidJwtException will be thrown, if the JWT failed processing or
            // validation in anyway.
            // Hopefully with meaningful explanations(s) about what went wrong.
            System.out.println("Invalid JWT! " + e);

            // Or maybe the audience was invalid
            if (e.hasErrorCode(ErrorCodes.AUDIENCE_INVALID)) {
                try {
                    System.out.println("JWT had wrong audience: " + e.getJwtContext().getJwtClaims().getAudience());
                } catch (MalformedClaimException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return res;
    }

    PublicKey getPublicKey(String uid){
        return jwtPubKeys.get(uid);
    }

    boolean addJWT(String jwt){
        return false;
    }
}
