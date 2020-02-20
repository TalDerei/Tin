import React, { useState } from 'react';
import Buzz from './Buzz.png'
import './App.css';
import GoogleLogin from 'react-google-login';
import MessagePage from './messagePage';

const App = () => {
  const messagesArray = new Array<Object>();
  const [signedIn, setSignedIn] = useState<boolean>(false);
  const [userName, setUserName] = useState<string>('Patient0');

  const responseGoogle = (response: any) => {
    console.log(response.details);
    const user = response.Qt.Ad;
    setUserName(user);
    response.Ca ? setSignedIn(true) : setSignedIn(false);
    const appLogo = document.getElementById("App-logo");
    const messageDiv = document.getElementById("messages");
    const loginButton = document.getElementById("login-button");
    const inputDisplays = document.getElementById("input-items");
    loginButton!.style.display = "none";
    if (appLogo!.style.display === "none") {
      appLogo!.style.display = "block";
      inputDisplays!.style.display = "none";
      messageDiv!.style.display = "none";
    } else {
      messageDiv!.style.display = "block";
      inputDisplays!.style.display = "block";
      appLogo!.style.display = "none";
    }
  }

  return (
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
            messagesArray={messagesArray}
          />
        </div>
      </header>
    </div>
  );
}

export default App;
