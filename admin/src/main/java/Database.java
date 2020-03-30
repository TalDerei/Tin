import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;

public class Database {
    /**
     * PreparedStatement: prepared statement is a query that you set up in adavance, and represents a framework to build a certain action.
     * Query: query is a specific instantiation of a prepared statement, where queries are sql text (actions) that tells the database what to do.
     */

    /**
    * The connection to the database.  When there is no connection, it should
    * be null.  Otherwise, there is a valid open connection
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
    * A prepared statement for creating the table (tbldata) in our database
    */
    private PreparedStatement mCreateTable;
    /**
    * A prepared statement for dropping the table (tbldata) in our database
    */
    private PreparedStatement mDropTable;
    /**
     * A prepared statement for listing the table (tbldata) in our database
     */
    private PreparedStatement mShowTable;
    /**
     * A prepared statement for creating the user table (userdata) in our database
     */
    private PreparedStatement mCreateUsers;
    /**
     * A prepared statement for dropping the users table (userdata) in our database
     */
    private PreparedStatement mDropUsers;
    /**
     * A prepared statement for insert a new user into userdata
     */
    private PreparedStatement mInsertUser;
    /**
     * A prepared statement for deleting a row from userdata
     */
    private PreparedStatement mDeleteUser;
    /**
     * A prepared statement for update user nickname in userdata
     */
    private PreparedStatement mUpdateNickname;
    /**
     * A prepared statement for getting all data in the database
     */
    private PreparedStatement mSelectAllUser;
    /**
     * A prepared statement for getting all data by given user_id in the database
     */
    private PreparedStatement mSelectAllByUser;
    /**
     * A prepared statement for deleting a row with user_id
     */
    private PreparedStatement mDeleteOneByUser;
    /**
     * A prepared statement for updating a serial user_id
     */
    private PreparedStatement mUpdateUser;
    /**
     * A prepared statement for creating likes table
     */
    private PreparedStatement mCreateLikes;
    /**
     * A prepared statement for dropping likes table
     */
    private PreparedStatement mDropLikes;
    /**
     * A prepared statement for deleting likes row
     */
    private PreparedStatement mDeleteLike;
    /**
     * A prepared statement for inserting like row
     */
    private PreparedStatement mInsertOneLike;
    /**
     * A prepareed statement for updating like vote 
    */
    private PreparedStatement mUpdateOneLike;
    /**
     * boolean for our database membership test: 
     * if mHasUserData == true, then drop UserData table first before dropping tblData
     */
    private boolean mHasUserData = false;


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
        /**
        * user_id in UserData
        */
        int mUserId;

        /**
        * able to set these value from UserData
        */
        String mEmail;
        String mNickname;

        public void setEmail(String email) {
            mEmail = email;
        }
        public void setNickname(String nickname) {
            mNickname = nickname;
        }

