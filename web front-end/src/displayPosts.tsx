//importing the necessary library and picture
import React from 'react';
import Display from './Display.jpg';

/**
 * This function returns the component that loads the singular posts
 * @param props properties passed in from MessagePage Component
 */
const DisplayPosts = (props: any): JSX.Element => {
  const arraysOfMessages: Object[] = props.messagePost;// arrays of the messages to display
  let arraytoReturn: JSX.Element[] = [];// same as above but this one will be returned
  /**
   * This function deals with the necessary activities that take place after the up/down vote button is clicked
   * @param event The DOM event that is triggered when the up/down vote button is clicked
   */
  function onVote(event: any): void {
    event.preventDefault();//prevent default event
    let newNumber: number = ++(event.target.value as number);//new number with one increment from previous one
    (event.target.value as number) = newNumber;//update the number being displayed in the DOM
    document.getElementById(event.target.name)!.innerHTML = newNumber.toString();//display that number in the DOM
  }

  arraysOfMessages.forEach((input: any) => {//for each posts in the database so far
    arraytoReturn.push(//push it to the array to be displayed eventually
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
      {
        arraytoReturn.reverse()//reverse the array so that the most recent post is displayed at the top
      }
    </div>
  );
}

//export the DisplayPosts Component
export default DisplayPosts;