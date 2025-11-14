# language: es

Característica: Gestión de estacionamientos
  Como propietario de estacionamiento
  Quiero gestionar mis estacionamientos a través de la API
  Para poder ofrecer mis espacios a los conductores

  Antecedentes:
    Dado que el servicio de API está disponible en "https://parkeoya-backend-latest-1.onrender.com"
    Y tengo credenciales válidas de propietario

  Escenario: Obtener todos los estacionamientos disponibles
    Dado que existen estacionamientos registrados en el sistema
    Cuando envío una petición GET a "/api/v1/parkings"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de estacionamientos
    Y cada estacionamiento debe tener "id", "name", "address", "availableSpots"

  Escenario: Crear un nuevo estacionamiento exitosamente
    Dado que estoy autenticado como propietario
    Cuando envío una petición POST a "/api/v1/parkings" con los siguientes datos:
      | campo          | valor                          |
      | name           | Estacionamiento Centro         |
      | description    | Estacionamiento en zona centro |
      | address        | Av. Principal 123              |
      | city           | Lima                           |
      | country        | Peru                           |
      | lat            | -12.046374                     |
      | lng            | -77.042793                     |
      | ratePerHour    | 5.00                           |
      | dailyRate      | 40.00                          |
      | totalSpots     | 50                             |
      | totalRows      | 5                              |
      | totalColumns   | 10                             |
      | openingTime    | 06:00                          |
      | closingTime    | 22:00                          |
    Entonces debo recibir un código de estado 201
    Y la respuesta debe contener el "id" del estacionamiento creado
    Y el estacionamiento debe tener spots generados automáticamente

  Escenario: Error al crear estacionamiento sin autenticación
    Dado que no estoy autenticado
    Cuando envío una petición POST a "/api/v1/parkings" con datos válidos
    Entonces debo recibir un código de estado 401
    Y la respuesta debe contener el mensaje "Unauthorized"

  Escenario: Obtener los espacios de un estacionamiento específico
    Dado que existe un estacionamiento con id "1"
    Y el estacionamiento tiene espacios registrados
    Cuando envío una petición GET a "/api/v1/parkings/1/spots"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de espacios
    Y cada espacio debe tener "id", "label", "row", "column", "status"

  Escenario: Agregar un espacio adicional a un estacionamiento
    Dado que estoy autenticado como propietario
    Y existe un estacionamiento con id "1" que me pertenece
    Cuando envío una petición POST a "/api/v1/parkings/1/spots" con los siguientes datos:
      | campo  | valor |
      | row    | 1     |
      | column | 11    |
      | label  | A-11  |
    Entonces debo recibir un código de estado 201
    Y la respuesta debe contener el "id" del espacio creado
    Y el espacio debe estar asociado al estacionamiento

  Escenario: Actualizar información de un estacionamiento
    Dado que estoy autenticado como propietario
    Y existe un estacionamiento con id "1" que me pertenece
    Cuando envío una petición PATCH a "/api/v1/parkings/1" con los siguientes datos:
      | campo       | valor                   |
      | ratePerHour | 6.50                    |
      | description | Estacionamiento Premium |
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener los datos actualizados
    Y el campo "ratePerHour" debe ser "6.50"

  Escenario: Obtener estacionamientos por propietario
    Dado que estoy autenticado como propietario con id "1"
    Y tengo 3 estacionamientos registrados
    Cuando envío una petición GET a "/api/v1/parkings/owner/1"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de 3 estacionamientos
    Y todos los estacionamientos deben pertenecer al propietario con id "1"

  Escenario: Error al actualizar estacionamiento que no me pertenece
    Dado que estoy autenticado como propietario con id "1"
    Y existe un estacionamiento con id "5" que pertenece a otro propietario
    Cuando envío una petición PATCH a "/api/v1/parkings/5" con datos válidos
    Entonces debo recibir un código de estado 403
    Y la respuesta debe contener el mensaje "Forbidden"
