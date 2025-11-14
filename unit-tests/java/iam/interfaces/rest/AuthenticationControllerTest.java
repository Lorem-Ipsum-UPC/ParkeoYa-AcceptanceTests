package upc.edu.pe.parkeoya.backend.v1.iam.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.aggregates.User;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.commands.SignInCommand;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.commands.SignUpDriverCommand;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.commands.SignUpParkingOwnerCommand;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.services.UserCommandService;
import upc.edu.pe.parkeoya.backend.v1.iam.interfaces.rest.resources.SignInResource;
import upc.edu.pe.parkeoya.backend.v1.iam.interfaces.rest.resources.SignUpDriverResource;
import upc.edu.pe.parkeoya.backend.v1.iam.interfaces.rest.resources.SignUpParkingOwnerResource;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserCommandService userCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User("test@test.com", "hashedPassword");
        testUser.setId(1L);
        testToken = "test-jwt-token";
    }

    @Test
    void signIn_WhenValidCredentials_ShouldReturnAuthenticatedUser() throws Exception {
        // Arrange
        SignInResource signInResource = new SignInResource("test@test.com", "password");
        ImmutablePair<User, String> authenticatedUser = ImmutablePair.of(testUser, testToken);
        
        when(userCommandService.handle(any(SignInCommand.class)))
            .thenReturn(Optional.of(authenticatedUser));

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInResource)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpected(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.token").value(testToken));
    }

    @Test
    void signIn_WhenInvalidCredentials_ShouldReturnNotFound() throws Exception {
        // Arrange
        SignInResource signInResource = new SignInResource("test@test.com", "wrongPassword");
        
        when(userCommandService.handle(any(SignInCommand.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInResource)))
                .andExpect(status().isNotFound());
    }

    @Test
    void signUpDriver_WhenValidData_ShouldCreateDriver() throws Exception {
        // Arrange
        SignUpDriverResource signUpDriverResource = new SignUpDriverResource(
            "driver@test.com", "password", "John Doe", "Lima", 
            "Peru", "123456789", "12345678"
        );
        
        when(userCommandService.handle(any(SignUpDriverCommand.class)))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-up/driver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDriverResource)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpected(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void signUpDriver_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        SignUpDriverResource signUpDriverResource = new SignUpDriverResource(
            "invalid-email", "password", "John Doe", "Lima", 
            "Peru", "123456789", "12345678"
        );
        
        when(userCommandService.handle(any(SignUpDriverCommand.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-up/driver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDriverResource)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUpParkingOwner_WhenValidData_ShouldCreateParkingOwner() throws Exception {
        // Arrange
        SignUpParkingOwnerResource signUpParkingOwnerResource = new SignUpParkingOwnerResource(
            "owner@test.com", "password", "Jane Doe", "Lima", 
            "Peru", "123456789", "My Company", "12345678901"
        );
        
        when(userCommandService.handle(any(SignUpParkingOwnerCommand.class)))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-up/parking-owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpParkingOwnerResource)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void signUpParkingOwner_WhenEmailAlreadyExists_ShouldReturnBadRequest() throws Exception {
        // Arrange
        SignUpParkingOwnerResource signUpParkingOwnerResource = new SignUpParkingOwnerResource(
            "existing@test.com", "password", "Jane Doe", "Lima", 
            "Peru", "123456789", "My Company", "12345678901"
        );
        
        when(userCommandService.handle(any(SignUpParkingOwnerCommand.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-up/parking-owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpParkingOwnerResource)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signIn_WhenMissingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"password\":\"password\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUpDriver_WhenMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"email\":\"test@test.com\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/authentication/sign-up/driver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}