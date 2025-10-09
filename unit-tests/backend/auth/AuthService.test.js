const AuthService = require('../../../../src/services/AuthService');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const User = require('../../../../src/models/User');

// Mock dependencies
jest.mock('bcrypt');
jest.mock('jsonwebtoken');
jest.mock('../../../../src/models/User');

describe('AuthService', () => {
    let authService;

    beforeEach(() => {
        authService = new AuthService();
        jest.clearAllMocks();
    });

    describe('validateCredentials', () => {
        it('should return true for valid credentials', async () => {
            // Arrange
            const email = 'test@example.com';
            const password = 'validPassword123';
            const hashedPassword = 'hashedPassword';
            
            User.findByEmail.mockResolvedValue({ 
                id: 1, 
                email, 
                password: hashedPassword,
                userType: 'driver'
            });
            bcrypt.compare.mockResolvedValue(true);

            // Act
            const result = await authService.validateCredentials(email, password);

            // Assert
            expect(result.isValid).toBe(true);
            expect(result.user.email).toBe(email);
            expect(User.findByEmail).toHaveBeenCalledWith(email);
            expect(bcrypt.compare).toHaveBeenCalledWith(password, hashedPassword);
        });

        it('should return false for invalid email', async () => {
            // Arrange
            const email = 'nonexistent@example.com';
            const password = 'anyPassword';
            
            User.findByEmail.mockResolvedValue(null);

            // Act
            const result = await authService.validateCredentials(email, password);

            // Assert
            expect(result.isValid).toBe(false);
            expect(result.error).toBe('Usuario no encontrado');
        });

        it('should return false for invalid password', async () => {
            // Arrange
            const email = 'test@example.com';
            const password = 'wrongPassword';
            const hashedPassword = 'hashedPassword';
            
            User.findByEmail.mockResolvedValue({ 
                id: 1, 
                email, 
                password: hashedPassword 
            });
            bcrypt.compare.mockResolvedValue(false);

            // Act
            const result = await authService.validateCredentials(email, password);

            // Assert
            expect(result.isValid).toBe(false);
            expect(result.error).toBe('Contraseña incorrecta');
        });
    });

    describe('generateJWTToken', () => {
        it('should generate valid JWT token with user data', () => {
            // Arrange
            const userData = {
                id: 1,
                email: 'test@example.com',
                userType: 'driver'
            };
            const mockToken = 'mockJWTToken';
            
            jwt.sign.mockReturnValue(mockToken);

            // Act
            const result = authService.generateJWTToken(userData);

            // Assert
            expect(result).toBe(mockToken);
            expect(jwt.sign).toHaveBeenCalledWith(
                { 
                    userId: userData.id, 
                    email: userData.email, 
                    userType: userData.userType 
                },
                process.env.JWT_SECRET,
                { expiresIn: '24h' }
            );
        });

        it('should handle token generation errors', () => {
            // Arrange
            const userData = { id: 1, email: 'test@example.com' };
            jwt.sign.mockImplementation(() => {
                throw new Error('Token generation failed');
            });

            // Act & Assert
            expect(() => authService.generateJWTToken(userData))
                .toThrow('Token generation failed');
        });
    });

    describe('hashPassword', () => {
        it('should hash password with correct salt rounds', async () => {
            // Arrange
            const password = 'testPassword123';
            const hashedPassword = 'hashedPassword';
            
            bcrypt.hash.mockResolvedValue(hashedPassword);

            // Act
            const result = await authService.hashPassword(password);

            // Assert
            expect(result).toBe(hashedPassword);
            expect(bcrypt.hash).toHaveBeenCalledWith(password, 12);
        });
    });

    describe('validateSession', () => {
        it('should validate valid JWT token', () => {
            // Arrange
            const token = 'validToken';
            const decodedToken = {
                userId: 1,
                email: 'test@example.com',
                userType: 'driver'
            };
            
            jwt.verify.mockReturnValue(decodedToken);

            // Act
            const result = authService.validateSession(token);

            // Assert
            expect(result.isValid).toBe(true);
            expect(result.userData).toEqual(decodedToken);
            expect(jwt.verify).toHaveBeenCalledWith(token, process.env.JWT_SECRET);
        });

        it('should reject invalid JWT token', () => {
            // Arrange
            const token = 'invalidToken';
            
            jwt.verify.mockImplementation(() => {
                throw new Error('Invalid token');
            });

            // Act
            const result = authService.validateSession(token);

            // Assert
            expect(result.isValid).toBe(false);
            expect(result.error).toBe('Token inválido');
        });
    });
});