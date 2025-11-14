package upc.edu.pe.parkeoya.backend.v1.reservations.application.internal.commandservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upc.edu.pe.parkeoya.backend.v1.deviceManagement.infrastructure.gateway.ParkingMqttService;
import upc.edu.pe.parkeoya.backend.v1.notifications.application.service.NotificationService;
import upc.edu.pe.parkeoya.backend.v1.notifications.domain.model.FcmToken;
import upc.edu.pe.parkeoya.backend.v1.notifications.domain.repository.FcmTokenRepository;
import upc.edu.pe.parkeoya.backend.v1.reservations.application.internal.outboundservices.acl.ExternalParkingService;
import upc.edu.pe.parkeoya.backend.v1.reservations.application.internal.outboundservices.acl.ExternalProfileService;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.aggregates.Reservation;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.commands.CreateReservationCommand;
import upc.edu.pe.parkeoya.backend.v1.reservations.domain.model.commands.UpdateReservationStatusCommand;
import upc.edu.pe.parkeoya.backend.v1.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCommandServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ExternalParkingService externalParkingService;

    @Mock
    private ExternalProfileService externalProfileServiceReservation;

    @Mock
    private ParkingMqttService parkingMqttService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private ReservationCommandServiceImpl reservationCommandService;

    private CreateReservationCommand createCommand;
    private UpdateReservationStatusCommand updateCommand;
    private Reservation testReservation;
    private UUID parkingSpotId;

    @BeforeEach
    void setUp() {
        parkingSpotId = UUID.randomUUID();
        createCommand = new CreateReservationCommand(
            1L, 1L, parkingSpotId,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(3),
            2
        );

        updateCommand = new UpdateReservationStatusCommand(1L, "COMPLETED");

        // Create test reservation with mock
        testReservation = mock(Reservation.class);
        when(testReservation.getId()).thenReturn(1L);
        when(testReservation.getDriverId()).thenReturn(1L);
        when(testReservation.getParkingId()).thenReturn(1L);
        when(testReservation.getParkingSpotId()).thenReturn(parkingSpotId);
        when(testReservation.getStatus()).thenReturn("ACTIVE");
    }

    @Test
    void handleCreateReservationCommand_WhenValidData_ShouldCreateReservation() throws IOException {
        // Arrange
        String driverFullName = "John Doe";
        String spotLabel = "A1";
        Float ratePerHour = 10.0f;
        
        when(externalProfileServiceReservation.getDriverFullNameByUserId(createCommand.driverId()))
            .thenReturn(driverFullName);
        when(externalParkingService.getSpotLabel(createCommand.parkingSpotId(), createCommand.parkingId()))
            .thenReturn(spotLabel);
        when(externalParkingService.getParkingRatePerHour(createCommand.parkingId()))
            .thenReturn(ratePerHour);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        Optional<Reservation> result = reservationCommandService.handle(createCommand);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testReservation, result.get());
        
        verify(externalProfileServiceReservation).getDriverFullNameByUserId(createCommand.driverId());
        verify(externalParkingService).getSpotLabel(createCommand.parkingSpotId(), createCommand.parkingId());
        verify(externalParkingService).getParkingRatePerHour(createCommand.parkingId());
        verify(reservationRepository).save(any(Reservation.class));
        verify(externalParkingService).updateParkingSpotAvailability(
            createCommand.parkingId(), createCommand.parkingSpotId(), "RESERVED"
        );
    }

    @Test
    void handleCreateReservationCommand_WhenNullRatePerHour_ShouldReturnEmpty() throws IOException {
        // Arrange
        String driverFullName = "John Doe";
        String spotLabel = "A1";
        
        when(externalProfileServiceReservation.getDriverFullNameByUserId(createCommand.driverId()))
            .thenReturn(driverFullName);
        when(externalParkingService.getSpotLabel(createCommand.parkingSpotId(), createCommand.parkingId()))
            .thenReturn(spotLabel);
        when(externalParkingService.getParkingRatePerHour(createCommand.parkingId()))
            .thenReturn(null);

        // Act
        Optional<Reservation> result = reservationCommandService.handle(createCommand);

        // Assert
        assertTrue(result.isEmpty());
        
        verify(externalProfileServiceReservation).getDriverFullNameByUserId(createCommand.driverId());
        verify(externalParkingService).getSpotLabel(createCommand.parkingSpotId(), createCommand.parkingId());
        verify(externalParkingService).getParkingRatePerHour(createCommand.parkingId());
        verify(reservationRepository, never()).save(any());
        verify(externalParkingService, never()).updateParkingSpotAvailability(any(), any(), any());
    }

    @Test
    void handleCreateReservationCommand_ShouldUpdateAvailableSpotsCount() throws IOException {
        // Arrange
        String driverFullName = "John Doe";
        String spotLabel = "A1";
        Float ratePerHour = 10.0f;
        
        when(externalProfileServiceReservation.getDriverFullNameByUserId(createCommand.driverId()))
            .thenReturn(driverFullName);
        when(externalParkingService.getSpotLabel(createCommand.parkingSpotId(), createCommand.parkingId()))
            .thenReturn(spotLabel);
        when(externalParkingService.getParkingRatePerHour(createCommand.parkingId()))
            .thenReturn(ratePerHour);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        reservationCommandService.handle(createCommand);

        // Assert
        verify(externalParkingService).updateAvailableSpotsCount(
            createCommand.parkingId(), 1, "decrement"
        );
    }

    @Test
    void handleUpdateReservationStatusCommand_WhenValidData_ShouldUpdateStatus() {
        // Arrange
        when(reservationRepository.findById(updateCommand.reservationId()))
            .thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        Optional<Reservation> result = reservationCommandService.handle(updateCommand);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testReservation, result.get());
        
        verify(reservationRepository).findById(updateCommand.reservationId());
        verify(testReservation).updateStatus(updateCommand.status());
        verify(reservationRepository).save(testReservation);
    }

    @Test
    void handleUpdateReservationStatusCommand_WhenReservationNotFound_ShouldReturnEmpty() {
        // Arrange
        when(reservationRepository.findById(updateCommand.reservationId()))
            .thenReturn(Optional.empty());

        // Act
        Optional<Reservation> result = reservationCommandService.handle(updateCommand);

        // Assert
        assertTrue(result.isEmpty());
        
        verify(reservationRepository).findById(updateCommand.reservationId());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void handleUpdateReservationStatusCommand_WhenStatusIsCompleted_ShouldUpdateSpotAvailability() {
        // Arrange
        UpdateReservationStatusCommand completedCommand = new UpdateReservationStatusCommand(1L, "COMPLETED");
        
        when(reservationRepository.findById(completedCommand.reservationId()))
            .thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        reservationCommandService.handle(completedCommand);

        // Assert
        verify(externalParkingService).updateParkingSpotAvailability(
            testReservation.getParkingId(),
            testReservation.getParkingSpotId(),
            "AVAILABLE"
        );
        verify(externalParkingService).updateAvailableSpotsCount(
            testReservation.getParkingId(), 1, "increment"
        );
    }

    @Test
    void handleUpdateReservationStatusCommand_WhenStatusIsCancelled_ShouldUpdateSpotAvailability() {
        // Arrange
        UpdateReservationStatusCommand cancelledCommand = new UpdateReservationStatusCommand(1L, "CANCELLED");
        
        when(reservationRepository.findById(cancelledCommand.reservationId()))
            .thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        reservationCommandService.handle(cancelledCommand);

        // Assert
        verify(externalParkingService).updateParkingSpotAvailability(
            testReservation.getParkingId(),
            testReservation.getParkingSpotId(),
            "AVAILABLE"
        );
        verify(externalParkingService).updateAvailableSpotsCount(
            testReservation.getParkingId(), 1, "increment"
        );
    }

    @Test
    void handleUpdateReservationStatusCommand_WhenStatusIsActive_ShouldNotUpdateSpotAvailability() {
        // Arrange
        UpdateReservationStatusCommand activeCommand = new UpdateReservationStatusCommand(1L, "ACTIVE");
        
        when(reservationRepository.findById(activeCommand.reservationId()))
            .thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        reservationCommandService.handle(activeCommand);

        // Assert
        verify(externalParkingService, never()).updateParkingSpotAvailability(any(), any(), any());
        verify(externalParkingService, never()).updateAvailableSpotsCount(any(), any(), any());
    }

    @Test
    void handleCreateReservationCommand_ShouldSendNotificationWhenFcmTokenExists() throws IOException {
        // Arrange
        String driverFullName = "John Doe";
        String spotLabel = "A1";
        Float ratePerHour = 10.0f;
        FcmToken fcmToken = mock(FcmToken.class);
        when(fcmToken.getToken()).thenReturn("test-fcm-token");
        
        when(externalProfileServiceReservation.getDriverFullNameByUserId(createCommand.driverId()))
            .thenReturn(driverFullName);
        when(externalParkingService.getSpotLabel(createCommand.parkingSpotId(), createCommand.parkingId()))
            .thenReturn(spotLabel);
        when(externalParkingService.getParkingRatePerHour(createCommand.parkingId()))
            .thenReturn(ratePerHour);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(fcmTokenRepository.findByUserId(createCommand.driverId()))
            .thenReturn(List.of(fcmToken));

        // Act
        reservationCommandService.handle(createCommand);

        // Assert
        verify(notificationService).sendNotificationToUser(
            eq("test-fcm-token"),
            eq("Reserva confirmada"),
            contains("Tu reserva ha sido confirmada")
        );
    }
}