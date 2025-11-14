const { Given, When, Then } = require('@cucumber/cucumber');
const axios = require('axios');
const { expect } = require('chai');

// Variables compartidas entre steps
let apiBaseUrl;
let authToken;
let lastResponse;
let testData = {};

// Antecedentes comunes
Given('que el servicio de API está disponible en {string}', function (url) {
  apiBaseUrl = url;
  this.apiBaseUrl = url;
});

Given('tengo credenciales válidas de propietario', async function () {
  // Simular autenticación de propietario
  const response = await axios.post(`${apiBaseUrl}/api/v1/authentication/sign-in`, {
    email: 'owner@test.com',
    password: 'TestPassword123!'
  });
  authToken = response.data.token;
  this.authToken = authToken;
  this.userType = 'owner';
});

Given('tengo credenciales válidas de conductor', async function () {
  // Simular autenticación de conductor
  const response = await axios.post(`${apiBaseUrl}/api/v1/authentication/sign-in`, {
    email: 'driver@test.com',
    password: 'TestPassword123!'
  });
  authToken = response.data.token;
  this.authToken = authToken;
  this.userType = 'driver';
});

Given('estoy autenticado como propietario', async function () {
  const response = await axios.post(`${this.apiBaseUrl}/api/v1/authentication/sign-in`, {
    email: 'owner@test.com',
    password: 'TestPassword123!'
  });
  this.authToken = response.data.token;
});

Given('estoy autenticado como conductor', async function () {
  const response = await axios.post(`${this.apiBaseUrl}/api/v1/authentication/sign-in`, {
    email: 'driver@test.com',
    password: 'TestPassword123!'
  });
  this.authToken = response.data.token;
});

Given('estoy autenticado como propietario con id {string}', async function (ownerId) {
  this.currentUserId = ownerId;
  this.authToken = 'mock-owner-token-' + ownerId;
});

Given('estoy autenticado como conductor con id {string}', async function (driverId) {
  this.currentUserId = driverId;
  this.authToken = 'mock-driver-token-' + driverId;
});

Given('no estoy autenticado', function () {
  this.authToken = null;
});

Given('estoy autenticado en el sistema', function () {
  this.authToken = 'mock-auth-token';
});

// Steps para peticiones HTTP
When('envío una petición GET a {string}', async function (endpoint) {
  try {
    const config = this.authToken ? {
      headers: { Authorization: `Bearer ${this.authToken}` }
    } : {};
    
    lastResponse = await axios.get(`${this.apiBaseUrl}${endpoint}`, config);
    this.lastResponse = lastResponse;
    this.statusCode = lastResponse.status;
  } catch (error) {
    this.lastResponse = error.response;
    this.statusCode = error.response?.status;
  }
});

When('envío una petición POST a {string} con los siguientes datos:', async function (endpoint, dataTable) {
  const data = {};
  dataTable.hashes().forEach(row => {
    data[row.campo] = row.valor;
  });

  try {
    const config = this.authToken ? {
      headers: { Authorization: `Bearer ${this.authToken}` }
    } : {};
    
    lastResponse = await axios.post(`${this.apiBaseUrl}${endpoint}`, data, config);
    this.lastResponse = lastResponse;
    this.statusCode = lastResponse.status;
  } catch (error) {
    this.lastResponse = error.response;
    this.statusCode = error.response?.status;
  }
});

When('envío una petición POST a {string} con datos válidos', async function (endpoint) {
  const mockData = {
    name: 'Test Parking',
    address: 'Test Address',
    city: 'Lima',
    totalSpots: 20
  };

  try {
    const config = this.authToken ? {
      headers: { Authorization: `Bearer ${this.authToken}` }
    } : {};
    
    lastResponse = await axios.post(`${this.apiBaseUrl}${endpoint}`, mockData, config);
    this.lastResponse = lastResponse;
    this.statusCode = lastResponse.status;
  } catch (error) {
    this.lastResponse = error.response;
    this.statusCode = error.response?.status;
  }
});

When('envío una petición PATCH a {string} con los siguientes datos:', async function (endpoint, dataTable) {
  const data = {};
  dataTable.hashes().forEach(row => {
    data[row.campo] = row.valor;
  });

  try {
    const config = {
      headers: { Authorization: `Bearer ${this.authToken}` }
    };
    
    lastResponse = await axios.patch(`${this.apiBaseUrl}${endpoint}`, data, config);
    this.lastResponse = lastResponse;
    this.statusCode = lastResponse.status;
  } catch (error) {
    this.lastResponse = error.response;
    this.statusCode = error.response?.status;
  }
});

When('envío una petición DELETE a {string} con los siguientes datos:', async function (endpoint, dataTable) {
  const data = {};
  dataTable.hashes().forEach(row => {
    data[row.campo] = row.valor;
  });

  try {
    const config = {
      headers: { Authorization: `Bearer ${this.authToken}` },
      data: data
    };
    
    lastResponse = await axios.delete(`${this.apiBaseUrl}${endpoint}`, config);
    this.lastResponse = lastResponse;
    this.statusCode = lastResponse.status;
  } catch (error) {
    this.lastResponse = error.response;
    this.statusCode = error.response?.status;
  }
});

// Validaciones de respuesta
Then('debo recibir un código de estado {int}', function (expectedStatus) {
  expect(this.statusCode).to.equal(expectedStatus);
});

Then('la respuesta debe contener el mensaje {string}', function (expectedMessage) {
  const responseBody = this.lastResponse?.data;
  const messageFound = JSON.stringify(responseBody).includes(expectedMessage);
  expect(messageFound).to.be.true;
});

Then('la respuesta debe contener una lista de estacionamientos', function () {
  expect(this.lastResponse.data).to.be.an('array');
  expect(this.lastResponse.data.length).to.be.greaterThan(0);
});

Then('la respuesta debe contener el {string} del estacionamiento creado', function (field) {
  expect(this.lastResponse.data).to.have.property(field);
  expect(this.lastResponse.data[field]).to.not.be.undefined;
});

Then('la respuesta debe contener el {string} de la reserva', function (field) {
  expect(this.lastResponse.data).to.have.property(field);
  this.createdReservationId = this.lastResponse.data[field];
});

Then('la respuesta debe contener el {string} de la reseña', function (field) {
  expect(this.lastResponse.data).to.have.property(field);
});

Then('la respuesta debe contener los datos actualizados', function () {
  expect(this.lastResponse.data).to.be.an('object');
  expect(Object.keys(this.lastResponse.data).length).to.be.greaterThan(0);
});

Then('el campo {string} debe ser {string}', function (field, expectedValue) {
  expect(this.lastResponse.data[field]).to.equal(expectedValue);
});

Then('la respuesta debe confirmar el registro del token', function () {
  expect(this.lastResponse.data).to.have.property('success');
  expect(this.lastResponse.data.success).to.be.true;
});

Then('la respuesta debe confirmar el envío de la notificación', function () {
  expect(this.lastResponse.data).to.have.property('success');
  expect(this.lastResponse.data.success).to.be.true;
});

Then('la respuesta debe confirmar la eliminación del token', function () {
  expect(this.lastResponse.data).to.have.property('success');
  expect(this.lastResponse.data.success).to.be.true;
});

module.exports = { testData };
