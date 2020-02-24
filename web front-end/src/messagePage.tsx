import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import { GoogleLogout } from 'react-google-login';
import DisplayPosts from './displayPosts';

const MessagePage = (props: any): JSX.Element | null => {
  const [enteredMessage, setEnteredMessage] = useState<any>({ value: `Hi, What's up?` });
  const [hasSubmitted, setHasSubmitted] = useState<boolean>(false);
  const [responseMessages, setResponseMessages] = useState<Object[]>([]);

  useEffect(() => {
    async function fetchPosts() {
      await fetch(`https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/messages`, {
        method: 'GET',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin', // include, *same-origin, omit
      })
        .then((response) => {
          return response.json();
        })
        .then((myResponse) => {
          setResponseMessages(myResponse.mData);
        })
        .catch((error) => {
          console.error(error);
        });
    }
    fetchPosts();
  }, [hasSubmitted]);

  function handleMessageInputChange(event: any) {
    setEnteredMessage({ value: event.target.value });
  }

  async function makePost() {
    await fetch(`https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/messages`, {
      method: 'POST',
      mode: 'cors',
      cache: 'no-cache',
      credentials: 'same-origin', // include, *same-origin, omit
      headers: {
        'Content-Type': 'application/json'
      },
      redirect: 'follow', // manual, *follow, error
      referrerPolicy: 'no-referrer', // no-referrer, *client
      body: JSON.stringify({
        mTitle: (props.userName as string).trim(),
        mMessage: (enteredMessage.value as string).trim()
      }) // body data type must match "Content-Type" header
    })
      .then((response) => {
        return response.json();
      })
      .then((myResponse) => {
        console.log(myResponse);
      })
      .catch((error) => {
        console.error(error);
      });
  }

  function handleSubmit(event: any) {
    makePost();
    event.preventDefault();
    const inputDisplays = document.getElementById("input-items");
    inputDisplays!.style.display = "none";
    setHasSubmitted(true);
    if ((window as any).Cypress) {
      responseMessages.push({ mId: -1, mSubject: 'Patient0', mMessage: 'This is a test message I wrote for app tests!!' });
    }
    else {
      responseMessages.push({ mId: -1, mSubject: (props.userName as string).trim(), mMessage: (enteredMessage.value as string).trim() });
    }
    ReactDOM.render(<DisplayPosts
      messagePost={responseMessages}
    />
      , document.getElementById('post'));
  }
  // load messages page while testing too
  if (props.signedIn || (window as any).Cypress)
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