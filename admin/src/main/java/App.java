import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Properties;

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
        System.out.println("  [T] Create tbldata table");
        System.out.println("  [Z] Create userdata table");
        System.out.println("  [X] Create likes table");
        System.out.println("  [D] Drop tbldata table");
        System.out.println("  [Y] Drop userdata table");
        System.out.println("  [J] Drop likes table");
        System.out.println("  [P] delete likes row");
        System.out.println("  [Q] insert likes row");
        System.out.println("  [W] get total likes");
        System.out.println("  [A] Query for all tables");
        System.out.println("  [a] Query for all Users");
        System.out.println("  [1] Query for row from tblData by id");
        System.out.println("  [*] Query for all rows from tblData");
        System.out.println("  [s] Query for a specific email from tblData");
        System.out.println("  [-] Delete a row in tblData");
        System.out.println("  [m] Delete a user in UserData");
        System.out.println("  [d] Delete a row in tblData by email");
        System.out.println("  [+] Insert a new row in tblData");
        System.out.println("  [i] Insert a new User on UserData");
        System.out.println("  [~] Update a row in tblData");
        System.out.println("  [u] Update a user in UserData");
        System.out.println("  [l] Update a like vote in tblData");
        System.out.println("  [n] Update a nickname in UserData");
        System.out.println("  [q] Quit Program");
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
        String actions = "TDAZXYJPQa1*s-md+i~ulnq?W";

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
            } else if (action == 'T') {
                db.createTable();
            } else if (action == 'Z') {
                db.createUser();
            } else if (action == 'X') {
                db.createLikes();
            } else if (action == 'D') {
                db.dropTable();
            } else if (action == 'Y') {
                db.dropUser();
            } else if (action == 'J') {
                db.dropLikes();
            } else if (action == 'A') {
                ArrayList<Database.Table> table = db.showTable();
                if (table == null)
                    continue;
                System.out.println("  Current Database Tables");
                System.out.println("  -------------------------");
                for (Database.Table rd : table ) {
                    System.out.println(rd.mSchema + '|' + rd.mName + '|' + rd.mOwner);
                }
            } else if (action == '1') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                Database.RowData res = db.selectOne(id);
                if (res != null) {
                    System.out.println("  [" + res.mId + "] " + res.mSubject);
                    System.out.println("--> [message] " + res.mMessage);
                    System.out.println("--> [userID] " + res.mUserId);
                }
            } else if (action == 'W') {
                int id = getInt(in, "Enter the row ID");
                int res = db.getLikeTotal(id);
                System.out.println(res + " total likes");
            } else if (action == 'Q') {
                int userID = getInt(in, "enter the userID");
                int messageID = getInt(in, "enter the messsageID");
                //int likes = 0;
                int res = db.insertOneLike(userID, messageID);
                System.out.println(res + " rows added");
            } else if (action == '*') {
                ArrayList<Database.RowData> res = db.selectAll();
                if (res == null)
                    continue;
                System.out.println("  Current Database Contents");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + rd.mSubject);
                }
            } else if (action == '-') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1)
                    continue;
                int res = db.deleteRow(id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == 'P') {
                int id = getInt(in, "Enter the row ID");
                if (id == -1) {
                    continue;
                }
                int res = db.deleteLike(id);
                if (res == -1) {
                    continue;
                }
                System.out.println(" " + res + " rows deleted");
            } else if (action == '+') {
                String subject = getString(in, "Enter the subject");
                String message = getString(in, "Enter the message");
                //int likes = 0;
                if (subject.equals("") || message.equals(""))
                    continue;
                int res = db.insertRow(subject, message);
                System.out.println(res + " rows added");
            } else if (action == 'Q') {
                int userID = getInt(in, "enter the userID");
                int messageID = getInt(in, "enter the messsageID");
                //int likes = 0;
                int res = db.insertOneLike(userID, messageID);
                System.out.println(res + " rows added");
            } else if (action == '~') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newMessage = getString(in, "Enter the new message");
                int res = db.updateOne(id, newMessage);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'i') {
                String email = getString(in, "Enter the email");
                String nickname = getString(in, "Enter the nickname");
                String biography = getString(in, "Enter your biography");
                if (email.equals("") || nickname.equals("")) continue;
                int res = db.insertUser(email, nickname, biography);
                System.out.println(res + " rows added");
            } else if (action == 'u') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                int newUserID = getInt(in, "Enter the user_id");
                int res = db.updateUser(id, newUserID);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'n') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                String newNickname = getString(in, "Enter the new nickname");
                int res = db.updateNickname(id, newNickname);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'a') {
                ArrayList<Database.UserData> res = db.selectAllUsers();
                if (res == null)
                    continue;
                System.out.println("  Current Users ");
                System.out.println("  -------------------------");
                for (Database.UserData rd : res) {
                    System.out.println("  [" + rd.mId + "] " + " [email] " + rd.mEmail + " [nickname] " + rd.mNickname);
                }
            } else if (action == 's') {
                String email = getString(in, "Enter the email");
                ArrayList<Database.RowData> res = db.selectAllByUser(email);
                System.out.println("  Current Database Contents by User");
                System.out.println("  -------------------------");
                for (Database.RowData rd : res) {
                    System.out.println("[subject] " + rd.mSubject + " [message] " + rd.mMessage + " [nickname] " + rd.mNickname);
                }
            } else if (action == 'd') {
                String email = getString(in, "Enter the email");
                if (email.equals(""))
                    continue;
                int res = db.deleteRowByUser(email);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows deleted");
            } else if (action == 'l') {
                int id = getInt(in, "Enter the row ID :> ");
                if (id == -1)
                    continue;
                int newLikes = getInt(in, "Enter the likes");
                int res = db.updateLike(id, newLikes);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            } else if (action == 'm') {
                int user_id = getInt(in, "Enter the User ID :> ");
                int res = db.deleteUser(user_id);
                if (res == -1)
                    continue;
                System.out.println("  " + res + " rows updated");
            }
        }
        // Always remember to disconnect from the database when the program exits
        db.disconnect();
    }
}

