import React from 'react';
import { render } from '@testing-library/react';
import App from './App';

describe('All Tests for The Buzz front-end', () => {
  test('Has Login button', () => {
    const { getByText } = render(<App />);
    const linkElement = getByText(/Login/i);
    expect(linkElement).toBeInTheDocument();
  });
});
