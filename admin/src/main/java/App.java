import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map; //java.util.Map interface represents a mapping between a key and a value
import java.util.ArrayList;

import java.util.Properties;
import java.io.InputStreamReader;
import java.io.InputStream;

/**
 * App is our basic admin app.  For now, it is a demonstration of the six key
 * operations on a database: connect, insert, update, query, delete, disconnect
 */
public class App {
    /**
     * Print the menu for our program
     */
    static void menu() {
        System.out.println("Main Menu");
        System.out.println("  [a] Create userdata table");
        System.out.println("  [b] Create tbldata table");
        System.out.println("  [c] Create likes table");
        System.out.println("  [d] Drop likes table");
        System.out.println("  [e] Drop tbldata table");
        System.out.println("  [f] Drop userdata table");
        System.out.println("  [g] Query for all tables");
        System.out.println("  [h] Query for all Users");
        System.out.println("  [i] Query for all likes");
        System.out.println("  [j] Query for row from tblData by id");
        System.out.println("  [k] Query for all rows from tblData");
        System.out.println("  [l] Query for a specific email from tblData");
        System.out.println("  [m] Insert a new row in tblData");
        System.out.println("  [n] Insert a new user in UserData");
        System.out.println("  [o] Insert a new likes row in likes");
        System.out.println("  [p] Delete a row in tblData");
        System.out.println("  [r] Delete a user in UserData");
        System.out.println("  [s] Delete a row in tblData by email");
        System.out.println("  [t] Delete a likes row");
        System.out.println("  [u] Update a row in tblData");
        System.out.println("  [v] Update a user in UserData");
        System.out.println("  [w] Update a like vote in tblData");
        System.out.println("  [x] Update a nickname in UserData");
        System.out.println("  [y] Create google files table");
        System.out.println("  [z] drop google files table");
        System.out.println("  [q] Quit Program");
        System.out.println("  [1] Query for row in files table by fileId");
        System.out.println("  [2] delete file in files table by fileId");
        System.out.println("  [3] Query for all flagged messages");
        System.out.println("  [4] delete a flagged message");
        System.out.println("  [5] Set the flag of a message");
        System.out.println("  [?] Help (this message)");
    }

