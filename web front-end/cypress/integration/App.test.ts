/**
 * Tests for our App called "The Buzz"
 */

describe('The Buzz tests', () => {
  // Visit our site locally first
  before(() => {
    cy.visit('/');
  });
  let message = 'This is a test message I wrote for app tests!!';
  // test that the landing page was loaded correctly
  it('successfully loads login page', () => {
    cy.get('.App-logo').should('be.visible'); // check that app-logo is visible
    cy.get('#login-button').should('be.visible'); // check that login button is visible
    cy.contains('Login');
  });

  it('messages page is loaded', () => {
    // cy.get('.googleLoginButton').click(); // click the login button (this would require more time to test properly so I exposed the messages page without having to log in)
    cy.get('#messages').should('be.visible');
    cy.contains('Patient0');
    cy.contains('What would you like to post today?');
    cy.contains('Logout');
  });

  it('lets user post messages', () => {
    cy.get('label > input').clear().type(message); // type custom message
    cy.get('[type="submit"]').click(); // post the custom message
  });

  it('User name is displayed', () => {
    cy.get('u').contains('Patient0:'); // check for user named "Patient0"
  });

  it('posted message is displayed', () => {
    cy.contains(message); // post custom message
  });

  it('Display picture is visible', () => {
    cy.get('#Display-picture').should('be.visible'); // get display picture
  });

  it('displays upvote and downvote buttons', () => {
    cy.get('#post > :nth-child(1) > :nth-child(1) > :nth-child(3)').should('be.visible'); // upvote button
    cy.get('#post > :nth-child(1) > :nth-child(1) > :nth-child(4)').should('be.visible'); // downvote button
  });

  it('Upvote is reflected in the DOM', () => {
    cy.contains('Upvote').click(); // upvote once
    cy.get('#post > :nth-child(1) > :nth-child(1)').contains('1'); // check for upvote score
  });

  it('Downvote is reflected in the DOM', () => {
    cy.contains('Downvote').dblclick(); // downvote twice
    cy.get('#post > :nth-child(1) > :nth-child(1)').contains('2'); // check for downvote score
  });

  it('Logout of app', () => {
    cy.get('.googleLogoutButton').should('be.visible'); //check if logout button is visible
    cy.contains('Logout').click(); //click the logout button
  });
});
