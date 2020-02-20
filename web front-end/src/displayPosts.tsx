import React from 'react';
import Display from './Display.jpg';

const DisplayPosts = (props: any): JSX.Element => {
  function onUpVote(event: any) {
    event.preventDefault();

  }

  function onDownVote(event: any) {
    event.preventDefault();

  }
  return (
    <p>
      <div id="profile">
        <img src={Display} className="Display-picture" alt="dp" id="Display-picture" width="100" height="100" />
        <b><u>{(props.messagesArray)[0].name}:</u></b> <br />
      </div>
      {(props.messagesArray)[0].message}
      <br />
      <button onClick={onUpVote}><kbd>Upvote</kbd></button>  {(props.messagesArray)[0].upVote} <button onClick={onDownVote}><kbd>Downvote</kbd></button>  {(props.messagesArray)[0].downVote}
    </p>);
}

export default DisplayPosts;