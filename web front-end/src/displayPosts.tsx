import React, { useState } from 'react';
import Display from './Display.jpg';

const DisplayPosts = (props: any): JSX.Element => {
  let [upVotes, setUpVotes] = useState<number>(0);
  let [downVotes, setDownVotes] = useState<number>(0);
  function onUpVote(event: any) {
    event.preventDefault();
    const newNumber = ++upVotes;
    setUpVotes(newNumber);
  }

  function onDownVote(event: any) {
    event.preventDefault();
    const newNumber = ++downVotes;
    setDownVotes(newNumber);
  }
  return (
    <div>
      <div id="profile">
        <img src={Display} className="Display-picture" alt="dp" id="Display-picture" width="100" height="100" />
        <b><u>{(props.messagesArray)[0].name}:</u></b> <br />
      </div>
      {(props.messagesArray)[0].message}
      <br />
      <button onClick={onUpVote}>Upvote</button>  {upVotes} <button onClick={onDownVote}>Downvote</button>  {downVotes}
    </div>);
}

export default DisplayPosts;