# language: es

Característica: Sistema de reseñas y calificaciones
  Como conductor
  Quiero dejar reseñas sobre los estacionamientos que he utilizado
  Para ayudar a otros conductores a tomar mejores decisiones

  Antecedentes:
    Dado que el servicio de API está disponible en "https://parkeoya-backend-latest-1.onrender.com"
    Y tengo credenciales válidas de conductor

  Escenario: Crear una reseña exitosamente después de usar un estacionamiento
    Dado que estoy autenticado como conductor con id "1"
    Y he completado una reserva en el estacionamiento con id "1"
    Cuando envío una petición POST a "/api/v1/reviews" con los siguientes datos:
      | campo     | valor                                                            |
      | driverId  | 1                                                                |
      | parkingId | 1                                                                |
      | rating    | 5                                                                |
      | comment   | Excelente estacionamiento, muy limpio y seguro. Recomendado 100% |
    Entonces debo recibir un código de estado 201
    Y la respuesta debe contener el "id" de la reseña
    Y la reseña debe estar asociada al conductor y al estacionamiento

  Escenario: Error al crear reseña con calificación inválida
    Dado que estoy autenticado como conductor
    Cuando envío una petición POST a "/api/v1/reviews" con los siguientes datos:
      | campo     | valor                 |
      | driverId  | 1                     |
      | parkingId | 1                     |
      | rating    | 6                     |
      | comment   | Calificación inválida |
    Entonces debo recibir un código de estado 400
    Y la respuesta debe contener el mensaje "Rating must be between 1 and 5"

  Escenario: Obtener todas las reseñas de un estacionamiento específico
    Dado que el estacionamiento con id "1" tiene 10 reseñas
    Cuando envío una petición GET a "/api/v1/reviews/parking/1"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de 10 reseñas
    Y cada reseña debe tener "id", "driverId", "rating", "comment", "createdAt"
    Y las reseñas deben estar ordenadas por fecha de creación descendente

  Escenario: Obtener reseñas realizadas por un conductor
    Dado que estoy autenticado como conductor con id "1"
    Y he realizado 3 reseñas en diferentes estacionamientos
    Cuando envío una petición GET a "/api/v1/reviews/driver/1"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener una lista de 3 reseñas
    Y todas las reseñas deben ser del conductor con id "1"

  Escenario: Ver promedio de calificaciones de un estacionamiento
    Dado que el estacionamiento con id "1" tiene las siguientes reseñas:
      | rating |
      | 5      |
      | 4      |
      | 5      |
      | 3      |
      | 5      |
    Cuando envío una petición GET a "/api/v1/reviews/parking/1"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe incluir el promedio de calificación
    Y el promedio debe ser "4.4"

  Escenario: No permitir reseña sin haber usado el estacionamiento
    Dado que estoy autenticado como conductor con id "1"
    Y nunca he reservado en el estacionamiento con id "5"
    Cuando envío una petición POST a "/api/v1/reviews" con datos válidos para el estacionamiento "5"
    Entonces debo recibir un código de estado 403
    Y la respuesta debe contener el mensaje "You must complete a reservation before reviewing"

  Escenario: Crear reseña solo con calificación sin comentario
    Dado que estoy autenticado como conductor con id "1"
    Y he completado una reserva en el estacionamiento con id "2"
    Cuando envío una petición POST a "/api/v1/reviews" con los siguientes datos:
      | campo     | valor |
      | driverId  | 1     |
      | parkingId | 2     |
      | rating    | 4     |
      | comment   |       |
    Entonces debo recibir un código de estado 201
    Y la respuesta debe contener el "id" de la reseña
    Y el campo "comment" debe estar vacío

  Escenario: Filtrar reseñas de estacionamiento por calificación mínima
    Dado que el estacionamiento con id "1" tiene reseñas con diferentes calificaciones
    Y existen 5 reseñas con calificación de 4 o superior
    Cuando envío una petición GET a "/api/v1/reviews/parking/1?minRating=4"
    Entonces debo recibir un código de estado 200
    Y la respuesta debe contener solo reseñas con rating >= 4
    Y debo ver exactamente 5 reseñas
