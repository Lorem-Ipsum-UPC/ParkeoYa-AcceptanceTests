package upc.edu.pe.parkeoya.backend.v1.reservations.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.aggregates.Reservation;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.commands.CreateReservationCommand;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdAndStatusQuery;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.services.ReservationCommandService;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.services.ReservationQueryService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationsController.class)
class ReservationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationCommandService reservationCommandService;

    @MockitoBean
    private ReservationQueryService reservationQueryService;

    private Reservation testReservation;
    private UUID parkingSpotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        parkingSpotId = UUID.randomUUID();
        startTime = LocalDateTime.now().plusHours(1);
        endTime = LocalDateTime.now().plusHours(3);
        
        // Create mock reservation
        testReservation = org.mockito.Mockito.mock(Reservation.class);
        when(testReservation.getId()).thenReturn(1L);
        when(testReservation.getDriverId()).thenReturn(1L);
        when(testReservation.getParkingId()).thenReturn(1L);
        when(testReservation.getStatus()).thenReturn("ACTIVE");
    }

    @Test
    void createReservation_WhenValidData_ShouldCreateReservation() throws Exception {
        // Arrange
        String createReservationJson = String.format("""
            {
                "driverId": 1,
                "parkingId": 1,
                "parkingSpotId": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "durationInHours": 2
            }
            """, 
            parkingSpotId.toString(),
            startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        when(reservationCommandService.handle(any(CreateReservationCommand.class)))
            .thenReturn(Optional.of(testReservation));

        // Act & Assert
        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createReservationJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createReservation_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = """
            {
                "driverId": 1,
                "parkingId": 1
            }
            """;
        
        when(reservationCommandService.handle(any(CreateReservationCommand.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReservationsByDriverIdAndStatus_WhenReservationsExist_ShouldReturnReservations() throws Exception {
        // Arrange
        Long driverId = 1L;
        String status = "ACTIVE";
        List<Reservation> reservations = List.of(testReservation);
        
        when(reservationQueryService.handle(any(GetAllReservationsByDriverIdAndStatusQuery.class)))
            .thenReturn(reservations);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reservations/driver/{driverId}/status/{status}", driverId, status))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReservationsByDriverIdAndStatus_WhenNoReservationsExist_ShouldReturnEmptyList() throws Exception {
        // Arrange
        Long driverId = 1L;
        String status = "ACTIVE";
        
        when(reservationQueryService.handle(any(GetAllReservationsByDriverIdAndStatusQuery.class)))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/reservations/driver/{driverId}/status/{status}", driverId, status))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getReservationsByParkingId_WhenReservationsExist_ShouldReturnReservations() throws Exception {
        // Arrange
        Long parkingId = 1L;
        List<Reservation> reservations = List.of(testReservation);
        
        when(reservationQueryService.handle(any(GetAllReservationsByParkingIdQuery.class)))
            .thenReturn(reservations);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reservations/parking/{parkingId}", parkingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReservationsByParkingId_WhenNoReservationsExist_ShouldReturnEmptyList() throws Exception {
        // Arrange
        Long parkingId = 1L;
        
        when(reservationQueryService.handle(any(GetAllReservationsByParkingIdQuery.class)))
            .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/reservations/parking/{parkingId}", parkingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createReservation_WhenMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"driverId\":1}"; // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReservation_WhenIOException_ShouldHandleException() throws Exception {
        // Arrange
        String createReservationJson = String.format("""
            {
                "driverId": 1,
                "parkingId": 1,
                "parkingSpotId": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "durationInHours": 2
            }
            """, 
            parkingSpotId.toString(),
            startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        when(reservationCommandService.handle(any(CreateReservationCommand.class)))
            .thenThrow(new java.io.IOException("Connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createReservationJson))
                .andExpect(status().isInternalServerError());
    }
}