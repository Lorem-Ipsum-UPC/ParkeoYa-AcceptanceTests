# language: es

Característica: Gestión de perfiles de usuario
  Como usuario del sistema
  Quiero gestionar mi perfil personal
  Para mantener mi información actualizada

  Antecedentes:
    Dado que el servicio de API está disponible en "https://parkeoya-backend-latest-1.onrender.com"
    Y estoy autenticado en el sistema

  Escenario: Actualizar perfil de propietario de estacionamiento exitosamente
    Dado que estoy autenticado como propietario con id "1"
    Y mi perfil actual tiene los siguientes datos:
      | campo       | valor                  |
      | fullName    | Juan García            |
      | phone       | 987654321              |
      | companyName | Estacionamientos SAC   |
    Cuando envío una petición PATCH a "/api/v1/profiles/parking-owner/1" con los siguientes datos:
      | campo       | valor                        |
      | fullName    | Juan García Pérez            |
      | phone       | 987654322                    |
      | city        | Lima                         |
      | country     | Peru                         |
      | companyName | Estacionamientos Premium SAC |
      | ruc         | 20123456789                  |
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener los datos actualizados
    Y el campo "fullName" debe ser "Juan García Pérez"
    Y el campo "companyName" debe ser "Estacionamientos Premium SAC"

  Escenario: Actualizar perfil de conductor exitosamente
    Dado que estoy autenticado como conductor con id "2"
    Y mi perfil actual tiene información básica
    Cuando envío una petición PATCH a "/api/v1/profiles/driver/2" con los siguientes datos:
      | campo    | valor              |
      | fullName | María López Rivera |
      | phone    | 912345678          |
      | city     | Arequipa           |
      | country  | Peru               |
      | dni      | 12345678           |
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener los datos actualizados del conductor
    Y el campo "fullName" debe ser "María López Rivera"
    Y el campo "city" debe ser "Arequipa"

  Escenario: Obtener perfil de propietario por ID de usuario
    Dado que existe un propietario con userId "1"
    Y el propietario tiene un perfil completo
    Cuando envío una petición GET a "/api/v1/profiles/parking-owner/1"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener todos los campos del perfil
    Y debo ver "fullName", "email", "phone", "city", "country", "companyName", "ruc"

  Escenario: Obtener perfil de conductor por ID de usuario
    Dado que existe un conductor con userId "2"
    Y el conductor tiene un perfil completo
    Cuando envío una petición GET a "/api/v1/profiles/driver/2"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener todos los campos del perfil de conductor
    Y debo ver "fullName", "email", "phone", "city", "country", "dni"

  Escenario: Error al actualizar perfil con datos inválidos
    Dado que estoy autenticado como propietario con id "1"
    Cuando envío una petición PATCH a "/api/v1/profiles/parking-owner/1" con los siguientes datos:
      | campo | valor |
      | phone | abc   |
      | ruc   | 123   |
    Entonces debo recibir un código de estado 400
    Y la respuesta debe contener mensajes de validación
    Y debo ver el error "Invalid phone format"
    Y debo ver el error "RUC must be 11 digits"

  Escenario: Error al actualizar perfil que no me pertenece
    Dado que estoy autenticado como propietario con id "1"
    Y existe otro propietario con id "5"
    Cuando envío una petición PATCH a "/api/v1/profiles/parking-owner/5" con datos válidos
    Entonces debo recibir un código de estado 403
    Y la respuesta debe contener el mensaje "Forbidden: You can only update your own profile"

  Escenario: Actualizar solo algunos campos del perfil
    Dado que estoy autenticado como conductor con id "2"
    Cuando envío una petición PATCH a "/api/v1/profiles/driver/2" con los siguientes datos:
      | campo | valor     |
      | phone | 999888777 |
    Entonces debo recibir un código de estado 200
    Y solo el campo "phone" debe estar actualizado
    Y los demás campos deben mantener sus valores originales

  Escenario: Obtener perfil inexistente
    Dado que no existe un usuario con id "9999"
    Cuando envío una petición GET a "/api/v1/profiles/driver/9999"
    Entonces debo recibir un código de estado 404
    Y la respuesta debe contener el mensaje "Profile not found"
