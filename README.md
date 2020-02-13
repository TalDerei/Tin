# CSE216 Team Tin Repository
This is the repository where all the work for "Team Tin" from CSE 216 Spring 2020 will be committed into.

## Front End
Front end set up in TypeScript with the React library.

To get it running, go into thhe `web front-end` directory and run `npm install`.
After that run `npm start` from the same directory to start the front end which
will directly open the app in browser in [http://localhost:3000/](http://localhost:3000/)

The good thing about how things are set up is that, you can edit your code in 
`App.tsx` and the browser will do a `live update` and you will see your change 
right away when you save the code in `App.tsx`.

## Back End
Back end set up in python with Flask.

To run the backend, the process might be more involved. You should have python,
pip, virtualenv installed. Things might be better if you are running a bash from
your mac or windows (wsl bash).

First, delete your whole `backend` directory. From the `cse216` directory, run
`virtualenv backend`. This will make a folder named backend. After this run
`git pull origin short-term-project`. This will get all the backend code you previously
deleted back to your local branch.

Now go into the backend directory with `cd backend` and run `source bin/activate`.
This will run a python virtuall shell. From there do run
```
pip install -U Flask
pip install -U flask-cors
```
These are the dependencies we are gonna need for backend. After this go into the 
server directory with `cd server` and run `python server.py` to start the server
for backend in [http://localhost:5000/](http://localhost:5000/). You can edit 
`server.py` to edit your backend code. To stop the server run `Ctrl + C` like 
any other program. To get out of the python shell run `deactivate`.

# Contributors
- Anmol Shrestha <ans221@lehigh.edu>
- Hailey Goldschmidt <hag322@lehigh.edu>
- Sang-jun Park <sap716@lehigh.edu>
- Tal Derei <tad222@lehigh.edu>
- Tori Dorn <vld222@lehigh.edu>
- Andrew Johnson <agj221@lehigh.edu> (TA)

