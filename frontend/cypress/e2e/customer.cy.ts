describe('Customer app', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('redirects to menu and shows app shell', () => {
    cy.url().should('include', '/menu');
    cy.contains('Restaurant').should('be.visible');
  });

  it('shows menu content or loading', () => {
    cy.visit('/menu');
    cy.get('body').should('be.visible');
    // Either menu content or loading/categories
    cy.get('mat-toolbar, .menu-container, mat-spinner, [class*="menu"]').should('exist');
  });

  it('navigates to cart from toolbar', () => {
    cy.visit('/menu');
    cy.get('[routerlink="/cart"]').first().click();
    cy.url().should('include', '/cart');
  });

  it('shows login when opening user menu', () => {
    cy.visit('/menu');
    cy.get('button[mat-icon-button]').contains('person').first().click({ force: true });
    cy.get('mat-menu').should('be.visible');
  });
});
