import React from 'react';

const MessagePage = (props: any): JSX.Element => {
  if (props.signedIn)
    return <p>Here I am</p>;
  else
    return <p>xyz</p>;
}

export default MessagePage;