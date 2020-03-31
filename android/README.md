# CSE216 Team Tin Repository
This is the repository where all the work for "Team Tin" from CSE 216 Spring 2020 will be committed into.

# Contributors
- Sang-jun Park <sap716@lehigh.edu> (android)
- Anmol Shrestha <ans221@lehigh.edu>
- Hailey Goldschmidt <hag322@lehigh.edu>
- Tal Derei <tad222@lehigh.edu>
- Tori Dorn <vld222@lehigh.edu>
- Andrew Johnson <agj221@lehigh.edu> (TA)

## main update 
1) LoginActivity
- implementation of Login with GoogleApiClient and OAuth2.0.

2) MainActivity
- Message board that is consist of profile icon, user name, and comments.
- Able to sending a message by Post request.
- Can go to Vote activity by clicking comments, but cannot interact with backend.
- Likes vote have not implemented yet.

3) ProfileActivity
- Profile page is poped up by clicking of profile icon on message board.
- Display all messages written by selected users.

4) UnitTest
- Done by Espresso JUnit test (filename: LoginActityTest).
