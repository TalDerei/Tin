# CSE 216: Administrative App
- Semester: Spring 2020
- Student ID: tad222

Admin controls the Postgres database hosted on Heroku for persisting user data. Admin creates PreparedStatements and supports CRUD functionality that's mapped to HTTP requests in the backend. New admin features include: user's biography and seperate likes table. 

## Commands:

### create or drop tables
- [a] Create userdata table
- [b] Create tbldata table
- [c] Create likes table
- [d] Drop likes table
- [e] Drop tbldata table
- [f] Drop userdata table
- [g] Query for all tables

- * table creation and deletion order matters: 
- * create table [a] before [b] before [c] , drop table [d] before [e] before [f]

### get, insert, update, delete tblData (message) [id(integer), subject(string), message(string), user_id(foreign key)]
- [m] Insert a new row into tblData
- [k] Query for all rows from tblData
- [j] Query for row from tblData by id
- [p] Delete a row in tblData
- [u] Update a message in tblData

### get, insert, update, delete userData (user profile) [id(integer), email(string), nickname(string), biography(string)]
- [h] Query for all Users
- [n] Insert a new user in UserData
- [x] Update a nickname in UserData
- [v] Update a user_id in UserData
- [r] Delete a user in UserData

### search by user profile in tblData (message)
- [l] Query for a specific row from tblData by email
- [s] Delete a specific row in tblData by email

### get, insert, update, delete likes (user likes) [user_id(integer), message_id(integer), likes (integer)]
- [i] Query for all likes by user_id
- [o] Insert a like row in likes table
- [w] Update a like/dislikes vote in likes by user_id
- [t] Delete likes by user_id

### other
- [q] Quit program
- [?] Help message]