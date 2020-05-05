//import all required functions and libraries
import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom';
import { GoogleLogout } from 'react-google-login';
import DisplayPosts from './displayPosts';

/**
 * This function returns the component that loads the message page
 * @param props all the properties passed from the `App` component
 */
const MessagePage = (props: any): JSX.Element | null => {
  const [enteredMessage, setEnteredMessage] = useState<any>({ value: '' }); // state to change the typed message for post
  const [enteredLink, setEnteredLink] = useState<any>({ value:'' }); // state to change the typed hyperlink for post
  const [enteredFile, setEnteredFile] = useState({file:null});
  const [enteredFileBase64, setEnteredFileBase64] = useState("");
  const [hasSubmitted, setHasSubmitted] = useState<boolean>(false);// state for checking if the message had been posted
  const [responseMessages, setResponseMessages] = useState<Object[]>([]);// state for updating the messages collected so far
  const [responseFiles, setResponseFiles] = useState<Object[]>([]);// state for updating the messages collected so far
  const herokuUrl = "https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/";
  //const herokuUrl = "http://localhost:4567/";



  //using React's 'useEffect' function
  useEffect(() => {
    /**
     * This function sends a 'GET' request to the backend to get all the posted messages
     */
    async function fetchPosts() {
      await fetch(herokuUrl + "/messages", {
        method: 'GET',
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin', // include, *same-origin, omit
      })
        .then((response) => {
          return response.json();// parse json from response
        })
        .then((myResponse) => {
          setResponseMessages(myResponse.mData);// update the state data from the posts parsed from response data
        })
        .catch((error) => {
          console.error(error);//log error
        });
    }
    fetchPosts();//call the above function manually
  }, [hasSubmitted]);


  /**
   * This function is triggered every time the input text box is typed into updating the words typed into the text input box
   * @param event DOM event when the input text field is typed in
   */
  function handleMessageInputChange(event: any) {
    setEnteredMessage({ value: event.target.value });//update the input box with every keystroke
  }

  /**
   * This function is almost same function with handleMessageInputChange, but for hyperlink.
   * @param event DOM event when the input text field is typed in
   */
  function handleLinkInputChange(event: any) {
    setEnteredLink({ value: event.target.value });//update the input box with every keystroke
  }

  /**
   * This function is for uploading file object
   * @param event DOM event to upload a file.
   * @return enteredFileBase64, enteredFileName, enteredFile
   */
  function handleFileInputChange(event: any) {
    setEnteredFile({file:event.target.files[0]});
    let reader = new FileReader();
    reader.readAsDataURL(event.target.files[0]);
    reader.onload = function(event) {
      let base64:any = reader.result;
      if (base64 == null)
        base64 = "None";
      setEnteredFileBase64(base64);
      console.log("Base64");
      console.log(base64);
    }
  }


  /**
   * This function makes a 'POST' request to the backend with the name of the user and the posted message as the JSON body passed through
   * @param messageToPost The message to be posted into the database
   */
  async function makePost(messageToPost: string) {
    await fetch(herokuUrl + `messages`, {
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
        mMessage: messageToPost
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

  async function makeRegistration(messageToPost: string) {
    await fetch(herokuUrl + `/usrs/login`, {
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
        mMessage: messageToPost,
        idToken: idToken
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
   * This function makes a 'POST' request to the backend with the name of the user and the posted message as the JSON body passed through
   * @param messageToPost The message to be posted into the database
   */
  async function makeMultiPost(file: any) {
    const formData = new FormData();
    if (file == null) return false;
    formData.append("upload_file", file);
    formData.append("mime", file.type);

    await fetch(herokuUrl + `upload`, {
      method: 'POST',
      mode: 'cors',
      cache: 'no-cache',
      credentials: 'same-origin', // include, *same-origin, omit
      redirect: 'follow', // manual, *follow, error
      referrerPolicy: 'no-referrer', // no-referrer, *client
      body: formData // body data type must match "Content-Type" header
    })
      .then((response) => {
        return response.json();//parse the json from response
      })
      .then((myResponse) => {
         console.log(myResponse);
        setResponseFiles(myResponse.mData);// update the state data from the posts parsed from response data
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
    setHasSubmitted(true);//set the hasSubmitted state to 'true'
    if ((window as any).Cypress) {// if this app run is a test
      responseMessages.push({ mId: -1, mSubject: 'Patient0', mMessage: 'This is a test message I wrote for app tests!!' });//push this Object to the array to be dispalyed
    }
    else {// if not testing
      let toReturn: string = enteredMessage.value;// The message to be posted
      let file: any = enteredFile.file;
      if (enteredMessage.value === '') {// if the input text box is empty
        toReturn = 'I was too lazy to type 😊';// set this string to post if the text input is empty
        setEnteredMessage({ value: 'I was too lazy to type 😊' });// same thing as above
      }
      makePost(toReturn);//make the 'POST' request with the appropriate message
      makeMultiPost(file);// file upload
      //makeMultiPost(toReturn, file);//make the 'POST' request with the appropriate message
      responseMessages.push({ mId: -1, mSubject: (props.userName as string).trim(), mMessage: toReturn });//push the latest post to the array of posts to display
    }
    ReactDOM.render(<DisplayPosts//render the posts here
      messagePost={responseMessages}
      filePost={responseFiles}
    />
      , document.getElementById('post'));//in this id
    setEnteredMessage({ value: '' });//empty the input text box
  }

  // load messages page while testing too
  if (props.signedIn || (window as any).Cypress)
    return (
      //start of the div for displaying all the messages
      <div id="messages">
        <div id="input-items">
          {props.userName} <br />
          <form onSubmit={handleSubmit}>
            <label>
              <br></br>
              <input type="text" name="name" value={enteredMessage.value} onChange={handleMessageInputChange} placeholder="What would you like to post today?" id="input-text" />
            </label>
            <input type="submit" value="Post" />
            <label>
              <br></br>
            <input type="text" name="link" value={enteredLink.value} onChange={handleLinkInputChange} placeholder="optional link?" id="input-link" width={40} />
            </label>
            <label>
              <br></br>
            <input type="file" name="file" onChange={handleFileInputChange} id="input-file" width={30} />
            </label>
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

/**
 * This function does necessary hides/shows of elements when the user logs out
 */
function onLogout(): void {
  const appLogo = document.getElementById("App-logo");// app logo element
  const loginButton = document.getElementById("login-button");//login button element
  const messageDiv = document.getElementById("messages");// messages div element
  loginButton!.style.display = "none";//hide login button
  appLogo!.style.display = "block";//show app logo
  loginButton!.style.display = "block";//show login button
  messageDiv!.style.display = "none";//hide message posts div
}

//export the MessagePage Component into `App` Component
export default MessagePage;