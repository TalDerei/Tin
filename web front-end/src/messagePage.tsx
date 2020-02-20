import React from 'react';
import { GoogleLogout } from 'react-google-login';


const MessagePage = (props: any): JSX.Element => {
  if (props.signedIn)
    return <p>
      Here I am <br/>
      <GoogleLogout
        className="googleLogoutButton"
        clientId="372884561524-22jfggk3pefbnanh83o92mqqlmkbvvd9.apps.googleusercontent.com"
        buttonText="Logout"
      >
      </GoogleLogout>
    </p>;
  else
    return <p>xyz</p>;
}

export default MessagePage;