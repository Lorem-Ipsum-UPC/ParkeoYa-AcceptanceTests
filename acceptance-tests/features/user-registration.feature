# language: es

Característica: Registro diferenciado por tipo de usuario
  Como usuario potencial
  Quiero registrarme seleccionando mi tipo de usuario
  Para acceder a funcionalidades específicas según mi rol

  Antecedentes:
    Dado que estoy en la página principal de ParkeoYa
    Y la página de registro está disponible

  Escenario: Registro exitoso como conductor
    Dado que estoy en la página de registro
    Cuando selecciono "Conductor" como tipo de usuario
    Y completo el campo "Nombre" con "Juan Pérez"
    Y completo el campo "Email" con "juan.perez@email.com"
    Y completo el campo "Contraseña" con "MiContraseña123!"
    Y completo el campo "Confirmar contraseña" con "MiContraseña123!"
    Y completo el campo "Teléfono" con "987654321"
    Y hago clic en "Registrarse"
    Entonces debo ser redirigido al dashboard de conductor
    Y debo ver el mensaje "¡Bienvenido a ParkeoYa! Tu cuenta de conductor ha sido creada exitosamente"
    Y debo ver opciones para buscar estacionamientos
    Y debo ver mi perfil de conductor en la barra de navegación

  Escenario: Registro exitoso como propietario
    Dado que estoy en la página de registro
    Cuando selecciono "Propietario" como tipo de usuario
    Y completo el campo "Nombre" con "María García"
    Y completo el campo "Email" con "maria.garcia@email.com"
    Y completo el campo "Contraseña" con "MiContraseña456!"
    Y completo el campo "Confirmar contraseña" con "MiContraseña456!"
    Y completo el campo "Teléfono" con "987654322"
    Y completo el campo "Nombre de la empresa" con "Estacionamientos García SAC"
    Y hago clic en "Registrarse"
    Entonces debo ser redirigido al panel de propietario
    Y debo ver el mensaje "¡Bienvenido a ParkeoYa! Tu cuenta de propietario ha sido creada exitosamente"
    Y debo ver la opción "Registrar nuevo estacionamiento"
    Y debo ver mi perfil de propietario en la barra de navegación

  Escenario: Error al registrarse con email duplicado
    Dado que existe un usuario con email "usuario.existente@email.com"
    Y estoy en la página de registro
    Cuando selecciono "Conductor" como tipo de usuario
    Y completo el formulario con datos válidos
    Y uso el email "usuario.existente@email.com"
    Y hago clic en "Registrarse"
    Entonces debo ver el mensaje de error "Este email ya está registrado"
    Y debo permanecer en la página de registro
    Y el formulario debe mantener los datos ingresados excepto la contraseña

  Escenario: Error al registrarse con contraseñas que no coinciden
    Dado que estoy en la página de registro
    Cuando selecciono "Conductor" como tipo de usuario
    Y completo el campo "Contraseña" con "MiContraseña123!"
    Y completo el campo "Confirmar contraseña" con "OtraContraseña456!"
    Y completo los demás campos con datos válidos
    Y hago clic en "Registrarse"
    Entonces debo ver el mensaje de error "Las contraseñas no coinciden"
    Y debo permanecer en la página de registro
    Y los campos de contraseña deben estar vacíos