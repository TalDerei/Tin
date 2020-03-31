//importing the necessary library and picture
import React from 'react';
console.log("display bio pg");
/**
 * This function returns the component that loads the singular posts
 * @param props properties passed in from MessagePage Component
 */
const DisplayBio = (props: any): JSX.Element => {
  const arrayOfBios: Object[] = props.bioToPost;// arrays of the messages to display
  let arraytoReturn: JSX.Element[] = [];// same as above but this one will be returned

  arrayOfBios.forEach((input: any) => {//for each posts in the database so far
    arraytoReturn.push(//push it to the array to be displayed eventually
      <div key={input.mId} className="individual-message">
        {input.mMessage}
        <br />
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
export default DisplayBio;