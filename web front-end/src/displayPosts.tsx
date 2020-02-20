import React from 'react';
import Display from './Display.jpg';

const DisplayPosts = (props: any): JSX.Element => {
  return (
    <p>
      <img src={Display} className="Display-picture" alt="dp" id="Display-picture" width="100" height="100" />
      <b><u>{(props.messagesArray)[0].name}:</u></b> <br />
      {(props.messagesArray)[0].message}
    </p>);
}

export default DisplayPosts;