        /**
        * Construct a RowData object by providing values for its fields
        */
        public RowData(int id, String subject, String message, int user_id) {
            mId = id;
            mSubject = subject;
            mMessage = message;
            mUserId = user_id;
        }
    }

    public static class UserData {
        /**
         * The ID of this row of UserData
         */
        int mId;
        /**
         * The ID when user's login
         */
        String mEmail;
        /**
         * Used nickname
         */
        String mNickname;

        /**
         * Construct a RowData object by providing values for its fields
         */
        public UserData(int id, String email, String nickname) {
            mId = id;
            mEmail = email;
            mNickname = nickname;
        }
    }

    public static class Likes {
        /**
         * userID in likes table
        */
        int mUserID;
        /**
         * messageID in likes table
         */
        int mMessageID;
        /**
         * likes field in likes tbale
         */
        int mLikes;

        /**
         * Construct a likes object by providing values for its fields
         */
        public void likes(int userID, int messageID, int likes) {
            mUserID = userID;
            mMessageID = messageID;
            mLikes = likes;
        }
    }

    /**
     * Construct a Table object by providing values for its fields
     */
    public static class Table {
        String mSchema;
        String mName;
        String mOwner;
        public Table(String schema, String name, String owner) {
            mSchema = schema;
            mName = name;
            mOwner = owner;
        }
    }

    /**
    * The Database constructor is private: we only create Database objects
    * through the getDatabase() method.
    */
    private Database() {
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

    static Database getDatabase(String ip, String port, String user, String pass) {
        // Create an un-configured Database object
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":" + port + "/", user, pass);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        }
        setPrepareStatement(db);
        return db;
    }

    static Database getDatabaseFromUri(String Uri) {
        // Create an un-configured Database object
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        try {
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI(Uri);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
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
        setPrepareStatement(db);
        return db;
    }

    static void setPrepareStatement(Database db) {
        /**
         * Attempt to create all of our prepared statements (CRUD OPERATIONS). If any of thes fail, the whole getDatabase() call should fail
        */
        try {
            // 1. prepared statements associated with tbldata table
            db.mCreateTable = db.mConnection.prepareStatement("CREATE TABLE tblData (id SERIAL PRIMARY KEY, subject VARCHAR(50) NOT NULL, " + "message VARCHAR(500) NOT NULL, " +
                "user_id INTEGER REFERENCES UserData(id) ON DELETE SET NULL)");
            db.mDropTable = db.mConnection.prepareStatement("DROP TABLE tblData");
            db.mDeleteOne = db.mConnection.prepareStatement("DELETE FROM tblData WHERE id = ?");
            db.mSelectAll = db.mConnection.prepareStatement("SELECT id, subject FROM tblData");
            db.mSelectOne = db.mConnection.prepareStatement("SELECT * FROM tblData WHERE id = ?");
            db.mInsertOne = db.mConnection.prepareStatement("INSERT INTO tblData VALUES (default, ?, ?)");
            db.mUpdateOne = db.mConnection.prepareStatement("UPDATE tblData SET message = ? WHERE id = ?");

            // 2. prepared statements associated with user table
            db.mCreateUsers = db.mConnection.prepareStatement("CREATE TABLE UserData (id SERIAL PRIMARY KEY, email VARCHAR(50) NOT NULL, nickname VARCHAR(50) NOT NULL)");
            db.mDropUsers = db.mConnection.prepareStatement("DROP TABLE UserData");
            db.mDeleteUser = db.mConnection.prepareStatement("DELETE FROM UserData WHERE id = ?");
            db.mUpdateUser = db.mConnection.prepareStatement("UPDATE tblData SET user_id = ? WHERE id = ?");
            db.mInsertUser = db.mConnection.prepareStatement("INSERT INTO UserData VALUES (default, ?, ?)");
            db.mUpdateNickname = db.mConnection.prepareStatement("UPDATE UserData SET nickname = ? WHERE id = ?");
            db.mSelectAllUser = db.mConnection.prepareStatement("SELECT id, email, nickname FROM UserData");

            // 3. prepared statements associated with likes table
            db.mCreateLikes = db.mConnection.prepareStatement("CREATE TABLE likes (user_id INTEGER NOT NULL, message_id INTEGER NOT NULL, likes INTEGER NOT NULL, " +
                "PRIMARY KEY (user_id, message_id), FOREIGN KEY (user_id) REFERENCES UserData(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (message_id) REFERENCES tblData(id) ON DELETE CASCADE)");
            db.mDropLikes = db.mConnection.prepareStatement("DROP TABLE likes");
            db.mDeleteLike = db.mConnection.prepareStatement("DELETE FROM likes WHERE user_id = ?");
            db.mInsertOneLike = db.mConnection.prepareStatement("INSERT INTO likes VALUES (?, ?, ?)");
            db.mUpdateOneLike = db.mConnection.prepareStatement("UPDATE likes SET likes = ? WHERE user_id = ?");
            
            // 4. prepared statements associated with JOIN between tbldata & userdata
            db.mSelectAllByUser = db.mConnection.prepareStatement("SELECT tblData.id, subject, message, nickname FROM tblData " + 
                "INNER JOIN UserData ON tblData.user_id = UserData.id WHERE email = ?");
            db.mDeleteOneByUser = db.mConnection.prepareStatement("DELETE FROM tblData USING UserData " + "WHERE tblData.user_id = UserData.id AND email = ?");
            // list of all of the posts, including the email address of the user who made each post
            // db.mPosts = db.mConnection.prepareStatement("SELECT tbldata.id, tbldata.subject, tbldata.message, userdata.email " +
            //  "FROM tbldata LEFT JOIN userdata ON tbldata.user_id = userdata.id;"); 

            // 5. prepared statement to list all tables
            db.mShowTable = db.mConnection.prepareStatement("SELECT * FROM pg_catalog.pg_tables " + "WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema' ");
            
            
            //db.Sum = db.mConnection.prepareStatement("SELECT SUM(likes.user_id) FROM likes WHERE likes.message_id = ?");
            //("SELECT * FROM likes WHERE user_id = ?");
 
        /**
         * catch SQL exception, print stack trace, and close database connection if error is thrown
         */
        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
        }
    }

    /**
     * Close the current connection to the database, if one exists.
     *
     * NB: The connection will always be null after this call, even if an
     *     error occurred during the closing operation.
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
    int insertRow(String subject, String message) {
        int count = 0;
        //int likes = 0; // like vote starts with 0
        try {
            mInsertOne.setString(1, subject);
            mInsertOne.setString(2, message);
            //mInsertOne.setInt(3, likes);
            count += mInsertOne.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    int insertOneLike(int userID, int messageID) {
        int count = 0;
        int likes = 0; // like vote start with 0;
        try {
            mInsertOneLike.setInt(1, userID);
            mInsertOneLike.setInt(2, messageID);
            mInsertOneLike.setInt(3, likes);
            count += mInsertOneLike.executeUpdate();
        } catch  (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Insert a row into the database
     *
     * @param email userID for login
     * @param nickname used name
     *
     * @return The number of rows that were inserted
     */
    int insertUser(String email, String nickname) {
        int count = 0;
        try {
            mInsertUser.setString(1, email);
            mInsertUser.setString(2, nickname);
            count += mInsertUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Query the database for a list of all subjects and their IDs
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAll() {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            ResultSet rs = mSelectAll.executeQuery();
            while (rs.next()) {
                res.add(new RowData(rs.getInt("id"), rs.getString("subject"),
                        null, -1));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Query the database for a list of all subjects and their IDs
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<UserData> selectAllUsers() {
        ArrayList<UserData> res = new ArrayList<UserData>();
        try {
            ResultSet rs = mSelectAllUser.executeQuery();
            while (rs.next()) {
                res.add(new UserData(rs.getInt("id"), rs.getString("email"),
                        rs.getString("nickname")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Query the database for a list of all subjects and their IDs
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<RowData> selectAllByUser(String email) {
        ArrayList<RowData> res = new ArrayList<RowData>();
        try {
            mSelectAllByUser.setString(1, email);
            ResultSet rs = mSelectAllByUser.executeQuery();
            while (rs.next()) {
                RowData rowData = new RowData(rs.getInt("id"), rs.getString("subject"),
                        rs.getString("message"), -1);
                rowData.setEmail(email);
                rowData.setNickname(rs.getString("nickname"));
                res.add(rowData);
            }
            rs.close();
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
                        rs.getString("message"), rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * show tables from Database
     *
     * @return All rows, as an ArrayList
     */
    ArrayList<Table> showTable() {
        ArrayList<Table> res = new ArrayList<Table>();
        try {
            ResultSet rs = mShowTable.executeQuery();
            while (rs.next()) {
                res.add(new Table(rs.getString("schemaname"),
                        rs.getString("tablename"),
                        rs.getString("tableowner")));
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error at showTable()");
            return null;
        }
    }

    /**
     * Delete a row by ID
     *
     * @param id The id of the row to delete
     *
     * @return The number of rows that were deleted.  -1 indicates an error.
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
     * Delete a row by ID
     *
     * @param id The id of the row to delete
     *
     * @return The number of rows that were deleted.  -1 indicates an error.
     */
    int deleteUser(int id) {
        int res = -1;
        try {
            mDeleteUser.setInt(1, id);
            res = mDeleteUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    int deleteLike(int id) {
        int res = -1;
        try {
            mDeleteLike.setInt(1,id);
            res = mDeleteLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Delete a row by ID
     *
     * @param email userID for login
     *
     * @return The number of rows that were deleted.  -1 indicates an error.
     */
    int deleteRowByUser(String email) {
        int res = -1;
        try {
            mDeleteOneByUser.setString(1, email);
            res = mDeleteOneByUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the message for a row in the database
     *
     * @param id The id of the row to update
     * @param message The new message contents
     *
     * @return The number of rows that were updated.  -1 indicates an error.
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
     * Update the message for a row in the database
     *
     * @param id The id of the row to update
     * @param likes vote
     *
     * @return The number of rows that were updated.  -1 indicates an error.
     */
    int updateLike(int user_id, int likes) {
        int res = -1;
        try {
            mUpdateOneLike.setInt(1, likes);
            mUpdateOneLike.setInt(2, user_id);
            res = mUpdateOneLike.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }




    /**
     * Update the message for a row in the database
     *
     * @param id The id of the row to update
     * @param nickname used name
     *
     * @return The number of rows that were updated.  -1 indicates an error.
     */
    int updateNickname(int id, String nickname) {
        int res = -1;
        try {
            mUpdateNickname.setString(1, nickname);
            mUpdateNickname.setInt(2, id);
            res = mUpdateNickname.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Update the message for a row in the database
     *
     * @param id The id of the row to update
     * @param user_id
     *
     * @return The number of rows that were updated.  -1 indicates an error.
     */
    int updateUser(int id, int user_id) {
        int res = -1;
        try {
            mUpdateUser.setInt(1, user_id);
            mUpdateUser.setInt(2, id);
            res = mUpdateUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Create tblData.  If it already exists, this will print an error.
     */
    void createTable() {
        try {
            System.out.println("Create Table...");
            mCreateTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create UserData.  If it already exists, this will print an error.
     */
    void createUser() {
        try {
            System.out.println("Create Users Table...");
            mCreateUsers.execute();
            mHasUserData = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove tblData from the database.  If it does not exist, this will print an error.
     */
    void dropTable() {
        try {
            System.out.println("Delete Table...");
            mDropTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove UserData from the database.  If it does not exist, this will print an error.
     */
    void dropUser() {
        try {
            System.out.println("Delete Users Table...");
            mDropUsers.execute();
            mHasUserData = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create likes table.  If it already exists, this will print an error.
     */
    void createLikes() {
        try {
            System.out.println("Create Likes Table...");
            mCreateLikes.execute();
            mHasUserData = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove likes table from the database. If it does not exist, this will print an error.
     */
    void dropLikes() {
        try {
            System.out.println("Delete Likes Table...");
            mDropLikes.execute();
            mHasUserData = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
