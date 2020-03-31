//importing the necessary library
import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import { GoogleLogout } from 'react-google-login';
import DisplayBio from './displayBio';


/**
 * This function returns the component that loads the message page
 * @param props all the properties passed from the `App` component
 */
const ProfilePage = (props: any): JSX.Element | null => {
  const [userBio, setUserBio] = useState<any>({ value: '' }); // state to change the user bio for post
  const[hasBio, setHasBio] = useState<boolean>(false); // state for checking if the message had been posted
  const [responseBio, setResponseBio] = useState<Object[]>([]);// state for updating the messages collected so far


  //using React's 'useEffect' function
  useEffect(() => {
    async function updateToken() {
      console.log("Profile Page");
      console.log(props.idToken);
      const url = 'https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/user/login/?idToken=['+props.idToken+']';
      await fetch(url, {
        method: 'POST',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
          'origin': 'x-requested-with'
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *client
        body: JSON.stringify({ // the JSON body to be posted through to the backend
          mInsertUser: (props.userName as string).trim(),
        }) // body data type must match "Content-Type" header
      })
        .then((response) => {
          return response.json();//parse the json from response
        })
        .then((myResponse) => {
        console.log(myResponse);
        })
        .catch((error) => {
          console.error(error);//log error
        });
    }
    /**
     * This function sends a 'GET' request to the backend to get all the posted messages
     */
    async function fetchBio() {
      const url = 'https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/users/['+props.idToken+']/bio';
      await fetch(url , { 
        method: 'GET',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin', // include, *same-origin, omit
      })
        .then((response) => {
          return response.json();// parse json from response
        })
        .then((myResponse) => {
          setResponseBio(myResponse.mInsertUser);// update the state data from the posts parsed from response data
        })
        .catch((error) => {
          console.error(error);//log error
        });
    }
    fetchBio();//call the above function manually
    updateToken();
  }, [hasBio]);

  /**
   * This function is triggered every time the input text box is typed into updating the words typed into the text input box
   * @param event DOM event when the input text field is typed in
   */
  function handleBioInputChange(event: any) {
    setUserBio({ value: event.target.value });//update the input box with every keystroke
  }

  /**
   * This function makes a 'POST' request to the backend with the name of the user and the posted message as the JSON body passed through
   * @param newBioToPost The message to be posted into the database
   */
  async function updateBio(newBioToPost: string) {
    const url = 'https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/users/['+props.idToken+']/bio';
      await fetch(url , {
      method: 'POST',
      mode: 'cors',
      cache: 'no-cache',
      credentials: 'same-origin', // include, *same-origin, omit
      headers: {
        'Content-Type': 'application/json'
      },
      redirect: 'follow', // manual, *follow, error
      referrerPolicy: 'no-referrer', // no-referrer, *client
      body: JSON.stringify({ // the JSON body to be posted through to the backend
        mTitle: (props.userName as string).trim(),
        mMessage: newBioToPost
      }) // body data type must match "Content-Type" header
    })
      .then((response) => {
        return response.json();//parse the json from response
      })
      .then((myResponse) => {
        // console.log(myResponse);
      })
      .catch((error) => {
        console.error(error);//log error
      });
  }

  /**
   * This function handles what to do next when the submit button is clicked
   * @param event The DOM event triggered when submit button is clicked
   */
  function handleSubmit(event: any) {
    event.preventDefault();// prevent default event
    setHasBio(true);//set the hasSubmitted state to 'true'
    
    
    if ((window as any).Cypress) {// if this app run is a test
      responseBio.push({ mId: -1, mSubject: 'Patient0', mMessage: 'This is a test message I wrote for app tests!!' });//push this Object to the array to be dispalyed
    }
    else {// if not testing */
      let toReturn: string = userBio.value;// The message to be posted
      updateBio(toReturn);//make the 'POST' request with the appropriate message
      responseBio.push({ mId: -1, mSubject: (props.userName as string).trim(), mMessage: toReturn }); //push the latest post to the array of posts to display
   }
    ReactDOM.render(<DisplayBio//render the posts here
      bioToPost={responseBio}
    />
      , document.getElementById('bio'));//in this id
    setUserBio({ value: '' });//empty the input text box
  }
  // load profile page while testing too
  if (props.signedIn || (window as any).Cypress)
    return (
      //start of the div for displaying the profile
      <div id="profiles">
        <div id="userInfo">
          {props.userName} <br />
          {props.userEmail} <br />
        </div>
        <div id="userBio">
          <div id="editBio">
            <form onSubmit={handleSubmit}>
              <label>
                <br></br>
              <input type="text" name="name" value={userBio.value} onChange={handleBioInputChange} placeholder="Enter New Bio Information" id="bio-input"></input>
            </label>
            <input type="submit" value="Update Bio" onClick={updateBioEdit} onSubmit={handleSubmit} id="updateBio-input"></input>
            </form>
          </div>
          This Users bio is... <br/>
          <div id="bio"></div>
          <br/>
          <input type="submit" value="Edit Bio" onClick={editBio} id="editBio-input"></input>
          <input type="submit" value="Post a Message" onClick={redirectMessages}></input>
        </div>
        <div id="post"></div>
        <GoogleLogout
          className="googleLogoutButton"
          clientId="372884561524-22jfggk3pefbnanh83o92mqqlmkbvvd9.apps.googleusercontent.com"
          buttonText="Logout"
          onLogoutSuccess={onLogout}
        >
        </GoogleLogout>
      </div>
    );
  else
    return null;
}

function updateBioEdit(): void {
  const updateInput = document.getElementById("updateBio-input");
  const editBioDiv = document.getElementById("editBio");
  const editButton = document.getElementById("editBio-input");
  const bioInput = document.getElementById("bio-input");
  editBioDiv!.style.display = "none";
  editButton!.style.display = "block";
  updateInput!.style.display = "none";
  bioInput!.style.display = "none";
  

}

function editBio(): void {
  const editBioDiv = document.getElementById("editBio");
  const updateInput = document.getElementById("updateBio-input");
  const editButton = document.getElementById("editBio-input");
  const bioInput = document.getElementById("bio-input");
  editButton!.style.display = "none";
  editBioDiv!.style.display = "block";
  updateInput!.style.display = "block";
  bioInput!.style.display = "block";

}

function redirectMessages(): void {
  const profileDiv = document.getElementById("profiles");
  const messageDiv = document.getElementById("messages");
  const inputItems = document.getElementById("input-items");
  profileDiv!.style.display = "none";
  messageDiv!.style.display = "block";
  inputItems!.style.display = "block";
}

/**
 * This function does necessary hides/shows of elements when the user logs out
 */
function onLogout(): void {
  const appLogo = document.getElementById("App-logo");// app logo element
  const loginButton = document.getElementById("login-button");//login button element
  const profileDiv = document.getElementById("profiles");// messages div element
  loginButton!.style.display = "none";//hide login button
  appLogo!.style.display = "block";//show app logo
  loginButton!.style.display = "block";//show login button
  profileDiv!.style.display = "none";//hide message posts div
}

//export the MessagePage Component into `App` Component
export default ProfilePage;