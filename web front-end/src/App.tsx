import React, { useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';
import GoogleLogin from 'react-google-login';

const App = () => {

  const [signedIn, setSignedIn] = useState<boolean>(false);
  const [userName, setUserName] = useState<string>('Patient0');

  const responseGoogle = (response: any) => {
    // console.log(response);
    const user = response.Qt.Ad;
    setUserName(user);
    response.Ca ? setSignedIn(true) : setSignedIn(false);
    const appLogo = document.getElementById("App-logo");
    const loginButton = document.getElementById("login-button");
    loginButton!.style.display = "none";
    if (appLogo!.style.display === "none") {
      appLogo!.style.display = "block";
    } else {
      appLogo!.style.display = "none";
    }
  }

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" id="App-logo" />
        <div id="login-button">
          <GoogleLogin
            className="googleLoginButton"
            clientId="131496045117-i2iui8ssoajm8dlcvhenkcf64h4ieb0k.apps.googleusercontent.com"
            buttonText="Login"
            onSuccess={responseGoogle}
            onFailure={responseGoogle}
            cookiePolicy={'single_host_origin'}
          />
        </div>
      </header>
    </div>
  );
}

export default App;
