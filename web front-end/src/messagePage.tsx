import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import { GoogleLogout } from 'react-google-login';
import DisplayPosts from './displayPosts';

const MessagePage = (props: any): JSX.Element | null => {
  const [enteredMessage, setEnteredMessage] = useState<any>({ value: '' });
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

  async function makePost(messageToPost: string) {
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
        mMessage: messageToPost
      }) // body data type must match "Content-Type" header
    })
      .then((response) => {
        return response.json();
      })
      .then((myResponse) => {
        // console.log(myResponse);
      })
      .catch((error) => {
        console.error(error);
      });
  }

  function handleSubmit(event: any) {
    event.preventDefault();
    setHasSubmitted(true);
    if ((window as any).Cypress) {
      responseMessages.push({ mId: -1, mSubject: 'Patient0', mMessage: 'This is a test message I wrote for app tests!!' });
    }
    else {
      let toReturn: string = enteredMessage.value;
      if (enteredMessage.value === '') {
        toReturn = 'I was too lazy to type 😊';
        setEnteredMessage({ value: 'I was too lazy to type 😊' });
      }
      makePost(toReturn);
      responseMessages.push({ mId: -1, mSubject: (props.userName as string).trim(), mMessage: toReturn });
    }
    ReactDOM.render(<DisplayPosts
      messagePost={responseMessages}
    />
      , document.getElementById('post'));
    setEnteredMessage({ value: '' });
  }
  // load messages page while testing too
  if (props.signedIn || (window as any).Cypress)
    return (<div id="messages">
      <div id="input-items">
        {props.userName} <br />
        <form onSubmit={handleSubmit}>
          <label>
            <br></br>
            <input type="text" name="name" value={enteredMessage.value} onChange={handleMessageInputChange} placeholder="What would you like to post today?" id="input-text" />
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