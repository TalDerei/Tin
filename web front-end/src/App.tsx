// importing all the required functions/libraries
import React, { useState } from 'react';
import Buzz from './Buzz.png'
import './App.css';
import GoogleLogin from 'react-google-login';
import MessagePage from './messagePage';
import ProfilePage from './profilePage';

/**
 * This function is the root of all the web app functionality. This React Component gets loaded into `index.html`
 */
const App = () => {
  const [signedIn, setSignedIn] = useState<boolean>(false); // state for checking if user is signed in yet
  const [userName, setUserName] = useState<string>('Patient0'); // state for updating what the user name is
  const [userEmail, setUserEmail] = useState<string>();
  const [idToken, setIdToken] = useState<String>();
  const [accessToken, setAccessToken] = useState<String>();


  /**
   * Function that lets user go to messages page depending on what the google api login response is
   * @param response The response gotten by the google api when sign in is attempted
   */
  const responseGoogle = (response: any) => {
    console.log(response);
    const user = response.Pt.Ad; // parse the user name from the response
    const email = response.profileObj.email;
    const tokenId = response.tokenId;
    const accessToken = response.accessToken;
    setUserName(user); // set the user name
    setUserEmail(email);
    setIdToken(tokenId);
    setAccessToken(accessToken);

    response.Ca ? setSignedIn(true) : setSignedIn(false); // is the user successfully signed in? If yes change the state of the `signedIn` boolean accordingly
    const appLogo = document.getElementById("App-logo");// applogo element
    const messageDiv = document.getElementById("messages");// messages div element
    const loginButton = document.getElementById("login-button");// login button element
    const inputDisplays = document.getElementById("input-items");// input text element
    const profileDiv = document.getElementById("profiles");
    const updateButton = document.getElementById("updateBio-input");
    const bioEditor = document.getElementById("bio-input");
    updateButton!.style.display = "none";
    bioEditor!.style.display = "none";
    profileDiv!.style.display = "none";
    loginButton!.style.display = "none";// make the login button invisible
    // This is to make the page dynamic and hide and show different elemnts
    if (appLogo!.style.display === "none") { // if the app logo is invisible
      console.log("test if");
      appLogo!.style.display = "block";// make app logo visible
      inputDisplays!.style.display = "none";// make message input invisible
      messageDiv!.style.display = "none";// make whole messages div invisible
      profileDiv!.style.display = "none";
    } else {// if app logo is visible
      console.log("test else");
      profileDiv!.style.display = "block";
      messageDiv!.style.display = "none"; // make messages div visible
      inputDisplays!.style.display = "none";// make input displays visible
      appLogo!.style.display = "none";// make app logo invisible
    }
  }

  return (
    //make app div for root
    <div className="App">
      <header className="App-header">
        <img src={Buzz} className="App-logo" alt="logo" id="App-logo" />
        <div id="login-button">
          <GoogleLogin
            className="googleLoginButton"
            clientId="372884561524-22jfggk3pefbnanh83o92mqqlmkbvvd9.apps.googleusercontent.com" // localhost
            // clientId="131496045117-k91913gk3j5li0i9k4ov52vg187j56hu.apps.googleusercontent.com" // for heroku
            buttonText="Login"
            onSuccess={responseGoogle}
            onFailure={responseGoogle}
            cookiePolicy={'single_host_origin'}
          />
        </div>
        <div id="messagePage" >
          <MessagePage
            signedIn={signedIn}
            userName={userName}
            accessToken={accessToken}
          />
        </div>
        <div id="profilePage" >
          <ProfilePage
            signedIn={signedIn}
            userName={userName}
            userEmail={userEmail}
            idToken={idToken}
            />
        </div>
      </header>
    </div>
  );
}

//export the App Component to `index.html`
export default App;
