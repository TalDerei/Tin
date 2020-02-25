import React from 'react';
import Display from './Display.jpg';

const DisplayPosts = (props: any): JSX.Element => {
  const arraysOfMessages: Object[] = props.messagePost;
  let arraytoReturn: JSX.Element[] = [];
  function onVote(event: any): void {
    event.preventDefault();
    let newNumber: number = ++(event.target.value as number);
    (event.target.value as number) = newNumber;
    document.getElementById(event.target.name)!.innerHTML = newNumber.toString();
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
        <button
          onClick={onVote}
          value={0}
          name={`upvote${input.mId}`}
        >
          Upvote
        </button>
        <text
          id={`upvote${input.mId}`}
        >
          0
        </text>
        <button
          onClick={onVote}
          value={0}
          name={`downvote${input.mId}`}
        >
          Downvote
        </button>
        <text
          id={`downvote${input.mId}`}
        >
          0
        </text>
      </div>
    )
  });

  return (
    <div>
      {arraytoReturn.reverse()}
    </div>
  );
}

export default DisplayPosts;