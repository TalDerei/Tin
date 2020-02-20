import React, { useState } from 'react';
import ReactDOM from 'react-dom';
import { GoogleLogout } from 'react-google-login';
import DisplayPosts from './displayPosts';


const MessagePage = (props: any): JSX.Element | null => {
  const [enteredMessage, setEnteredMessage] = useState<any>({ value: `Hi, What's up?` });

  function handleMessageInputChange(event: any) {
    setEnteredMessage({ value: event.target.value });
  }

  function handleSubmit(event: any) {
    event.preventDefault();
    (props.messagesArray as Array<Object>).push({ name: props.userName, message: enteredMessage.value, upVote: 0, downVote: 0 });
    // console.log(props.messagesArray);
    const inputDisplays = document.getElementById("input-items");
    inputDisplays!.style.display = "none";

    ReactDOM.render(<DisplayPosts
      messagesArray={props.messagesArray}
    />
      , document.getElementById('posts'));
  }
  if (props.signedIn)
    return (<div id="messages">
      <div id="input-items">
        {props.userName} <br />
        <form onSubmit={handleSubmit}>
          <label>
            What would you like to post today?<br></br>
            <input type="text" name="name" value={enteredMessage.value} onChange={handleMessageInputChange} />
          </label>
          <input type="submit" value="Post" />
        </form>
      </div>
      <div id="posts"></div>
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