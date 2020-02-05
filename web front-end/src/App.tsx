import React, { useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';

const App = () => {
  const [toDisplay, setToDisplay] = useState<String>('Edit src/App.tsx and save to reload.');
  useEffect(() => {
    const response = fetch('http://localhost:5000/', {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache',
    }).then((response) => {
      return response.text();
    })
      .then((myResponse) => {
        setToDisplay(myResponse);
      })
      .catch((error) => {
        console.error(error);
      });
  }, []);
  function handleClick(e: any) {
    e.preventDefault();
    setToDisplay('Wow Congratulations you did some stuff');
  }
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          {toDisplay}
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
        <button onClick={handleClick}>Click Me for some Magic</button>
      </header>
    </div>
  );
}

export default App;
