import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Unit test for App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testDataBaseConstructor() {

        /**
         * test for Database connection
         */
        Properties prop = new Properties();
        String config = "config.properties"; // a remote database on heroku for unit testing
        try {
            InputStream input = App.class.getClassLoader().getResourceAsStream(config);
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String db_url = prop.getProperty("DATABASE_URL");
        Database db = Database.getDatabaseFromUri(db_url);
        assertFalse(db == null);

        /**
         * test for Creating User table in Database
         */
        db.createUser();
        ArrayList<Database.Table> table = db.showTable();
        assertTrue(table.size() == 1);
        assertTrue(table.get(0).mSchema.equals("public"));
        assertTrue(table.get(0).mName.equals("userdata"));
        assertTrue(table.get(0).mOwner.equals("jriwkmrrgglzdu"));

        /**
         * test for Creating RowData table in Database
         */
        db.createTable();
        table = db.showTable();
        assertTrue(table.size() == 2);
        assertTrue(table.get(1).mSchema.equals("public"));
        assertTrue(table.get(1).mName.equals("tbldata"));
        assertTrue(table.get(1).mOwner.equals("jriwkmrrgglzdu"));

        /**
         * test for inserting a message
         */
        String subject = "CSE216";
        String message = "Software Engineering!";
        int count = db.insertRow(subject, message);
        assertTrue(count == 1);
        int id = 1;
        Database.RowData rowData = db.selectOne(id); // first row
        assertTrue(rowData.mId == 1);
        assertTrue(rowData.mSubject.equals("CSE216"));
        assertTrue(rowData.mMessage.equals("Software Engineering!"));
        assertTrue(rowData.mLikes == 0);

        /**
         * test for inserting a user into UserData table
         */
        String email = "sap716@lehigh.edu";
        String nickname = "MyName";
        int nUser = db.insertUser(email, nickname);
        assertTrue(nUser == 1);
        ArrayList<Database.UserData> userData = db.selectAllUsers();
        assertTrue(userData.size() == 1);
        assertTrue(userData.get(0).mEmail.equals("sap716@lehigh.edu"));
        assertTrue(userData.get(0).mNickname.equals("MyName"));

        /**
         * test for updating an user profile and 'like' vote into a message
         */
        int newUserId = 1;
        int newLikes = 1;
        db.updateUser(id, newUserId);
        db.updateLike(id, newLikes);
        rowData = db.selectOne(id); // first row
        assertTrue(rowData.mLikes == 1);
        assertTrue(rowData.mUserId == 1);

        /**
         * test for updating a nickname
         */
        String newNickname = "NewName";
        db.updateNickname(id, newNickname);
        userData = db.selectAllUsers();
        assertTrue(userData.get(0).mNickname.equals("NewName"));

        /**
         * test for searching a message by using user profile (email)
         */
        ArrayList<Database.RowData> result = db.selectAllByUser(email);
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).mEmail.equals("sap716@lehigh.edu"));
        assertTrue(result.get(0).mNickname.equals("NewName"));
        assertTrue(result.get(0).mSubject.equals("CSE216"));
        assertTrue(result.get(0).mMessage.equals("Software Engineering!"));


        /**
         * test for deleting a message by using user profile (email)
         */
        db.deleteRowByUser(email);
        result = db.selectAll();
        assertTrue(result.size() == 0);

        /**
         * test for deleting an user from UserData
         */
        int user_id = 1;
        db.deleteUser(user_id);
        userData = db.selectAllUsers();
        assertTrue(userData.size() == 0);


        /**
         * test for Dropping RowData table in Database
         */
        db.dropTable();
        table = db.showTable();
        assertTrue(table.size() == 1);

        /**
         * test for Dropping User table in Database
         */
        db.dropUser();
        table = db.showTable();
        assertTrue(table.size() == 0);

        System.out.println("done!");
    }
}
