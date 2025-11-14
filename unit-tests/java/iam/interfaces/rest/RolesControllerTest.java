package upc.edu.pe.parkeoya.backend.v1.iam.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.entities.Role;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.queries.GetAllRolesQuery;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.model.valueobjects.Roles;
import upc.edu.pe.parkeoya.backend.v1.iam.domain.services.RoleQueryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RolesController.class)
class RolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleQueryService roleQueryService;

    private List<Role> testRoles;

    @BeforeEach
    void setUp() {
        Role driverRole = new Role();
        driverRole.setId(1L);
        driverRole.setName(Roles.ROLE_DRIVER);

        Role parkingOwnerRole = new Role();
        parkingOwnerRole.setId(2L);
        parkingOwnerRole.setName(Roles.ROLE_PARKING_OWNER);

        testRoles = List.of(driverRole, parkingOwnerRole);
    }

    @Test
    void getAllRoles_WhenRolesExist_ShouldReturnRolesList() throws Exception {
        // Arrange
        when(roleQueryService.handle(any(GetAllRolesQuery.class)))
            .thenReturn(testRoles);

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpected(jsonPath("$[0].name").value("ROLE_DRIVER"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpected(jsonPath("$[1].name").value("ROLE_PARKING_OWNER"));
    }

    @Test
    void getAllRoles_WhenNoRolesExist_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(roleQueryService.handle(any(GetAllRolesQuery.class)))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllRoles_ShouldAcceptGetRequest() throws Exception {
        // Arrange
        when(roleQueryService.handle(any(GetAllRolesQuery.class)))
            .thenReturn(testRoles);

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles"))
                .andExpected(status().isOk());
    }

    @Test
    void getAllRoles_ShouldProduceApplicationJson() throws Exception {
        // Arrange
        when(roleQueryService.handle(any(GetAllRolesQuery.class)))
            .thenReturn(testRoles);

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));
    }
}