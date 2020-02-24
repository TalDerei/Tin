import React, { useState } from 'react';
import Display from './Display.jpg';

const DisplayPosts = (props: any): JSX.Element => {
  const arraysOfMessages: Object[] = props.messagePost;
  let arraytoReturn: JSX.Element[] = [];
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

  arraysOfMessages.forEach((input: any) => {
    arraytoReturn.push(
      <div key={input.mId} className="individual-message">
        <div className="profile" key={input.mId}>
          <img src={Display} className="Display-picture" alt="dp" id="Display-picture" width="100" height="100" />
          <b><u>{input.mSubject}:</u></b> <br />
        </div>
        {input.mMessage}
        <br />
        <button onClick={onUpVote}>Upvote</button>  {upVotes} <button onClick={onDownVote}>Downvote</button>  {downVotes}
      </div>
    )
  });

  return (
    <div>
      {arraytoReturn}
    </div>
  );
}

export default DisplayPosts;