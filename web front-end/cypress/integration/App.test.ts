/**
 * Tests for our App called "The Buzz"
 */

describe('The Buzz tests', () => {
  // Visit our site locally first
  before(() => {
    cy.visit('/');
  });

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

  it('lets use post messages', () => {
    cy.get('label > input').clear().type('This is a test message to wrote for app tests!!');
    cy.get('[type="submit"]').click();
  });
});