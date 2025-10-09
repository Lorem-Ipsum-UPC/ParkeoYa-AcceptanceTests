# ParkeoYa Testing Suite

Este repositorio contiene toda la suite de pruebas para el proyecto ParkeoYa, incluyendo Unit Tests, Integration Tests y Acceptance Tests automatizados para Web Services relacionados con los User Stories especificados en cada Sprint.

## Estructura del Repositorio

```
parkeoya-testing/
├── README.md
├── unit-tests/                 # Pruebas unitarias para componentes individuales
│   ├── backend/
│   │   ├── auth/              # Tests para componentes de autenticación
│   │   ├── parking/           # Tests para gestión de estacionamientos
│   │   └── reservation/       # Tests para sistema de reservas
│   └── frontend/
│       ├── components/        # Tests para componentes de UI
│       └── services/          # Tests para servicios del frontend
├── integration-tests/          # Pruebas de integración entre módulos
│   ├── api/                   # Tests de integración de API
│   └── frontend-backend/      # Tests de integración frontend-backend
├── acceptance-tests/           # Pruebas BDD con archivos .feature y steps
│   ├── features/              # Archivos .feature en Gherkin
│   └── steps/                 # Definiciones de pasos en JavaScript/Java
├── test-data/                  # Datos de prueba y fixtures
├── reports/                    # Reportes de cobertura y resultados
└── config/                     # Configuraciones de testing (Jest, etc.)
```

## Tecnologías Utilizadas

- **Jest**: Framework de testing para JavaScript/TypeScript
- **Cucumber**: Para pruebas BDD con Gherkin
- **Selenium WebDriver**: Para pruebas de integración frontend
- **JUnit**: Para pruebas unitarias del backend en Java
- **Mockito**: Para mocking en pruebas Java
- **React Testing Library**: Para pruebas de componentes React

## Configuración del Entorno

### Prerrequisitos
- Node.js 18+
- Java 17+
- Maven 3.8+

### Instalación
```bash
npm install
mvn clean install
```

### Ejecución de Pruebas

#### Unit Tests
```bash
# Frontend
npm run test:unit:frontend

# Backend
mvn test
```

#### Integration Tests
```bash
npm run test:integration
```

#### Acceptance Tests (BDD)
```bash
npm run test:acceptance
```

#### Todas las Pruebas
```bash
npm run test:all
```

### Reportes de Cobertura
```bash
npm run coverage
```

## Convenciones de Naming

- **Unit Tests**: `ComponentName.test.js` o `ServiceName.test.java`
- **Integration Tests**: `ComponentIntegration.test.js`
- **Feature Files**: `feature-name.feature`
- **Step Definitions**: `feature-name.steps.js`



