//importing the necessary library and picture
import React from 'react';
import Display from './Display.jpg';

/**
 * This function returns the component that loads the singular posts
 * @param props properties passed in from MessagePage Component
 */
const DisplayPosts = (props: any): JSX.Element => {
  const herokuUrl = "https://cors-anywhere.herokuapp.com/https://limitless-ocean-62391.herokuapp.com/";
  //const herokuUrl = "http://localhost:4567/";
  const arraysOfMessages: Object[] = props.messagePost;// arrays of the messages to display
  const arraysOfFiles: Object[] = props.filePost;// arrays of the files to display
  const selectedFile = props.filePost[0]; // selected file
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

    async function downloadFile(event: any) {
        event.preventDefault();//prevent default event
        let fileId: string = event.target.value;
        let url: string = herokuUrl + "file/" + fileId;
        await fetch(url, {
            method: 'GET',
            mode: 'cors',
            cache: 'no-cache',
            credentials: 'same-origin', // include, *same-origin, omit
        })
            .then((response) => {
                //return response.json();// parse json from response
                console.log(response);
                // TODO: decoding byteArray into a file
                //createAndDownloadBlobFile(filename, )
                return true;
            })
            .then((myResponse) => {
                console.log(myResponse);
                //setResponseMessages(myResponse.mData);// update the state data from the posts parsed from response data
            })
            .catch((error) => {
                console.error(error);//log error
            });
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
        <button
          onClick={downloadFile}
          value={selectedFile}
          name={`file${input.mId}`}
          >
              Download File
        </button>
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