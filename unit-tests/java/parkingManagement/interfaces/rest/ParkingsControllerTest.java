package upc.edu.pe.parkeoya.backend.v1.parkingManagement.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.commands.CreateParkingCommand;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.queries.GetAllParkingQuery;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.queries.GetParkingByIdQuery;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.queries.GetParkingSpotsByParkingIdQuery;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.services.ParkingCommandService;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.services.ParkingQueryService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingsController.class)
class ParkingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingCommandService parkingCommandService;

    @MockitoBean
    private ParkingQueryService parkingQueryService;

    private Parking testParking;

    @BeforeEach
    void setUp() {
        CreateParkingCommand createCommand = new CreateParkingCommand(
            1L, "Test Parking", "Description", "Test Address", 
            -12.0463, -77.0428, 10.0f, 50, 50, 5, 10, "image.jpg"
        );
        testParking = new Parking(createCommand);
        // Use reflection to set ID since there might not be a public setter
        try {
            var idField = testParking.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testParking, 1L);
        } catch (Exception ignored) {
            // If reflection fails, continue without setting ID
        }
    }

    @Test
    void createParking_WhenValidData_ShouldCreateParking() throws Exception {
        // Arrange
        String createParkingJson = """
            {
                "ownerId": 1,
                "name": "Test Parking",
                "description": "Description",
                "address": "Test Address",
                "lat": -12.0463,
                "lng": -77.0428,
                "ratePerHour": 10.0,
                "totalSpots": 50,
                "availableSpots": 50,
                "totalRows": 5,
                "totalColumns": 10,
                "imageUrl": "image.jpg"
            }
            """;
        
        when(parkingCommandService.handle(any(CreateParkingCommand.class))).thenReturn(Optional.of(testParking));

        // Act & Assert
        mockMvc.perform(post("/api/v1/parkings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createParkingJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createParking_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = """
            {
                "ownerId": 1,
                "name": "",
                "description": "",
                "address": ""
            }
            """;
        
        when(parkingCommandService.handle(any(CreateParkingCommand.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/parkings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllParkings_WhenParkingsExist_ShouldReturnParkingsList() throws Exception {
        // Arrange
        List<Parking> parkings = List.of(testParking);
        
        when(parkingQueryService.handle(any(GetAllParkingQuery.class)))
            .thenReturn(parkings);

        // Act & Assert
        mockMvc.perform(get("/api/v1/parkings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllParkings_WhenNoParkingsExist_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(parkingQueryService.handle(any(GetAllParkingQuery.class)))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/parkings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getParkingById_WhenParkingExists_ShouldReturnParking() throws Exception {
        // Arrange
        Long parkingId = 1L;
        
        when(parkingQueryService.handle(any(GetParkingByIdQuery.class)))
            .thenReturn(Optional.of(testParking));

        // Act & Assert
        mockMvc.perform(get("/api/v1/parkings/{parkingId}", parkingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getParkingById_WhenParkingNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long parkingId = 999L;
        
        when(parkingQueryService.handle(any(GetParkingByIdQuery.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/parkings/{parkingId}", parkingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getParkingSpotsByParkingId_WhenSpotsExist_ShouldReturnSpotsList() throws Exception {
        // Arrange
        Long parkingId = 1L;
        ParkingSpot mockParkingSpot = org.mockito.Mockito.mock(ParkingSpot.class);
        List<ParkingSpot> spots = List.of(mockParkingSpot);
        
        when(parkingQueryService.handle(any(GetParkingSpotsByParkingIdQuery.class)))
            .thenReturn(spots);

        // Act & Assert
        mockMvc.perform(get("/api/v1/parkings/{parkingId}/spots", parkingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getParkingSpotsByParkingId_WhenNoSpotsExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long parkingId = 1L;
        
        when(parkingQueryService.handle(any(GetParkingSpotsByParkingIdQuery.class)))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/parkings/{parkingId}/spots", parkingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createParking_WhenMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"ownerId\":1}"; // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/parkings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}