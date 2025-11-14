package upc.edu.pe.parkeoya.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
public class ParkeoyaIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("parkeoya_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully with TestContainers
    }

    @Test
    void testGetAllRoles_IntegrationTest() throws Exception {
        // Test the roles endpoint
        mockMvc.perform(get("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllParkings_IntegrationTest() throws Exception {
        // Test the parkings endpoint
        mockMvc.perform(get("/api/v1/parkings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAuthenticationEndpoints_IntegrationTest() throws Exception {
        // Test that authentication endpoints are accessible
        String signInJson = """
            {
                "email": "nonexistent@test.com",
                "password": "password"
            }
            """;

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signInJson))
                .andExpect(status().isInternalServerError()); // Expected since user doesn't exist
    }

    @Test
    void testCreateParking_InvalidData_IntegrationTest() throws Exception {
        // Test creating parking with invalid data
        String invalidParkingJson = """
            {
                "ownerId": 999,
                "name": "",
                "description": ""
            }
            """;

        mockMvc.perform(post("/api/v1/parkings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidParkingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReservation_InvalidData_IntegrationTest() throws Exception {
        // Test creating reservation with invalid data
        String invalidReservationJson = """
            {
                "driverId": 999,
                "parkingId": 999
            }
            """;

        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidReservationJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDatabaseConnection_IntegrationTest() {
        // Test that the database connection is working
        assert mysql.isRunning();
        assert mysql.getJdbcUrl().contains("parkeoya_test");
    }

    @Test
    void testApplicationHealthEndpoints_IntegrationTest() throws Exception {
        // Test basic application endpoints are responding
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/parkings"))
                .andExpect(status().isOk());
    }
}