    /**
     * Ask the user to enter a menu option; repeat until we get a valid option
     *
     * @param in A BufferedReader, for reading from the keyboard
     *
     * @return The character corresponding to the chosen menu option
     */
    static char prompt(BufferedReader in) {
        // The valid actions:
        String actions = "abcdefghijklmnopqrstuvwxyzq?1234";

        // We repeat until a valid single-character option is selected
        while (true) {
            System.out.print("[" + actions + "] :> ");
            String action;
            try {
                action = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (action.length() != 1)
                continue;
            if (actions.contains(action)) {
                return action.charAt(0);
            }
            System.out.println("Invalid Command");
        }
    }

    /**
     * Ask the user to enter a String message
     *
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     *
     * @return The string that the user provided.  May be "".
     */
    static String getString(BufferedReader in, String message) {
        String s;
        try {
            System.out.print(message + " :> ");
            s = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return s;
    }

    /**
     * Ask the user to enter an integer
     *
     * @param in A BufferedReader, for reading from the keyboard
     * @param message A message to display when asking for input
     *
     * @return The integer that the user provided.  On error, it will be -1
     */
    static int getInt(BufferedReader in, String message) {
        int i = -1;
        try {
            System.out.print(message + " :> ");
            i = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    static boolean getBoolean(BufferedReader in, String message) {
        boolean flag = false;
        try {
            System.out.print(message + " :> ");
            flag = Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * The main routine runs a loop that gets a request from the user and
     * processes it
     *
     * @param argv Command-line options.  Ignored by this program.
     */
    public static void main(String[] argv) {
        // get the Postgres configuration from the property files
        Properties prop = new Properties();
        String config = "backend.properties";
        try {
            InputStream input = App.class.getClassLoader().getResourceAsStream(config);
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String db_url = prop.getProperty("DATABASE_URL");

        // Get a fully-configured connection to the database, or exit
        // immediately
        Database db = Database.getDatabaseFromUri(db_url);
        if (db == null) {
            System.out.println("db is null!");
            return;
        }

        // Start our basic command-line interpreter:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            char action = prompt(in);
            if (action == '?') {
                menu();
            } else if (action == 'q') {
                break;
            } else if (action == 'b') {
                db.createTable();
            } else if (action == 'a') {
                db.createUser();
            } else if (action == 'c') {
                db.createLikes();
            } else if (action == 'y') {
                db.createFiles();
            } else if (action == 'e') {
                db.dropTable();
            } else if (action == 'f') {
                db.dropUser();
            } else if (action == 'd') {
                db.dropLikes();
            } else if (action == 'z') {
                db.dropFiles();
            } else if (action == 'g') {
                ArrayList<Database.Table> table = db.showTable();
                if (table == null)
                    continue;
                System.out.println("  Current Database Tables");
                System.out.println("  -------------------------");
                for (Database.Table rd : table ) {
                    System.out.println(rd.mSchema + '|' + rd.mName + '|' + rd.mOwner);
                }
            } else if (action == 'j') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                Database.RowData res = db.selectOne(id);
                if (res != null) {
                    System.out.println("  [" + res.mId + "] " + res.mSubject);
                    System.out.println("--> [message] " + res.mMessage);
                    System.out.println("--> [userID] " + res.mUserId);
                }
            } else if (action == '1') {
                int id = getInt(in, "Enter row ID");
                if (id == -1) 
                    continue;
                Database.GoogleDriveContent res = db.selectOneFile(id);
                if (res != null) {
                    System.out.println("--> [fileId] " + res.mFileId);
                    System.out.println("--> [messageId] " + res.mMessageId);
                    System.out.println("--> [fileSize] " + res.mFileSize);
                    System.out.println("--> [url] " + res.mURL);
                }
            } else if (action == 'i') {
                int id = getInt(in, "Enter the row ID");
                int res = db.getLikeTotal(id);
                System.out.println(res + " total likes");
            } else if (action == 'k') {
                ArrayList<Database.RowData> res = db.selectAll();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + rd.mSubject);
                }
            } else if(action == '3') {
                ArrayList<Database.RowData> res = db.selectAllFlaggedMessages();
                if (res == null)
                    continue;
                System.out.println("     Flagged Messages     ");
                System.out.println(" -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + rd.mSubject);
                }
            } else if(action == '4') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteFlaggedMessage(id);
                if (res == -1) {
                    continue;
                } else if(res == 0) {
                    System.out.println(" " + id + " was not flagged");
                }
                System.out.println("  " + res + " rows deleted");
            } else if(action == '5') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                boolean newFlag = getBoolean(in, "Set message flag :> ");
                int res = db.setMessageFlag(id, newFlag);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");  
            } else if (action == 'p') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteRow(id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == '2') {
                int id = getInt(in, "Enter the fileID");
                if (id == -1) 
                    continue;
                int res = db.deleteFile(id);
                if (res == -1)
                    continue;
                System.out.println(" " + res + " rows deleted");
            } else if (action == 't') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1) {
                    continue;
                }
                int res = db.deleteLike(id);
                if (res == -1) {
                    continue;
                }
                System.out.println(" " + res + " rows deleted");
            } else if (action == 'm') {
                String subject = getString(in, "Enter the subject");
                String message = getString(in, "Enter the message");
                //int likes = 0;
                if (subject.equals("") || message.equals(""))
                    continue;
                int res = db.insertRow(subject, message);
                System.out.println(res + " rows added");
            } else if (action == 'o') {
                int userID = getInt(in, "enter the userID");
                int messageID = getInt(in, "enter the messsageID");
                //int likes = 0;
                int res = db.insertOneLike(userID, messageID);
                System.out.println(res + " rows added");
            } else if (action == 'u') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateOne(id, newMessage);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'n') {
                String email = getString(in, "Enter the email");
                String nickname = getString(in, "Enter the nickname");
                String biography = getString(in, "Enter your biography");
                if (email.equals("") || nickname.equals("")) continue;
                int res = db.insertUser(email, nickname, biography);
                System.out.println(res + " rows added");
            } else if (action == 'v') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                int newUserID = getInt(in, "Enter the user_id");
                int res = db.updateUser(id, newUserID);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'x') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newNickname = getString(in, "Enter the new nickname");
                int res = db.updateNickname(id, newNickname);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'h') {
                ArrayList<Database.UserData> res = db.selectAllUsers();
                if (res == null)
                    continue;
                System.out.println("  Current Users ");
                System.out.println("  -------------------------");
                for (Database.UserData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + " [email] " + rd.mEmail + " [nickname] " + rd.mNickname + " [biography] " + rd.mBiography);
                }
            } else if (action == 'l') {
                String email = getString(in, "Enter the email");
                ArrayList<Database.RowData> res = db.selectAllByUser(email);
                System.out.println("  Current Database Contents by User");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("[subject] " + rd.mSubject + " [message] " + rd.mMessage + " [nickname] " + rd.mNickname);
                }
            } else if (action == 's') {
                String email = getString(in, "Enter the email");
                if (email.equals(""))
                    continue;
                int res = db.deleteRowByUser(email);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == 'w') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                int newLikes = getInt(in, "Enter the likes");
                int res = db.updateLike(id, newLikes);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'r') {
                int user_id = getInt(in, "Enter the User ID :> ");
                int res = db.deleteUser(user_id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " row deleted");
            } else {
                System.out.println("Unrecognized Command");
            }
        }
        // Always remember to disconnect from the database when the program exits
        db.disconnect();
    }
}

