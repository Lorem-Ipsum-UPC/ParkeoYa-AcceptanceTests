const request = require('supertest');
const app = require('../../../src/app');
const { sequelize } = require('../../../src/config/database');

describe('Authentication API Integration Tests', () => {
    let server;

    beforeAll(async () => {
        // Setup test database
        await sequelize.sync({ force: true });
        server = app.listen(0);
    });

    afterAll(async () => {
        await sequelize.close();
        await server.close();
    });

    beforeEach(async () => {
        // Clean up database before each test
        await sequelize.truncate({ cascade: true });
    });

    describe('POST /api/auth/register', () => {
        it('should register a new driver successfully', async () => {
            // Arrange
            const userData = {
                email: 'driver@test.com',
                password: 'TestPassword123!',
                name: 'Test Driver',
                userType: 'driver',
                phone: '987654321'
            };

            // Act
            const response = await request(app)
                .post('/api/auth/register')
                .send(userData)
                .expect(201);

            // Assert
            expect(response.body.success).toBe(true);
            expect(response.body.user.email).toBe(userData.email);
            expect(response.body.user.userType).toBe('driver');
            expect(response.body.token).toBeDefined();
            expect(response.body.user.password).toBeUndefined(); // Password should not be returned
        });

        it('should register a new owner successfully', async () => {
            // Arrange
            const userData = {
                email: 'owner@test.com',
                password: 'TestPassword123!',
                name: 'Test Owner',
                userType: 'owner',
                phone: '987654321',
                companyName: 'Test Parking Company'
            };

            // Act
            const response = await request(app)
                .post('/api/auth/register')
                .send(userData)
                .expect(201);

            // Assert
            expect(response.body.success).toBe(true);
            expect(response.body.user.email).toBe(userData.email);
            expect(response.body.user.userType).toBe('owner');
            expect(response.body.user.companyName).toBe(userData.companyName);
        });

        it('should reject duplicate email registration', async () => {
            // Arrange
            const userData = {
                email: 'duplicate@test.com',
                password: 'TestPassword123!',
                name: 'Test User',
                userType: 'driver',
                phone: '987654321'
            };

            // First registration
            await request(app)
                .post('/api/auth/register')
                .send(userData)
                .expect(201);

            // Act - Attempt duplicate registration
            const response = await request(app)
                .post('/api/auth/register')
                .send(userData)
                .expect(400);

            // Assert
            expect(response.body.success).toBe(false);
            expect(response.body.error).toContain('email ya está registrado');
        });

        it('should validate required fields', async () => {
            // Arrange
            const incompleteData = {
                email: 'test@example.com'
                // Missing required fields
            };

            // Act
            const response = await request(app)
                .post('/api/auth/register')
                .send(incompleteData)
                .expect(400);

            // Assert
            expect(response.body.success).toBe(false);
            expect(response.body.errors).toBeDefined();
        });
    });

    describe('POST /api/auth/login', () => {
        it('should login with valid credentials', async () => {
            // Arrange - First create a user
            const userData = {
                email: 'login@test.com',
                password: 'TestPassword123!',
                name: 'Login Test',
                userType: 'driver',
                phone: '987654321'
            };

            await request(app)
                .post('/api/auth/register')
                .send(userData)
                .expect(201);

            // Act
            const response = await request(app)
                .post('/api/auth/login')
                .send({
                    email: userData.email,
                    password: userData.password
                })
                .expect(200);

            // Assert
            expect(response.body.success).toBe(true);
            expect(response.body.user.email).toBe(userData.email);
            expect(response.body.token).toBeDefined();
        });

        it('should reject invalid credentials', async () => {
            // Act
            const response = await request(app)
                .post('/api/auth/login')
                .send({
                    email: 'nonexistent@test.com',
                    password: 'WrongPassword123!'
                })
                .expect(401);

            // Assert
            expect(response.body.success).toBe(false);
            expect(response.body.error).toContain('Credenciales inválidas');
        });
    });

    describe('GET /api/auth/profile', () => {
        let authToken;
        let userId;

        beforeEach(async () => {
            // Create and login a user to get auth token
            const userData = {
                email: 'profile@test.com',
                password: 'TestPassword123!',
                name: 'Profile Test',
                userType: 'driver',
                phone: '987654321'
            };

            const registerResponse = await request(app)
                .post('/api/auth/register')
                .send(userData);

            authToken = registerResponse.body.token;
            userId = registerResponse.body.user.id;
        });

        it('should return user profile with valid token', async () => {
            // Act
            const response = await request(app)
                .get('/api/auth/profile')
                .set('Authorization', `Bearer ${authToken}`)
                .expect(200);

            // Assert
            expect(response.body.success).toBe(true);
            expect(response.body.user.id).toBe(userId);
            expect(response.body.user.email).toBe('profile@test.com');
        });

        it('should reject request without token', async () => {
            // Act
            const response = await request(app)
                .get('/api/auth/profile')
                .expect(401);

            // Assert
            expect(response.body.success).toBe(false);
            expect(response.body.error).toContain('Token requerido');
        });

        it('should reject request with invalid token', async () => {
            // Act
            const response = await request(app)
                .get('/api/auth/profile')
                .set('Authorization', 'Bearer invalid-token')
                .expect(401);

            // Assert
            expect(response.body.success).toBe(false);
            expect(response.body.error).toContain('Token inválido');
        });
    });

    describe('Authentication Flow Integration', () => {
        it('should complete full authentication flow', async () => {
            // Step 1: Register
            const userData = {
                email: 'flow@test.com',
                password: 'TestPassword123!',
                name: 'Flow Test',
                userType: 'owner',
                phone: '987654321',
                companyName: 'Flow Test Company'
            };

            const registerResponse = await request(app)
                .post('/api/auth/register')
                .send(userData)
                .expect(201);

            expect(registerResponse.body.success).toBe(true);
            const registrationToken = registerResponse.body.token;

            // Step 2: Login with same credentials
            const loginResponse = await request(app)
                .post('/api/auth/login')
                .send({
                    email: userData.email,
                    password: userData.password
                })
                .expect(200);

            expect(loginResponse.body.success).toBe(true);
            const loginToken = loginResponse.body.token;

            // Step 3: Access protected route with login token
            const profileResponse = await request(app)
                .get('/api/auth/profile')
                .set('Authorization', `Bearer ${loginToken}`)
                .expect(200);

            expect(profileResponse.body.user.email).toBe(userData.email);
            expect(profileResponse.body.user.userType).toBe('owner');

            // Step 4: Verify old token is different from new token
            expect(registrationToken).not.toBe(loginToken);
        });
    });
});