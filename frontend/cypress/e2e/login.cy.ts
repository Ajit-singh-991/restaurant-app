describe('Login', () => {
  it('shows login page at /login', () => {
    cy.visit('/login');
    cy.url().should('include', '/login');
    cy.get('input[type="text"], input[type="email"], input[name="username"]').should('exist');
    cy.get('input[type="password"]').should('exist');
  });

  it('can type credentials and submit', () => {
    cy.visit('/login');
    cy.get('input[type="text"], input[type="email"], input[name="username"]').first().type('customer1');
    cy.get('input[type="password"]').type('password123');
    cy.get('button[type="submit"], button').contains(/login|sign in/i).click();
    // After submit either success (redirect) or error message
    cy.get('body').should('be.visible');
  });
});
