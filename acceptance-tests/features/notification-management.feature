# language: es

Característica: Sistema de notificaciones push
  Como usuario del sistema
  Quiero recibir notificaciones en mi dispositivo
  Para estar informado sobre eventos importantes de mis reservas y estacionamientos

  Antecedentes:
    Dado que el servicio de API está disponible en "https://parkeoya-backend-latest-1.onrender.com"
    Y el servicio de Firebase Cloud Messaging está configurado

  Escenario: Registrar token de notificación exitosamente
    Dado que estoy autenticado como conductor con id "1"
    Y tengo un token FCM válido "dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM"
    Cuando envío una petición POST a "/api/v1/notifications/register-token" con los siguientes datos:
      | campo  | valor                                |
      | userId | 1                                    |
      | token  | dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM     |
    Entonces debo recibir un código de estado 201
    Y la respuesta debe confirmar el registro del token
    Y el token debe estar asociado al usuario con id "1"

  Escenario: Enviar notificación push a un dispositivo específico
    Dado que estoy autenticado como sistema
    Y existe un usuario con token registrado "dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM"
    Cuando envío una petición POST a "/api/v1/notifications/send" con los siguientes datos:
      | campo | valor                                                       |
      | token | dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM                            |
      | title | Confirmación de reserva                                     |
      | body  | Tu reserva en Estacionamiento Centro ha sido confirmada     |
    Entonces debo recibir un código de estado 200
    Y la respuesta debe confirmar el envío de la notificación
    Y el usuario debe recibir la notificación en su dispositivo

  Escenario: Error al enviar notificación con token inválido
    Dado que estoy autenticado como sistema
    Cuando envío una petición POST a "/api/v1/notifications/send" con los siguientes datos:
      | campo | valor                      |
      | token | token_invalido_xyz         |
      | title | Notificación de prueba     |
      | body  | Este es un mensaje de test |
    Entonces debo recibir un código de estado 400
    Y la respuesta debe contener el mensaje "Invalid FCM token"

  Escenario: Desregistrar token de notificación
    Dado que estoy autenticado como conductor
    Y tengo un token registrado "dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM"
    Cuando envío una petición DELETE a "/api/v1/notifications/unregister-token" con los siguientes datos:
      | campo | valor                            |
      | token | dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM |
    Entonces debo recibir un código de estado 200
    Y la respuesta debe confirmar la eliminación del token
    Y el token no debe estar asociado a ningún usuario

  Escenario: Enviar notificación de inicio de reserva
    Dado que existe un conductor con id "1"
    Y el conductor tiene una reserva que comienza en 15 minutos
    Y el conductor tiene un token registrado
    Cuando el sistema envía notificación de recordatorio automático
    Entonces el conductor debe recibir una notificación con título "Recordatorio de reserva"
    Y el cuerpo debe contener "Tu reserva comienza en 15 minutos"

  Escenario: Enviar notificación de finalización de reserva
    Dado que existe un conductor con id "1"
    Y el conductor tiene una reserva activa que está por finalizar
    Y el conductor tiene un token registrado
    Cuando el sistema envía notificación de finalización automática
    Entonces el conductor debe recibir una notificación con título "Reserva finalizada"
    Y el cuerpo debe contener información sobre el pago pendiente

  Escenario: Registrar múltiples tokens para el mismo usuario
    Dado que estoy autenticado como conductor con id "1"
    Y ya tengo un token registrado en mi teléfono
    Cuando registro un nuevo token desde mi tablet
    Entonces debo recibir un código de estado 201
    Y ambos dispositivos deben recibir notificaciones
    Y el usuario debe tener 2 tokens activos

  Escenario: Error al registrar token duplicado
    Dado que estoy autenticado como conductor con id "1"
    Y ya tengo registrado el token "dGhpc19pc19hX3Rlc3RfdG9rZW4xMjM"
    Cuando intento registrar el mismo token nuevamente
    Entonces debo recibir un código de estado 409
    Y la respuesta debe contener el mensaje "Token already registered"

  Escenario: Notificación de nuevo espacio disponible en estacionamiento favorito
    Dado que soy un conductor con id "1"
    Y he marcado como favorito el estacionamiento con id "5"
    Y el estacionamiento estaba lleno
    Cuando un espacio se libera en el estacionamiento
    Entonces debo recibir una notificación con título "Espacio disponible"
    Y el cuerpo debe indicar el nombre del estacionamiento
