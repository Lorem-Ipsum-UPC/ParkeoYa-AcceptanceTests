import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from '../../../../src/components/auth/LoginForm';

// Mock the auth service
jest.mock('../../../../src/services/AuthService', () => ({
    login: jest.fn()
}));

describe('LoginForm', () => {
    const mockOnSuccess = jest.fn();
    const mockOnError = jest.fn();
    
    const defaultProps = {
        onSuccess: mockOnSuccess,
        onError: mockOnError
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('should render login form with all required fields', () => {
        // Act
        render(<LoginForm {...defaultProps} />);

        // Assert
        expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/contraseña/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /iniciar sesión/i })).toBeInTheDocument();
    });

    it('should show validation errors for empty fields', async () => {
        // Arrange
        const user = userEvent.setup();
        render(<LoginForm {...defaultProps} />);

        // Act
        const submitButton = screen.getByRole('button', { name: /iniciar sesión/i });
        await user.click(submitButton);

        // Assert
        await waitFor(() => {
            expect(screen.getByText(/el email es requerido/i)).toBeInTheDocument();
            expect(screen.getByText(/la contraseña es requerida/i)).toBeInTheDocument();
        });
    });

    it('should validate email format', async () => {
        // Arrange
        const user = userEvent.setup();
        render(<LoginForm {...defaultProps} />);

        // Act
        const emailInput = screen.getByLabelText(/email/i);
        await user.type(emailInput, 'invalid-email');
        
        const submitButton = screen.getByRole('button', { name: /iniciar sesión/i });
        await user.click(submitButton);

        // Assert
        await waitFor(() => {
            expect(screen.getByText(/formato de email inválido/i)).toBeInTheDocument();
        });
    });

    it('should submit form with valid credentials', async () => {
        // Arrange
        const user = userEvent.setup();
        const AuthService = require('../../../../src/services/AuthService');
        AuthService.login.mockResolvedValue({ 
            success: true, 
            user: { id: 1, email: 'test@example.com' },
            token: 'mockToken'
        });

        render(<LoginForm {...defaultProps} />);

        // Act
        const emailInput = screen.getByLabelText(/email/i);
        const passwordInput = screen.getByLabelText(/contraseña/i);
        const submitButton = screen.getByRole('button', { name: /iniciar sesión/i });

        await user.type(emailInput, 'test@example.com');
        await user.type(passwordInput, 'password123');
        await user.click(submitButton);

        // Assert
        await waitFor(() => {
            expect(AuthService.login).toHaveBeenCalledWith({
                email: 'test@example.com',
                password: 'password123'
            });
            expect(mockOnSuccess).toHaveBeenCalledWith({
                success: true,
                user: { id: 1, email: 'test@example.com' },
                token: 'mockToken'
            });
        });
    });

    it('should handle login errors', async () => {
        // Arrange
        const user = userEvent.setup();
        const AuthService = require('../../../../src/services/AuthService');
        AuthService.login.mockResolvedValue({
            success: false,
            error: 'Credenciales inválidas'
        });

        render(<LoginForm {...defaultProps} />);

        // Act
        const emailInput = screen.getByLabelText(/email/i);
        const passwordInput = screen.getByLabelText(/contraseña/i);
        const submitButton = screen.getByRole('button', { name: /iniciar sesión/i });

        await user.type(emailInput, 'test@example.com');
        await user.type(passwordInput, 'wrongpassword');
        await user.click(submitButton);

        // Assert
        await waitFor(() => {
            expect(mockOnError).toHaveBeenCalledWith('Credenciales inválidas');
            expect(screen.getByText(/credenciales inválidas/i)).toBeInTheDocument();
        });
    });

    it('should disable submit button while loading', async () => {
        // Arrange
        const user = userEvent.setup();
        const AuthService = require('../../../../src/services/AuthService');
        
        // Mock a delayed response
        AuthService.login.mockImplementation(
            () => new Promise(resolve => setTimeout(() => resolve({ success: true }), 1000))
        );

        render(<LoginForm {...defaultProps} />);

        // Act
        const emailInput = screen.getByLabelText(/email/i);
        const passwordInput = screen.getByLabelText(/contraseña/i);
        const submitButton = screen.getByRole('button', { name: /iniciar sesión/i });

        await user.type(emailInput, 'test@example.com');
        await user.type(passwordInput, 'password123');
        await user.click(submitButton);

        // Assert
        expect(submitButton).toBeDisabled();
        expect(screen.getByText(/iniciando sesión/i)).toBeInTheDocument();
    });

    it('should toggle password visibility', async () => {
        // Arrange
        const user = userEvent.setup();
        render(<LoginForm {...defaultProps} />);

        // Act
        const passwordInput = screen.getByLabelText(/contraseña/i);
        const toggleButton = screen.getByRole('button', { name: /mostrar contraseña/i });

        expect(passwordInput).toHaveAttribute('type', 'password');
        
        await user.click(toggleButton);

        // Assert
        expect(passwordInput).toHaveAttribute('type', 'text');
        expect(screen.getByRole('button', { name: /ocultar contraseña/i })).toBeInTheDocument();
    });

    it('should clear form after successful submission', async () => {
        // Arrange
        const user = userEvent.setup();
        const AuthService = require('../../../../src/services/AuthService');
        AuthService.login.mockResolvedValue({ success: true });

        render(<LoginForm {...defaultProps} />);

        // Act
        const emailInput = screen.getByLabelText(/email/i);
        const passwordInput = screen.getByLabelText(/contraseña/i);
        const submitButton = screen.getByRole('button', { name: /iniciar sesión/i });

        await user.type(emailInput, 'test@example.com');
        await user.type(passwordInput, 'password123');
        await user.click(submitButton);

        // Assert
        await waitFor(() => {
            expect(emailInput).toHaveValue('');
            expect(passwordInput).toHaveValue('');
        });
    });
});