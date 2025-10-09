const ParkingService = require('../../../../src/services/ParkingService');
const Parking = require('../../../../src/models/Parking');
const Space = require('../../../../src/models/Space');

// Mock dependencies
jest.mock('../../../../src/models/Parking');
jest.mock('../../../../src/models/Space');

describe('ParkingService', () => {
    let parkingService;

    beforeEach(() => {
        parkingService = new ParkingService();
        jest.clearAllMocks();
    });

    describe('createParking', () => {
        it('should create parking successfully with valid data', async () => {
            // Arrange
            const parkingData = {
                name: 'Test Parking',
                address: 'Test Address 123',
                capacity: 50,
                ownerId: 1,
                hourlyRate: 5.00,
                operatingHours: '24/7'
            };

            const mockCreatedParking = {
                id: 1,
                ...parkingData,
                createdAt: new Date()
            };

            Parking.create.mockResolvedValue(mockCreatedParking);
            Space.bulkCreate.mockResolvedValue([]);

            // Act
            const result = await parkingService.createParking(parkingData);

            // Assert
            expect(result.success).toBe(true);
            expect(result.parking).toEqual(mockCreatedParking);
            expect(Parking.create).toHaveBeenCalledWith(parkingData);
        });

        it('should reject parking with duplicate address', async () => {
            // Arrange
            const parkingData = {
                name: 'Test Parking',
                address: 'Duplicate Address 123',
                capacity: 50,
                ownerId: 1
            };

            Parking.findByAddress.mockResolvedValue({ id: 1, address: parkingData.address });

            // Act
            const result = await parkingService.createParking(parkingData);

            // Assert
            expect(result.success).toBe(false);
            expect(result.error).toBe('Ya existe un estacionamiento en esta dirección');
            expect(Parking.create).not.toHaveBeenCalled();
        });

        it('should reject parking with invalid capacity', async () => {
            // Arrange
            const parkingData = {
                name: 'Test Parking',
                address: 'Test Address 123',
                capacity: 0,
                ownerId: 1
            };

            // Act
            const result = await parkingService.createParking(parkingData);

            // Assert
            expect(result.success).toBe(false);
            expect(result.error).toBe('La capacidad debe ser mayor a 0');
        });
    });

    describe('updateParking', () => {
        it('should update parking successfully', async () => {
            // Arrange
            const parkingId = 1;
            const updateData = {
                name: 'Updated Parking Name',
                hourlyRate: 6.00
            };

            const existingParking = {
                id: parkingId,
                name: 'Old Name',
                address: 'Test Address',
                update: jest.fn().mockResolvedValue(true)
            };

            Parking.findById.mockResolvedValue(existingParking);

            // Act
            const result = await parkingService.updateParking(parkingId, updateData);

            // Assert
            expect(result.success).toBe(true);
            expect(existingParking.update).toHaveBeenCalledWith(updateData);
        });

        it('should handle non-existent parking', async () => {
            // Arrange
            const parkingId = 999;
            const updateData = { name: 'New Name' };

            Parking.findById.mockResolvedValue(null);

            // Act
            const result = await parkingService.updateParking(parkingId, updateData);

            // Assert
            expect(result.success).toBe(false);
            expect(result.error).toBe('Estacionamiento no encontrado');
        });
    });

    describe('getParkingsByOwner', () => {
        it('should return parkings for valid owner', async () => {
            // Arrange
            const ownerId = 1;
            const mockParkings = [
                { id: 1, name: 'Parking 1', ownerId },
                { id: 2, name: 'Parking 2', ownerId }
            ];

            Parking.findByOwnerId.mockResolvedValue(mockParkings);

            // Act
            const result = await parkingService.getParkingsByOwner(ownerId);

            // Assert
            expect(result).toEqual(mockParkings);
            expect(Parking.findByOwnerId).toHaveBeenCalledWith(ownerId);
        });

        it('should return empty array for owner with no parkings', async () => {
            // Arrange
            const ownerId = 1;

            Parking.findByOwnerId.mockResolvedValue([]);

            // Act
            const result = await parkingService.getParkingsByOwner(ownerId);

            // Assert
            expect(result).toEqual([]);
        });
    });

    describe('deleteParking', () => {
        it('should delete parking successfully', async () => {
            // Arrange
            const parkingId = 1;
            const mockParking = {
                id: parkingId,
                destroy: jest.fn().mockResolvedValue(true)
            };

            Parking.findById.mockResolvedValue(mockParking);

            // Act
            const result = await parkingService.deleteParking(parkingId);

            // Assert
            expect(result.success).toBe(true);
            expect(mockParking.destroy).toHaveBeenCalled();
        });

        it('should handle deletion of non-existent parking', async () => {
            // Arrange
            const parkingId = 999;

            Parking.findById.mockResolvedValue(null);

            // Act
            const result = await parkingService.deleteParking(parkingId);

            // Assert
            expect(result.success).toBe(false);
            expect(result.error).toBe('Estacionamiento no encontrado');
        });
    });

    describe('searchParkings', () => {
        it('should return parkings matching search criteria', async () => {
            // Arrange
            const searchCriteria = {
                location: 'Lima',
                maxDistance: 5,
                priceRange: { min: 0, max: 10 }
            };

            const mockParkings = [
                { id: 1, name: 'Parking 1', address: 'Lima Centro' },
                { id: 2, name: 'Parking 2', address: 'Lima Norte' }
            ];

            Parking.search.mockResolvedValue(mockParkings);

            // Act
            const result = await parkingService.searchParkings(searchCriteria);

            // Assert
            expect(result).toEqual(mockParkings);
            expect(Parking.search).toHaveBeenCalledWith(searchCriteria);
        });
    });
});