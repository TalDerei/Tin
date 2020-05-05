package edu.lehigh.cse216.tad222.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.lang.InterruptedException;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
     * A prepared statement for getting all data in the database
     */
    private PreparedStatement mSelectMessagesByUser;

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
    
    private PreparedStatement mSelectOneUser;

    private PreparedStatement mSelectOneUserById;

    private PreparedStatement mSelectAllUser;

    private PreparedStatement mRemoveLike;

    private PreparedStatement mDeleteLike;

    private PreparedStatement mInsertOneLike;

    private PreparedStatement mGetLikeUser;

    private PreparedStatement mGetLikeMessage;

    private PreparedStatement mUpdateOneLike;

    private PreparedStatement mLikesNeutral;

    private PreparedStatement mIsRegistered;

    private PreparedStatement mInsertFile;

    private PreparedStatement mSelectAllFiles;

    private PreparedStatement mSelectAllJoin;


    Set<User> activeUsers;
    MemcachedClient jwtPubKeys;
    MemcachedClient jwtKeys;


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

        int mLikes;

        String mLink;
        boolean flag;

        /**
         * primary key to connect UserData table
         */
        int mUser_id;

        /**
         * other attributes of UserData
         */
        String username; // specific user name
        String picture; // user picture url
        String userID; // user picture url

        /**
         * Foreign key to connect FileData
         */
        String fileId; // primary key in files table
        /**
         * other attributes of FileData
         */
        String fname; // filename
        long size; //  filesize

        /**
         * Construct a RowData object by providing values for its fields
         */
        public RowData(int id, String subject, String message, int uid, int likes) {
            mId = id;
            mSubject = subject;
            mMessage = message;
            mUser_id = uid;
            this.mLikes = likes;
        }
    }


    /**
     * The Database constructor is private: we only create Database objects through
     * the getDatabase() method.
     */
    private Database() {
        activeUsers = new HashSet<User>();
        jwtPubKeys = buildMemcached();
        jwtKeys = buildMemcached();
    }

    public static MemcachedClient buildMemcached(){
        List<InetSocketAddress> servers =
      AddrUtil.getAddresses("mc4.dev.ec2.memcachier.com:11211".replace(",", " "));
    AuthInfo authInfo =
      AuthInfo.plain("4A3703","85B292273D2D7A730CEE5962438DDDFB");

    MemcachedClientBuilder builder = new XMemcachedClientBuilder(servers);

    // Configure SASL auth for each server
    for(InetSocketAddress server : servers) {
      builder.addAuthInfo(server, authInfo);
    }

    // Use binary protocol
    builder.setCommandFactory(new BinaryCommandFactory());
    // Connection timeout in milliseconds (default: )
    builder.setConnectTimeout(1000);
    // Reconnect to servers (default: true)
    builder.setEnableHealSession(true);
    // Delay until reconnect attempt in milliseconds (default: 2000)
    builder.setHealSessionInterval(2000);
    MemcachedClient mc = null;

    try {
      mc = builder.build();
    } catch (IOException ioe) {
      System.err.println("Couldn't create a connection to MemCachier: " + ioe.getMessage());
    }
        if(mc == null) {
            System.out.println("Something went horribly wrong while making MemcachedClient! Fix it first!");
        }
    return mc;
    }

    /**
     * Get a fully-configured connection to the database
     * 

     * @param db_url Heroku db URL
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
        boolean connect_to_local = false;

        // Give the Database object a connection, fail if we cannot get one
        try {
            System.out.println("entered getDatabase!!!!!!!!!!!!!!");
            System.out.println(db_url);
            Class.forName("org.postgresql.Driver");
            if (connect_to_local) db_url = "jdbc:postgresql://localhost:5432/cse216";
            URI dbUri = new URI(db_url);
            System.out.println("dbURI is!!!!!!!!!!: " + dbUri.toString());


            String username = "postgres";
            String password = "";
            if (connect_to_local == false) {
                username = dbUri.getUserInfo().split(":")[0];
                password = dbUri.getUserInfo().split(":")[1];
            }

            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                    + "?sslmode=require";
            if (connect_to_local) dbUrl = "jdbc:postgresql://localhost:5432/cse216";
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            // success: localhost:4567/messages
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
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO tblData VALUES (default, ?, ?, ?, ?, ?) RETURNING id");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT id, subject, message, user_id FROM tblData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT id, subject, message, user_id from tblData WHERE id = ?");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE tblData SET message = ? WHERE id = ?");
            db.mSelectMessagesByUser = db.mConnection.prepareStatement("SELECT id, subject, message, user_id FROM tblData WHERE user_id = ?");
            db.mSelectAllJoin = db.mConnection.prepareStatement("SELECT tblData.id, subject, message, tblData.user_id, link, flag, " +
                    "UserData.nickname, UserData.picture, UserData.userid, files.fileId, files.fname, files.size FROM tblData " +
                    "LEFT JOIN UserData ON tblData.user_id = UserData.id " +
                    "LEFT JOIN files ON tblData.id = files.messageId ORDER BY tblData.id");

            // UserData table:
            db.mDeleteUser = db.mConnection.prepareStatement("DELETE FROM UserData WHERE id = ?");
            db.mUpdateUser = db.mConnection.prepareStatement("UPDATE tblData SET user_id = ? WHERE id = ?");
            db.mInsertUser = db.mConnection.prepareStatement("INSERT INTO UserData VALUES (default, ?, ?, ?, ?, ?)");
            db.mUpdateNickname = db.mConnection.prepareStatement("UPDATE UserData SET nickname = ? WHERE id = ?");
            db.mSelectOneUser = db.mConnection.prepareStatement("SELECT id, email, userID, nickname, picture, biography FROM UserData WHERE userID = ?");
            db.mSelectOneUserById = db.mConnection.prepareStatement("SELECT id, email, userID, nickname, picture, biography FROM UserData WHERE id = ?");
            db.mSelectAllUser = db.mConnection.prepareStatement("SELECT id, email, nickname, biography FROM UserData");

            // likes table:
            db.mRemoveLike = db.mConnection.prepareStatement("DELETE FROM likes WHERE message_id = ?");
            db.mDeleteLike = db.mConnection.prepareStatement("DELETE FROM likes WHERE user_id = ? AND message_id = ?");
            db.mGetLikeUser = db.mConnection.prepareStatement("SELECT user_id, message_id FROM likes WHERE user_id = ? AND message_id = ?");
            db.mGetLikeMessage = db.mConnection.prepareStatement("SELECT likes FROM likes WHERE message_id = ?");
            db.mLikesNeutral = db.mConnection.prepareStatement("SELECT SUM(likes.likes) AS total FROM likes WHERE likes.message_id = ?");
            db.mInsertOneLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (?, ?, ?)");
            db.mUpdateOneLike = db.mConnection.prepareStatement("UPDATE likes SET likes = ? WHERE user_id = ?");

            // files table
            db.mInsertFile = db.mConnection.prepareStatement("INSERT INTO files (fileid, messageid, mime, url, fname, size) VALUES (?,?,?,?,?,?)");
            db.mSelectAllFiles = db.mConnection.prepareStatement("SELECT * FROM files");

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
     * @param subject The subject for this new row
     * @param message The message body for this new row
     * 
     * @return The number of rows that were inserted
     */
    int insertRow(String subject, String message, int user_id, String link, boolean flag) {
        int res = 0;
        try {
            mInsertOne.setString(1, subject);
            mInsertOne.setString(2, message);
            mInsertOne.setInt(3, user_id);
            mInsertOne.setString(4, link);
            mInsertOne.setBoolean(5, flag);
            //mInsertOne.executeUpdate();
            ResultSet rs = mInsertOne.executeQuery();
            if (rs.next()) {
                res = rs.getInt("id");
            }
        } catch (SQLException e) {
            res = -1;
            e.printStackTrace();
        }
        return res;
    }

    int getUserMessageLikes(int uid, int messageId) {
        int res = -1;
        try {
            mGetLikeUser.setInt(1, uid);
            mGetLikeUser.setInt(2, messageId);
            ResultSet rs = mGetLikeUser.executeQuery();
            if (rs.next()) {
                System.out.println("uid");
                System.out.println(rs.getInt("user_id"));
                System.out.println("mid");
                System.out.println(rs.getInt("message_id"));
                res = 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int getMessageLikes(int messageId) {
        int res = 0;
        try {
            mGetLikeMessage.setInt(1, messageId);
            ResultSet rs = mGetLikeMessage.executeQuery();
            while (rs.next()) {
                int likes = rs.getInt("likes");
                System.out.println("given likes is ");
                System.out.println(likes);
                res += likes;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res = -1;
        }
        return res;
    }

    int deleteLike(int uid, int messageId) {
        int res = 1;
        try {
            mDeleteLike.setInt(1, uid);
            mDeleteLike.setInt(2, messageId);
            mDeleteLike.executeUpdate();
        } catch (SQLException e) {
            res = -1;
            e.printStackTrace();
        }
        return res;
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
        System.out.println("entered selectAll!!!!!!!!!!!!!!!");
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()) { 
                //res.add(new RowData(rs.getInt("id"), rs.getString("subject"),
                //    rs.getString("message"), rs.getInt("user_id"), getTotalLikes(rs.getInt("id"))));
                int likeVote = getMessageLikes(rs.getInt("id"));
                res.add(new RowData(rs.getInt("id"), rs.getString("subject"),
                    rs.getString("message"), rs.getInt("user_id"), likeVote)
                );
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
     * Query the database for a list of all subjects and their IDs
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAllJoin() {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAllJoin.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++ ) {
                String columnName = rsmd.getColumnName(i);
                System.out.println("col" + i + ":" + columnName);
            }

            while (rs.next()) {
                //res.add(new RowData(rs.getInt("id"), rs.getString("subject"),
                //    rs.getString("message"), rs.getInt("user_id"), getTotalLikes(rs.getInt("id"))));
                int likeVote = getMessageLikes(rs.getInt("id"));
                RowData row = new RowData(rs.getInt("id"), rs.getString("subject"),rs.getString("message"), rs.getInt("user_id"), likeVote);
                // optional attributes from tblData
                row.flag = rs.getBoolean("flag");
                row.mLink = rs.getString("link");
                // optional attributes from UserData
                row.username = rs.getString("nickname");
                row.picture = rs.getString("picture");
                row.userID = rs.getString("userid");
                // optional attributes from files
                row.fileId = rs.getString("fileId");
                row.fname = rs.getString("fname");
                row.size = rs.getLong("size");
                res.add(row);
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



    ArrayList<RowData> selectMessagesByUser(int uid) {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            mSelectMessagesByUser.setInt(1, uid);
            ResultSet rs = mSelectMessagesByUser.executeQuery();
            while (rs.next()) {
                int likeVote = getMessageLikes(rs.getInt("id"));
                res.add(new RowData(rs.getInt("id"), rs.getString("subject"),
                        rs.getString("message"), rs.getInt("user_id"), likeVote)
                );
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
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
                res = new RowData(rs.getInt("id"), rs.getString("subject"),
                    rs.getString("message"), rs.getInt("user_id"), getTotalLikes(id));
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
    /**boolean registerUser(String email, String nickname, String uid, String bio) {
        // session_id random string for user is created and passed to front end
        User u = new User(email, nickname, uid, bio);
        return false;
    }*/

    boolean isRegistered(User u) {
        int res = 0;
        try {
            mIsRegistered.setString(1, u.getUserID());
            //res = mIsRegistered.executeQuery().getFetchSize();
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

    User selectOneUser(String userID) {
        User res = null;
        try {
            mSelectOneUser.setString(1, userID);
            ResultSet rs = mSelectOneUser.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++ ) {
                String columnName = rsmd.getColumnName(i);
                System.out.println("col" + i + ":" + columnName);
            }

            if (rs.next()) {
                res = new User(rs.getInt("id"), rs.getString("email"),
                        rs.getString("nickname"), rs.getString("userID"),
                        rs.getString("picture"), rs.getString("biography"));
            }

            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    User selectOneUserById(int uid) {
        User res = null;
        try {
            mSelectOneUserById.setInt(1, uid);
            ResultSet rs = mSelectOneUserById.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            if (rs.next()) {
                res = new User(rs.getInt("id"), rs.getString("email"),
                        rs.getString("nickname"), rs.getString("userID"),
                        rs.getString("picture"), rs.getString("biography"));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    ArrayList<User> selectAllUsers() {
        System.out.println("Selecting All Users");
        ArrayList<User> res = new ArrayList<User>();
        try {
            ResultSet rs = mSelectAllUser.executeQuery();
            while (rs.next()) { 
                res.add(new User(rs.getInt("id"), rs.getString("email"),
                        rs.getString("nickname"), rs.getString("userID"),
                        rs.getString("picture"), rs.getString("biography")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * method to insert a single row to the files table in database
     */
    int insertFileRow(String fileid, int mid, String mime, String url, String fname, long size) {
        System.out.println("insertFileRow");
        int res = -1;
        try{
            mInsertFile.setString(1,fileid);
            mInsertFile.setInt(2, mid);
            mInsertFile.setString(3, mime);
            mInsertFile.setString(4, url);
            mInsertFile.setString(5, fname);
            mInsertFile.setLong(6, size);
            res = mInsertFile.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * method to retrieve all the entries to the files table
     */
    ArrayList<FileUploaded> selectAllFiles() {
        System.out.println("Selecting All Files");
        ArrayList<FileUploaded> res = new ArrayList<FileUploaded>();
        try{
            ResultSet rs = mSelectAllFiles.executeQuery();
            while (rs.next()) { 
                res.add(new FileUploaded(rs.getString("fileid"), rs.getInt("messageid"),
                        rs.getString("mime"), rs.getString("url"),
                        rs.getString("fname"), rs.getLong("size")));
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
     * @param email The user's email
     * @param nickname The user's nickname
     * @param userID google ID
     * @param bio local
     *
     * @return The number of rows that were inserted
     */
    int insertUser(String email, String nickname, String userID, String picture, String bio) {
        int res = -1;
        try {
            mInsertUser.setString(1, email);
            mInsertUser.setString(2, nickname);
            mInsertUser.setString(3, userID);
            mInsertUser.setString(4, picture);
            mInsertUser.setString(5, bio);
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

    int removeMessageLikes(int messageId) {
        int res = -1;
        try {
            mRemoveLike.setInt(1, messageId);
            res = mRemoveLike.executeUpdate();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    int insertOneLike(int uid, int messageId) {
        int res = -1;
        //if(getUserMessageLikes(uid, messageId) > 0) {
        //    return -2;
        //}
        try {
            mInsertOneLike.setInt(1, uid);
            mInsertOneLike.setInt(2, messageId);
            mInsertOneLike.setInt(3, 1);
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

    int getTotalLikes(int id){
        int res = -1;
        try {
            mLikesNeutral.setInt(1, id);
            ResultSet rs = mLikesNeutral.executeQuery();
            rs.next();
            res = rs.getInt("total");
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    String produceJWTKey(User u) throws JoseException, TimeoutException,InterruptedException,MemcachedException{
        // Generate an RSA key pair, which will be used for signing and verification of the JWT, wrapped in a JWK
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        
        // Give the JWK a Key ID (kid), which is just the polite thing to do
        rsaJsonWebKey.setKeyId("k" + rsaJsonWebKey);
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("BuzzServer");
        claims.setAudience(u.getEmail());
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setClaim("email", u.getEmail());
        claims.setClaim("name", "name");
        claims.setClaim("biography", u.getBio());
        //claims.setClaim("userID", u.getUserID());

        JsonWebSignature jws = new JsonWebSignature();

        // The payload comes in a json format
        jws.setPayload(claims.toJson());
        // The JWT is signed using the private key
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        // Set the key ID header
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        // Set the signature algorithm
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        
        jwtPubKeys.set(u.getUserID(), 0, rsaJsonWebKey.getPublicKey());

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
            // turn off by Sangjun
            //res = new User(jwtClaims.getClaimValueAsString("email"),
            //    jwtClaims.getClaimValueAsString("name"), jwtClaims.getClaimValueAsString("userID"),
            //    jwtClaims.getClaimValueAsString("biography"));
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

    PublicKey getPublicKey(String uid) {
        PublicKey toReturn = null;
        try{
           toReturn = jwtPubKeys.get(uid);
        }catch (TimeoutException te) {
            System.err.println("Timeout during set or get: " +
                               te.getMessage());
        } catch (InterruptedException ie) {
            System.err.println("Interrupt during set or get: " +
                               ie.getMessage());
        } catch (MemcachedException me) {
            System.err.println("Memcached error during get or set: " +
                               me.getMessage());
        }
        return toReturn;
    }
}
