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

  it('logins to Google', () => {
    cy.get('.googleLoginButton').click(); // click the login button
  });
});