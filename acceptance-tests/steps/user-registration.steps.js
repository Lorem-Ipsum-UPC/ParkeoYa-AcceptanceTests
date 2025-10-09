const { Given, When, Then } = require('@cucumber/cucumber');
const { expect } = require('@playwright/test');

// Navigation steps
Given('que estoy en la página principal de ParkeoYa', async function () {
    await this.page.goto('/');
});

Given('que estoy en la página de registro', async function () {
    await this.page.goto('/register');
});

Given('que estoy en el panel de propietario', async function () {
    await this.page.goto('/owner/dashboard');
});

// Form interaction steps
When('selecciono {string} como tipo de usuario', async function (userType) {
    await this.page.selectOption('[data-testid="user-type-select"]', userType);
});

When('completo el campo {string} con {string}', async function (fieldName, value) {
    const fieldSelector = `[data-testid="${fieldName.toLowerCase().replace(/\s+/g, '-')}-input"]`;
    await this.page.fill(fieldSelector, value);
});

When('hago clic en {string}', async function (buttonText) {
    await this.page.click(`button:has-text("${buttonText}")`);
});

// Verification steps
Then('debo ser redirigido al dashboard de conductor', async function () {
    await this.page.waitForURL('/driver/dashboard');
    expect(this.page.url()).toContain('/driver/dashboard');
});

Then('debo ser redirigido al panel de propietario', async function () {
    await this.page.waitForURL('/owner/dashboard');
    expect(this.page.url()).toContain('/owner/dashboard');
});

Then('debo ver el mensaje {string}', async function (message) {
    const messageElement = this.page.locator(`text=${message}`);
    await expect(messageElement).toBeVisible();
});

Then('debo ver opciones para buscar estacionamientos', async function () {
    const searchButton = this.page.locator('[data-testid="search-parking-button"]');
    await expect(searchButton).toBeVisible();
});

Then('debo ver la opción {string}', async function (optionText) {
    const optionElement = this.page.locator(`text=${optionText}`);
    await expect(optionElement).toBeVisible();
});

// Error handling steps
Then('debo ver el mensaje de error {string}', async function (errorMessage) {
    const errorElement = this.page.locator('.error-message', { hasText: errorMessage });
    await expect(errorElement).toBeVisible();
});

Then('debo permanecer en la página de registro', async function () {
    expect(this.page.url()).toContain('/register');
});

// Precondition steps
Given('que existe un usuario con email {string}', async function (email) {
    // Mock or create a user with the given email
    await this.apiContext.post('/api/users', {
        data: {
            email: email,
            userType: 'driver',
            name: 'Usuario Existente',
            password: 'password123'
        }
    });
});

Given('que la página de registro está disponible', async function () {
    const registerLink = this.page.locator('a[href="/register"]');
    await expect(registerLink).toBeVisible();
});

// Form validation steps
Then('el formulario debe mantener los datos ingresados excepto la contraseña', async function () {
    const passwordInput = this.page.locator('[data-testid="contraseña-input"]');
    const confirmPasswordInput = this.page.locator('[data-testid="confirmar-contraseña-input"]');
    
    await expect(passwordInput).toHaveValue('');
    await expect(confirmPasswordInput).toHaveValue('');
});

Then('los campos de contraseña deben estar vacíos', async function () {
    const passwordInput = this.page.locator('[data-testid="contraseña-input"]');
    const confirmPasswordInput = this.page.locator('[data-testid="confirmar-contraseña-input"]');
    
    await expect(passwordInput).toHaveValue('');
    await expect(confirmPasswordInput).toHaveValue('');
});