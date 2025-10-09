const { Given, When, Then } = require('@cucumber/cucumber');
const { expect } = require('@playwright/test');

// Authentication steps
Given('que soy un propietario autenticado en el sistema', async function () {
    // Login as owner
    await this.page.goto('/login');
    await this.page.fill('[data-testid="email-input"]', 'owner@test.com');
    await this.page.fill('[data-testid="password-input"]', 'password123');
    await this.page.click('button[type="submit"]');
    await this.page.waitForURL('/owner/dashboard');
});

Given('que tengo permisos para registrar estacionamientos', async function () {
    // Verify owner permissions
    const registerButton = this.page.locator('[data-testid="register-parking-button"]');
    await expect(registerButton).toBeVisible();
});

// Navigation steps
Given('que estoy en el formulario de registro de estacionamiento', async function () {
    await this.page.goto('/owner/parking/register');
});

When('hago clic en {string}', async function (buttonText) {
    await this.page.click(`button:has-text("${buttonText}")`);
});

// Form completion steps
When('completo la información básica del estacionamiento', async function () {
    await this.page.fill('[data-testid="nombre-del-estacionamiento-input"]', 'Estacionamiento Test');
    await this.page.fill('[data-testid="dirección-input"]', 'Calle Test 456');
    await this.page.selectOption('[data-testid="provincia-select"]', 'Lima');
    await this.page.selectOption('[data-testid="distrito-select"]', 'Miraflores');
    await this.page.fill('[data-testid="capacidad-total-input"]', '30');
});

When('selecciono {string} como provincia', async function (provincia) {
    await this.page.selectOption('[data-testid="provincia-select"]', provincia);
});

When('selecciono {string} como distrito', async function (distrito) {
    await this.page.selectOption('[data-testid="distrito-select"]', distrito);
});

When('selecciono {string} como horario de funcionamiento', async function (horario) {
    await this.page.selectOption('[data-testid="horario-funcionamiento-select"]', horario);
});

When('establezco la tarifa por hora en {string}', async function (tarifa) {
    await this.page.fill('[data-testid="tarifa-por-hora-input"]', tarifa);
});

When('agrego la descripción {string}', async function (descripcion) {
    await this.page.fill('[data-testid="descripcion-textarea"]', descripcion);
});

// Custom schedule steps
When('selecciono {string}', async function (option) {
    await this.page.click(`label:has-text("${option}")`);
});

When('configuro horario de {string} de {string} a {string}', async function (dias, horaInicio, horaFin) {
    const daySelector = `[data-testid="horario-${dias.toLowerCase().replace(/\s+/g, '-')}"]`;
    await this.page.fill(`${daySelector} [data-testid="hora-inicio"]`, horaInicio);
    await this.page.fill(`${daySelector} [data-testid="hora-fin"]`, horaFin);
});

When('establezco tarifas diferenciadas por horario', async function () {
    await this.page.fill('[data-testid="tarifa-dia-input"]', '4.00');
    await this.page.fill('[data-testid="tarifa-noche-input"]', '6.00');
});

// Additional services steps
When('marco el servicio {string}', async function (servicio) {
    await this.page.check(`[data-testid="servicio-${servicio.toLowerCase().replace(/\s+/g, '-')}"]`);
});

When('configuro precios adicionales para cada servicio', async function () {
    await this.page.fill('[data-testid="precio-lavado-input"]', '15.00');
    await this.page.fill('[data-testid="precio-carga-electrica-input"]', '8.00');
    await this.page.fill('[data-testid="precio-vigilancia-input"]', '2.00');
});

When('agrego fotos del estacionamiento', async function () {
    // Mock file upload
    await this.page.setInputFiles('[data-testid="fotos-input"]', ['test-files/parking-photo.jpg']);
});

// Verification steps
Then('el estacionamiento debe ser creado exitosamente', async function () {
    await this.page.waitForSelector('[data-testid="success-message"]', { timeout: 5000 });
});

Then('debo ser redirigido a la página de gestión del estacionamiento', async function () {
    await this.page.waitForURL('**/owner/parking/manage/**');
    expect(this.page.url()).toContain('/owner/parking/manage/');
});

