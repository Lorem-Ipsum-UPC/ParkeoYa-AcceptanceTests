# language: es

Característica: Registro de estacionamiento
  Como propietario de estacionamiento
  Quiero registrar mi local en la plataforma
  Para comenzar a recibir reservas y gestionar mis espacios

  Antecedentes:
    Dado que soy un propietario autenticado en el sistema
    Y tengo permisos para registrar estacionamientos

  Escenario: Registro exitoso de estacionamiento básico
    Dado que estoy en el panel de propietario
    Cuando hago clic en "Registrar nuevo estacionamiento"
    Y completo el campo "Nombre del estacionamiento" con "Estacionamiento Central"
    Y completo el campo "Dirección" con "Av. Principal 123, Lima"
    Y selecciono "Lima" como provincia
    Y selecciono "Lima" como distrito
    Y completo el campo "Capacidad total" con "50"
    Y selecciono "24 horas" como horario de funcionamiento
    Y establezco la tarifa por hora en "5.00"
    Y agrego la descripción "Estacionamiento seguro en el centro de la ciudad"
    Y hago clic en "Registrar estacionamiento"
    Entonces el estacionamiento debe ser creado exitosamente
    Y debo ver el mensaje "Estacionamiento registrado exitosamente"
    Y debo ser redirigido a la página de gestión del estacionamiento
    Y debo ver "Estacionamiento Central" en mi lista de estacionamientos

  Escenario: Registro de estacionamiento con horarios personalizados
    Dado que estoy en el formulario de registro de estacionamiento
    Cuando completo la información básica del estacionamiento
    Y selecciono "Horarios personalizados"
    Y configuro horario de Lunes a Viernes de "06:00" a "22:00"
    Y configuro horario de Sábado de "08:00" a "20:00"
    Y configuro horario de Domingo de "10:00" a "18:00"
    Y establezco tarifas diferenciadas por horario
    Y hago clic en "Registrar estacionamiento"
    Entonces el estacionamiento debe ser creado con los horarios personalizados
    Y debo poder ver los horarios configurados en el panel de gestión

  Escenario: Registro de estacionamiento con servicios adicionales
    Dado que estoy en el formulario de registro de estacionamiento
    Cuando completo la información básica del estacionamiento
    Y marco el servicio "Lavado de autos"
    Y marco el servicio "Carga eléctrica"
    Y marco el servicio "Vigilancia 24/7"
    Y configuro precios adicionales para cada servicio
    Y agrego fotos del estacionamiento
    Y hago clic en "Registrar estacionamiento"
    Entonces el estacionamiento debe ser creado con todos los servicios
    Y los usuarios deben poder ver estos servicios al buscar estacionamientos

  Escenario: Error al registrar con capacidad inválida
    Dado que estoy en el formulario de registro de estacionamiento
    Cuando completo todos los campos obligatorios
    Y ingreso "0" en el campo capacidad
    Y hago clic en "Registrar estacionamiento"
    Entonces debo ver el mensaje de error "La capacidad debe ser mayor a 0"
    Y debo permanecer en el formulario de registro
    Y los datos ingresados deben mantenerse

  Escenario: Error al registrar con dirección duplicada
    Dado que ya existe un estacionamiento en "Av. Principal 123, Lima"
    Y estoy en el formulario de registro de estacionamiento
    Cuando intento registrar otro estacionamiento en la misma dirección
    Y completo todos los demás campos correctamente
    Y hago clic en "Registrar estacionamiento"
    Entonces debo ver el mensaje de error "Ya existe un estacionamiento registrado en esta dirección"
    Y debo poder modificar la dirección para continuar con el registro

  Escenario: Configuración de espacios después del registro
    Dado que he registrado exitosamente un estacionamiento
    Cuando accedo a la configuración de espacios
    Y selecciono "Configurar espacios automáticamente"
    Y confirmo la distribución sugerida de 50 espacios
    Y asigno 40 espacios para autos regulares
    Y asigno 8 espacios para autos compactos  
    Y asigno 2 espacios para discapacitados
    Y hago clic en "Guardar configuración"
    Entonces los espacios deben ser creados según la configuración
    Y debo poder ver el estado de ocupación de cada espacio
    Y el estacionamiento debe estar disponible para reservas