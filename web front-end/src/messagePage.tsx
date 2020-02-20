import React, { useState } from 'react';
import ReactDOM from 'react-dom';
import { GoogleLogout } from 'react-google-login';
import Display from './Display.jpg';


const MessagePage = (props: any): JSX.Element | null => {
  const [enteredMessage, setEnteredMessage] = useState<any>({ value: `Hi, What's up?` });

  function handleMessageInputChange(event: any) {
    setEnteredMessage({ value: event.target.value });
  }

  function handleSubmit(event: any) {
    event.preventDefault();
    (props.messagesArray as Array<Object>).push({ name: props.userName, message: enteredMessage.value, upVote: 0, downVote: 0 });
    console.log(props.messagesArray);
    ReactDOM.render(<p>
      <img src={Display} className="Display-picture" alt="dp" id="Display-picture" width="100" height="100" />
      {(props.messagesArray)[0].message}
    </p>
      , document.getElementById('gibberish'));
  }
  if (props.signedIn)
    return (<div id="messages">
      {props.userName} <br />
      <form onSubmit={handleSubmit}>
        <label>
          Enter the message:
        <input type="text" name="name" value={enteredMessage.value} onChange={handleMessageInputChange} />
        </label>
        <input type="submit" value="Submit" />
      </form>
      <GoogleLogout
        className="googleLogoutButton"
        clientId="372884561524-22jfggk3pefbnanh83o92mqqlmkbvvd9.apps.googleusercontent.com"
        buttonText="Logout"
        onLogoutSuccess={onLogout}
      >
      </GoogleLogout>
      <div id="gibberish"></div>
    </div>
    );
  else
    return null;
}

function onLogout(): void {
  const appLogo = document.getElementById("App-logo");
  const loginButton = document.getElementById("login-button");
  const messageDiv = document.getElementById("messages");
  loginButton!.style.display = "none";
  appLogo!.style.display = "block";
  loginButton!.style.display = "block";
  messageDiv!.style.display = "none";
}

export default MessagePage;