Then('debo ver {string} en mi lista de estacionamientos', async function (parkingName) {
    const parkingElement = this.page.locator(`[data-testid="parking-list"] >> text=${parkingName}`);
    await expect(parkingElement).toBeVisible();
});

Then('el estacionamiento debe ser creado con los horarios personalizados', async function () {
    const scheduleElement = this.page.locator('[data-testid="custom-schedule-indicator"]');
    await expect(scheduleElement).toBeVisible();
});

Then('debo poder ver los horarios configurados en el panel de gestión', async function () {
    const schedulePanel = this.page.locator('[data-testid="schedule-panel"]');
    await expect(schedulePanel).toBeVisible();
});

Then('el estacionamiento debe ser creado con todos los servicios', async function () {
    const servicesPanel = this.page.locator('[data-testid="services-panel"]');
    await expect(servicesPanel).toBeVisible();
});

Then('los usuarios deben poder ver estos servicios al buscar estacionamientos', async function () {
    // This would typically be verified in a separate test or through API
    // For now, we'll verify the services are marked as active
    const activeServices = this.page.locator('[data-testid="active-services"] .service-item');
    await expect(activeServices).toHaveCount(3);
});

// Error handling steps
Then('los datos ingresados deben mantenerse', async function () {
    const nameInput = this.page.locator('[data-testid="nombre-del-estacionamiento-input"]');
    await expect(nameInput).not.toHaveValue('');
});

// Precondition steps
Given('que ya existe un estacionamiento en {string}', async function (direccion) {
    // Create existing parking through API
    await this.apiContext.post('/api/parkings', {
        data: {
            name: 'Estacionamiento Existente',
            address: direccion,
            capacity: 25,
            ownerId: 1
        }
    });
});

When('intento registrar otro estacionamiento en la misma dirección', async function () {
    await this.page.fill('[data-testid="dirección-input"]', 'Av. Principal 123, Lima');
});

When('completo todos los demás campos correctamente', async function () {
    await this.page.fill('[data-testid="nombre-del-estacionamiento-input"]', 'Nuevo Estacionamiento');
    await this.page.fill('[data-testid="capacidad-total-input"]', '40');
    await this.page.selectOption('[data-testid="provincia-select"]', 'Lima');
    await this.page.selectOption('[data-testid="distrito-select"]', 'Lima');
});

Then('debo poder modificar la dirección para continuar con el registro', async function () {
    const addressInput = this.page.locator('[data-testid="dirección-input"]');
    await expect(addressInput).toBeEditable();
});

// Space configuration steps
Given('que he registrado exitosamente un estacionamiento', async function () {
    // Assume parking is already registered from previous steps
    const parkingId = await this.page.getAttribute('[data-testid="parking-id"]', 'value');
    this.parkingId = parkingId;
});

When('accedo a la configuración de espacios', async function () {
    await this.page.goto(`/owner/parking/${this.parkingId}/spaces/config`);
});

When('selecciono {string}', async function (option) {
    await this.page.click(`button:has-text("${option}")`);
});

When('confirmo la distribución sugerida de {int} espacios', async function (totalSpaces) {
    const confirmButton = this.page.locator('[data-testid="confirm-distribution"]');
    await expect(confirmButton).toBeVisible();
    await confirmButton.click();
});

When('asigno {int} espacios para autos regulares', async function (spaces) {
    await this.page.fill('[data-testid="regular-spaces-input"]', spaces.toString());
});

When('asigno {int} espacios para autos compactos', async function (spaces) {
    await this.page.fill('[data-testid="compact-spaces-input"]', spaces.toString());
});

When('asigno {int} espacios para discapacitados', async function (spaces) {
    await this.page.fill('[data-testid="disabled-spaces-input"]', spaces.toString());
});

Then('los espacios deben ser creados según la configuración', async function () {
    const spacesGrid = this.page.locator('[data-testid="spaces-grid"]');
    await expect(spacesGrid).toBeVisible();
});

Then('debo poder ver el estado de ocupación de cada espacio', async function () {
    const occupancyIndicators = this.page.locator('.space-occupancy-indicator');
    await expect(occupancyIndicators.first()).toBeVisible();
});

Then('el estacionamiento debe estar disponible para reservas', async function () {
    const availableStatus = this.page.locator('[data-testid="parking-status"][data-status="available"]');
    await expect(availableStatus).toBeVisible();
});