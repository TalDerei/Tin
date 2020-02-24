# CSE 216 phase 1 (role: admin)
- Semester: Spring 2020
- Student ID: sap716
- Phone Number: 610-570-5116

## Commands

### set up or drop tables
- T: create tblData (message) and UserData (user profile).
- D: drop tblData (message) and UserData (user profile).
- A: show current existing tables.

### insert, list, delete, update tblData (message) [id(int), subject(str), message(str), likes(int), user_id(foreign key)]
- +: insert a row into tblData
- *: list all rows from tblData
- 1: list a specific row from tblData
- -: delete a row from tblData
- ~: update a message in tblData
- l: update a 'like' vote number in a specific row from tblData
- u: update an user_id in a specific row from tblData 

### insert, list, delete, update userData (user profile) [id(int), email(str), nickname(str)]
- i: insert user into UserData
- a: list all users from UserData
- n: update nickname of user from UserData
- m: delete user from UserData

### search by user profile in tblData (message)
- s: list rows in tblData by using an input email
- d: delete rows in tblData by using an input email

### others
- q: quit program
- ?: print usage
