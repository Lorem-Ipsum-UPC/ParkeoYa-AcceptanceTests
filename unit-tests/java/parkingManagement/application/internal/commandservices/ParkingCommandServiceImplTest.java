package upc.edu.pe.parkeoya.backend.v1.parkingManagement.application.internal.commandservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upc.edu.pe.parkeoya.backend.v1.deviceManagement.domain.model.aggregates.EdgeServer;
import upc.edu.pe.parkeoya.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories.EdgeServerRepository;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.application.internal.outboundservices.acl.ExternalDeviceService;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.commands.*;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import upc.edu.pe.parkeoya.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingCommandServiceImplTest {

    @Mock
    private ParkingRepository parkingRepository;

    @Mock
    private EdgeServerRepository edgeServerRepository;

    @Mock
    private ExternalDeviceService externalDeviceService;

    @InjectMocks
    private ParkingCommandServiceImpl parkingCommandService;

    private Parking testParking;
    private EdgeServer testEdgeServer;

    @BeforeEach
    void setUp() {
        CreateParkingCommand createCommand = new CreateParkingCommand(
            1L, "Test Parking", "Description", "Address", 
            -12.0463, -77.0428, 10.0f, 50, 5, 10
        );
        testParking = new Parking(createCommand);
        testParking.setId(1L);

        testEdgeServer = new EdgeServer(1L);
        testEdgeServer.setServerId(UUID.randomUUID());
    }

    @Test
    void handleCreateParkingCommand_WhenValidCommand_ShouldCreateParkingAndEdgeServer() {
        // Arrange
        CreateParkingCommand command = new CreateParkingCommand(
            1L, "Test Parking", "Description", "Address", 
            -12.0463, -77.0428, 10.0f, 50, 5, 10
        );
        
        when(parkingRepository.save(any(Parking.class))).thenReturn(testParking);
        when(edgeServerRepository.save(any(EdgeServer.class))).thenReturn(testEdgeServer);

        // Act
        Optional<Parking> result = parkingCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testParking, result.get());
        
        verify(parkingRepository).save(any(Parking.class));
        verify(edgeServerRepository).save(any(EdgeServer.class));
    }

    @Test
    void handleAddParkingSpotCommand_WhenValidCommand_ShouldAddParkingSpot() {
        // Arrange
        AddParkingSpotCommand command = new AddParkingSpotCommand(1L, "A1", "available");
        ParkingSpot mockParkingSpot = mock(ParkingSpot.class);
        UUID spotId = UUID.randomUUID();
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.of(testParking));
        when(mockParkingSpot.getId()).thenReturn(spotId);
        when(mockParkingSpot.getStatus()).thenReturn("available");
        when(mockParkingSpot.getLabel()).thenReturn("A1");
        when(mockParkingSpot.getParkingId()).thenReturn(1L);
        when(testParking.addParkingSpot(command)).thenReturn(mockParkingSpot);
        when(parkingRepository.save(testParking)).thenReturn(testParking);
        when(testParking.getParkingSpots()).thenReturn(java.util.List.of(mockParkingSpot));
        when(edgeServerRepository.findByParkingId_ParkingId(command.parkingId()))
            .thenReturn(Optional.of(testEdgeServer));

        // Act
        Optional<ParkingSpot> result = parkingCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockParkingSpot, result.get());
        
        verify(parkingRepository).findById(command.parkingId());
        verify(testParking).addParkingSpot(command);
        verify(parkingRepository).save(testParking);
        verify(edgeServerRepository).findByParkingId_ParkingId(command.parkingId());
        verify(externalDeviceService).createDevice(
            command.parkingId(), spotId, "available", "A1", testEdgeServer.getServerId()
        );
    }

    @Test
    void handleAddParkingSpotCommand_WhenParkingNotFound_ShouldThrowException() {
        // Arrange
        AddParkingSpotCommand command = new AddParkingSpotCommand(999L, "A1", "available");
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parkingCommandService.handle(command));
        assertEquals("Parking not found", exception.getMessage());
        
        verify(parkingRepository).findById(command.parkingId());
        verify(parkingRepository, never()).save(any());
        verify(externalDeviceService, never()).createDevice(any(), any(), any(), any(), any());
    }

    @Test
    void handleAddParkingSpotCommand_WhenEdgeServerNotFound_ShouldThrowException() {
        // Arrange
        AddParkingSpotCommand command = new AddParkingSpotCommand(1L, "A1", "available");
        ParkingSpot mockParkingSpot = mock(ParkingSpot.class);
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.of(testParking));
        when(testParking.addParkingSpot(command)).thenReturn(mockParkingSpot);
        when(parkingRepository.save(testParking)).thenReturn(testParking);
        when(edgeServerRepository.findByParkingId_ParkingId(command.parkingId()))
            .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parkingCommandService.handle(command));
        assertEquals("Edge Server not found for parking", exception.getMessage());
        
        verify(edgeServerRepository).findByParkingId_ParkingId(command.parkingId());
        verify(externalDeviceService, never()).createDevice(any(), any(), any(), any(), any());
    }

    @Test
    void handleUpdateParkingSpotAvailabilityCommand_WhenValidCommand_ShouldUpdateAvailability() {
        // Arrange
        UUID spotId = UUID.randomUUID();
        UpdateParkingSpotAvailabilityCommand command = new UpdateParkingSpotAvailabilityCommand(
            1L, spotId, "occupied"
        );
        ParkingSpot mockParkingSpot = mock(ParkingSpot.class);
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.of(testParking));
        when(testParking.getParkingSpot(spotId)).thenReturn(mockParkingSpot);
        when(parkingRepository.save(testParking)).thenReturn(testParking);

        // Act
        Optional<String> result = parkingCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("Parking spot with ID " + spotId + " availability updated to occupied"));
        
        verify(parkingRepository).findById(command.parkingId());
        verify(testParking).getParkingSpot(spotId);
        verify(mockParkingSpot).updateStatus("occupied");
        verify(parkingRepository).save(testParking);
    }

    @Test
    void handleUpdateParkingSpotAvailabilityCommand_WhenParkingNotFound_ShouldThrowException() {
        // Arrange
        UUID spotId = UUID.randomUUID();
        UpdateParkingSpotAvailabilityCommand command = new UpdateParkingSpotAvailabilityCommand(
            999L, spotId, "occupied"
        );
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parkingCommandService.handle(command));
        assertEquals("Parking not found", exception.getMessage());
        
        verify(parkingRepository).findById(command.parkingId());
        verify(parkingRepository, never()).save(any());
    }

    @Test
    void handleUpdateParkingSpotAvailabilityCommand_WhenParkingSpotNotFound_ShouldThrowException() {
        // Arrange
        UUID spotId = UUID.randomUUID();
        UpdateParkingSpotAvailabilityCommand command = new UpdateParkingSpotAvailabilityCommand(
            1L, spotId, "occupied"
        );
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.of(testParking));
        when(testParking.getParkingSpot(spotId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parkingCommandService.handle(command));
        assertEquals("Parking spot not found", exception.getMessage());
        
        verify(parkingRepository).findById(command.parkingId());
        verify(testParking).getParkingSpot(spotId);
        verify(parkingRepository, never()).save(any());
    }

    @Test
    void handleUpdateAvailableParkingSpotCountCommand_WhenValidCommand_ShouldUpdateCount() {
        // Arrange
        UpdateAvailableParkingSpotCountCommand command = new UpdateAvailableParkingSpotCountCommand(
            1L, 5, "increment"
        );
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.of(testParking));
        when(parkingRepository.save(testParking)).thenReturn(testParking);

        // Act
        Optional<String> result = parkingCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("Available parking spots count updated"));
        
        verify(parkingRepository).findById(command.parkingId());
        verify(testParking).updateAvailableSpotsCount(5, "increment");
        verify(parkingRepository).save(testParking);
    }

    @Test
    void handleUpdateAvailableParkingSpotCountCommand_WhenParkingNotFound_ShouldThrowException() {
        // Arrange
        UpdateAvailableParkingSpotCountCommand command = new UpdateAvailableParkingSpotCountCommand(
            999L, 5, "increment"
        );
        
        when(parkingRepository.findById(command.parkingId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> parkingCommandService.handle(command));
        assertEquals("Parking not found", exception.getMessage());
        
        verify(parkingRepository).findById(command.parkingId());
        verify(parkingRepository, never()).save(any());
    }
}