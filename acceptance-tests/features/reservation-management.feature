# language: es

Característica: Gestión de reservas de estacionamiento
  Como conductor
  Quiero realizar y gestionar mis reservas de estacionamiento
  Para asegurar un espacio disponible cuando lo necesite

  Antecedentes:
    Dado que el servicio de API está disponible en "https://parkeoya-backend-latest-1.onrender.com"
    Y tengo credenciales válidas de conductor

  Escenario: Crear una nueva reserva exitosamente
    Dado que estoy autenticado como conductor con id "1"
    Y existe un estacionamiento con id "1" con espacios disponibles
    Y el espacio "A-05" está disponible
    Cuando envío una petición POST a "/api/v1/reservations" con los siguientes datos:
      | campo          | valor              |
      | driverId       | 1                  |
      | vehiclePlate   | ABC-123            |
      | parkingId      | 1                  |
      | parkingSpotId  | A-05               |
      | startTime      | 2025-11-15T10:00   |
      | endTime        | 2025-11-15T14:00   |
    Entonces debo recibir un código de estado 201
    Y la respuesta debe contener el "id" de la reserva
    Y la respuesta debe contener un "qrCode" para confirmar entrada
    Y el estado de la reserva debe ser "PENDING"

  Escenario: Error al crear reserva en espacio no disponible
    Dado que estoy autenticado como conductor
    Y existe un estacionamiento con id "1"
    Y el espacio "A-05" está ocupado
    Cuando envío una petición POST a "/api/v1/reservations" con datos válidos para el espacio "A-05"
    Entonces debo recibir un código de estado 409
    Y la respuesta debe contener el mensaje "Parking spot not available"

  Escenario: Actualizar el estado de una reserva a confirmada
    Dado que estoy autenticado como conductor
    Y existe una reserva con id "10" en estado "PENDING"
    Cuando envío una petición PATCH a "/api/v1/reservations/10" con los siguientes datos:
      | campo  | valor     |
      | status | CONFIRMED |
    Entonces debo recibir un código de estado 200
    Y el estado de la reserva debe ser "CONFIRMED"
    Y la respuesta debe contener la fecha de actualización

  Escenario: Cancelar una reserva activa
    Dado que estoy autenticado como conductor con id "1"
    Y tengo una reserva con id "10" en estado "PENDING"
    Cuando envío una petición PATCH a "/api/v1/reservations/10" con los siguientes datos:
      | campo  | valor     |
      | status | CANCELLED |
    Entonces debo recibir un código de estado 200
    Y el estado de la reserva debe ser "CANCELLED"
    Y el espacio asociado debe quedar disponible nuevamente

  Escenario: Obtener todas las reservas de un estacionamiento
    Dado que estoy autenticado como propietario
    Y mi estacionamiento con id "1" tiene 5 reservas registradas
    Cuando envío una petición GET a "/api/v1/reservations/parking/1"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de 5 reservas
    Y cada reserva debe tener "id", "driverId", "parkingSpotId", "status", "startTime"

  Escenario: Obtener reservas de conductor filtradas por estado
    Dado que estoy autenticado como conductor con id "1"
    Y tengo 3 reservas en estado "CONFIRMED"
    Y tengo 2 reservas en estado "COMPLETED"
    Cuando envío una petición GET a "/api/v1/reservations/driver/1/status/CONFIRMED"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de 3 reservas
    Y todas las reservas deben tener estado "CONFIRMED"

  Escenario: Completar una reserva después del uso
    Dado que estoy autenticado como conductor
    Y existe una reserva con id "10" en estado "CONFIRMED"
    Y la hora actual es posterior al "endTime" de la reserva
    Cuando envío una petición PATCH a "/api/v1/reservations/10" con los siguientes datos:
      | campo  | valor     |
      | status | COMPLETED |
    Entonces debo recibir un código de estado 200
    Y el estado de la reserva debe ser "COMPLETED"
    Y debe generarse un registro de pago asociado

  Escenario: Error al actualizar reserva que no me pertenece
    Dado que estoy autenticado como conductor con id "1"
    Y existe una reserva con id "15" que pertenece a otro conductor
    Cuando envío una petición PATCH a "/api/v1/reservations/15" con datos válidos
    Entonces debo recibir un código de estado 403
    Y la respuesta debe contener el mensaje "Forbidden"
