package upc.edu.pe.parkeoya.backend.v1.iam.application.internal.commandservices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upc.edu.pe.parkeoya.backend.v1.iam.application.internal.outboundservices.acl.ExternalProfileService;
import upc.edu.pe.parkeoya.backend.v1.iam.application.internal.outboundservices.hashing.HashingService;
import upc.edu.pe.parkeoya.backend.v1.iam.application.internal.outboundservices.tokens.TokenService;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.aggregates.User;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.commands.SignInCommand;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.commands.SignUpDriverCommand;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.commands.SignUpParkingOwnerCommand;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.entities.Role;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.valueobjects.Roles;
import upc.edu.pe.parkeoya.backend.v1.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import upc.edu.pe.parkeoya.backend.v1.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private TokenService tokenService;

    @Mock
    private ExternalProfileService externalProfileService;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    private User testUser;
    private Role driverRole;
    private Role parkingOwnerRole;

    @BeforeEach
    void setUp() {
        testUser = new User("test@test.com", "hashedPassword");
        testUser.setId(1L);
        
        driverRole = new Role();
        driverRole.setName(Roles.ROLE_DRIVER);
        driverRole.setId(1L);
        
        parkingOwnerRole = new Role();
        parkingOwnerRole.setName(Roles.ROLE_PARKING_OWNER);
        parkingOwnerRole.setId(2L);
    }

    @Test
    void handleSignInCommand_WhenValidCredentials_ShouldReturnUserAndToken() {
        // Arrange
        SignInCommand command = new SignInCommand("test@test.com", "password");
        String expectedToken = "jwt-token";
        
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(testUser));
        when(hashingService.matches(command.password(), testUser.getPassword())).thenReturn(true);
        when(tokenService.generateToken(testUser.getEmail())).thenReturn(expectedToken);

        // Act
        Optional<ImmutablePair<User, String>> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get().getLeft());
        assertEquals(expectedToken, result.get().getRight());
        
        verify(userRepository).findByEmail(command.email());
        verify(hashingService).matches(command.password(), testUser.getPassword());
        verify(tokenService).generateToken(testUser.getEmail());
    }

    @Test
    void handleSignInCommand_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        SignInCommand command = new SignInCommand("nonexistent@test.com", "password");
        
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userCommandService.handle(command));
        assertEquals("User not found", exception.getMessage());
        
        verify(userRepository).findByEmail(command.email());
        verify(hashingService, never()).matches(any(), any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void handleSignInCommand_WhenInvalidPassword_ShouldThrowException() {
        // Arrange
        SignInCommand command = new SignInCommand("test@test.com", "wrongPassword");
        
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(testUser));
        when(hashingService.matches(command.password(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userCommandService.handle(command));
        assertEquals("Invalid password", exception.getMessage());
        
        verify(userRepository).findByEmail(command.email());
        verify(hashingService).matches(command.password(), testUser.getPassword());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void handleSignUpDriverCommand_WhenValidData_ShouldCreateDriverUser() {
        // Arrange
        SignUpDriverCommand command = new SignUpDriverCommand(
            "driver@test.com", "password", "John Doe", "Lima", 
            "Peru", "123456789", "12345678"
        );
        String hashedPassword = "hashedPassword";
        Long expectedDriverId = 1L;
        
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_DRIVER)).thenReturn(Optional.of(driverRole));
        when(hashingService.encode(command.password())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(externalProfileService.createDriver(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyLong())).thenReturn(expectedDriverId);

        // Act
        Optional<User> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        
        verify(userRepository).existsByEmail(command.email());
        verify(roleRepository).findByName(Roles.ROLE_DRIVER);
        verify(hashingService).encode(command.password());
        verify(userRepository).save(any(User.class));
        verify(externalProfileService).createDriver(
            command.fullName(), command.city(), command.country(),
            command.phone(), command.dni(), testUser.getId()
        );
    }

    @Test
    void handleSignUpDriverCommand_WhenEmailExists_ShouldThrowException() {
        // Arrange
        SignUpDriverCommand command = new SignUpDriverCommand(
            "existing@test.com", "password", "John Doe", "Lima", 
            "Peru", "123456789", "12345678"
        );
        
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userCommandService.handle(command));
        assertEquals("Email already exists", exception.getMessage());
        
        verify(userRepository).existsByEmail(command.email());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleSignUpDriverCommand_WhenRoleNotFound_ShouldThrowException() {
        // Arrange
        SignUpDriverCommand command = new SignUpDriverCommand(
            "driver@test.com", "password", "John Doe", "Lima", 
            "Peru", "123456789", "12345678"
        );
        
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_DRIVER)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userCommandService.handle(command));
        assertEquals("Driver role not found", exception.getMessage());
        
        verify(userRepository).existsByEmail(command.email());
        verify(roleRepository).findByName(Roles.ROLE_DRIVER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleSignUpParkingOwnerCommand_WhenValidData_ShouldCreateParkingOwnerUser() {
        // Arrange
        SignUpParkingOwnerCommand command = new SignUpParkingOwnerCommand(
            "owner@test.com", "password", "Jane Doe", "Lima", 
            "Peru", "123456789", "My Company", "12345678901"
        );
        String hashedPassword = "hashedPassword";
        Long expectedOwnerId = 1L;
        
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_PARKING_OWNER)).thenReturn(Optional.of(parkingOwnerRole));
        when(hashingService.encode(command.password())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(externalProfileService.createParkingOwner(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString(), anyLong())).thenReturn(expectedOwnerId);

        // Act
        Optional<User> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        
        verify(userRepository).existsByEmail(command.email());
        verify(roleRepository).findByName(Roles.ROLE_PARKING_OWNER);
        verify(hashingService).encode(command.password());
        verify(userRepository).save(any(User.class));
        verify(externalProfileService).createParkingOwner(
            command.fullName(), command.city(), command.country(),
            command.phone(), command.companyName(), command.ruc(), testUser.getId()
        );
    }

    @Test
    void handleSignUpParkingOwnerCommand_WhenProfileCreationFails_ShouldThrowException() {
        // Arrange
        SignUpParkingOwnerCommand command = new SignUpParkingOwnerCommand(
            "owner@test.com", "password", "Jane Doe", "Lima", 
            "Peru", "123456789", "My Company", "12345678901"
        );
        String hashedPassword = "hashedPassword";
        
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_PARKING_OWNER)).thenReturn(Optional.of(parkingOwnerRole));
        when(hashingService.encode(command.password())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(externalProfileService.createParkingOwner(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString(), anyLong())).thenReturn(0L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userCommandService.handle(command));
        assertEquals("Failed to create Parking Owner profile", exception.getMessage());
    }
}