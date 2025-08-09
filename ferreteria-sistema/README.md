# Sistema de Gestión para Ferretería

Sistema completo de gestión empresarial desarrollado con Spring Boot, diseñado específicamente para ferreterías. Incluye gestión de inventario, ventas, clientes, proveedores, empleados y reportes.

## 🚀 Características Principales

### Funcionalidades del Sistema
- **Dashboard Interactivo**: Estadísticas en tiempo real y acciones rápidas
- **Gestión de Inventario**: Control completo de productos y stock
- **Sistema de Ventas**: Facturación y gestión de clientes
- **Gestión de Compras**: Pedidos a proveedores y recepción de mercancía
- **Administración de Personal**: Empleados, horarios y roles
- **Reportes y Estadísticas**: Análisis de ventas, inventario y rendimiento
- **Sistema de Usuarios**: Autenticación y autorización por roles

### Tecnologías Utilizadas
- **Backend**: Spring Boot 3.2.0, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5, jQuery, Chart.js
- **Base de Datos**: Oracle Database (compatible con otras BD relacionales)
- **Seguridad**: Autenticación basada en roles, encriptación de contraseñas
- **Documentación API**: Swagger/OpenAPI 3

## 📋 Prerrequisitos

### Software Requerido
- **Java 17 o superior**
- **Maven 3.8+**
- **Oracle Database 12c+ o cualquier base de datos compatible con JPA**
- **Git** (para clonar el repositorio)

### Configuración de Base de Datos

#### 1. Configurar Oracle Database
```sql
-- Crear usuario para la aplicación
CREATE USER ferreteria_user IDENTIFIED BY ferreteria_pass;
GRANT CONNECT, RESOURCE, DBA TO ferreteria_user;
GRANT UNLIMITED TABLESPACE TO ferreteria_user;
```

#### 2. Ejecutar Script de Base de Datos
```bash
# Ejecutar el script SQL completo
sqlplus ferreteria_user/ferreteria_pass@localhost:1521/XE @ProyectoLengdeDatos_Completo.sql
```

## 🛠️ Instalación y Configuración

### 1. Clonar el Proyecto
```bash
git clone <url-del-repositorio>
cd ferreteria-sistema
```

### 2. Configurar Base de Datos
Editar `src/main/resources/application.properties`:
```properties
# Configuración de base de datos
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=ferreteria_user
spring.datasource.password=ferreteria_pass
```

### 3. Compilar y Ejecutar
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
```

### 4. Acceder al Sistema
- **URL Principal**: http://localhost:8080/ferreteria
- **Usuario por defecto**: admin
- **Contraseña por defecto**: admin123

## 📖 Estructura del Proyecto

### Arquitectura del Sistema
```
ferreteria-sistema/
├── src/main/
│   ├── java/com/ferreteria/sistema/
│   │   ├── config/          # Configuraciones (Seguridad, BD)
│   │   ├── controller/      # Controladores REST y Web
│   │   │   ├── rest/        # APIs REST
│   │   │   └── web/         # Controladores web
│   │   ├── entity/          # Entidades JPA
│   │   ├── repository/      # Repositorios de datos
│   │   ├── service/         # Lógica de negocio
│   │   └── security/        # Servicios de seguridad
│   └── resources/
│       ├── static/          # Recursos estáticos (CSS, JS)
│       └── templates/       # Plantillas Thymeleaf
└── ProyectoLengdeDatos_Completo.sql  # Script de BD completo
```

### Entidades Principales
- **Usuarios y Roles**: Sistema de autenticación y autorización
- **Clientes**: Gestión de información de clientes
- **Productos**: Catálogo de productos con categorías y precios
- **Stock**: Control de inventario y ubicaciones
- **Facturas**: Sistema de facturación y ventas
- **Pedidos**: Órdenes de compra a proveedores
- **Empleados**: Gestión de personal
- **Horarios**: Control de horarios laborales

## 🔐 Sistema de Roles y Permisos

### Roles Disponibles
- **ADMINISTRADOR**: Acceso completo al sistema
- **GERENTE**: Gestión y reportes
- **VENDEDOR**: Ventas y atención al cliente
- **BODEGUERO**: Gestión de inventario y pedidos

### Matriz de Permisos
| Funcionalidad | Admin | Gerente | Vendedor | Bodeguero |
|---------------|-------|---------|----------|-----------|
| Dashboard | ✅ | ✅ | ✅ | ✅ |
| Clientes | ✅ | ✅ | ✅ | ❌ |
| Productos | ✅ | ✅ | ❌ | ✅ |
| Stock | ✅ | ✅ | ❌ | ✅ |
| Facturas | ✅ | ✅ | ✅ | ❌ |
| Pedidos | ✅ | ✅ | ❌ | ✅ |
| Empleados | ✅ | ✅ | ❌ | ❌ |
| Usuarios/Roles | ✅ | ❌ | ❌ | ❌ |
| Reportes | ✅ | ✅ | ❌ | ❌ |

## 🔧 APIs REST Disponibles

### Endpoints Principales
- `GET /api/clientes` - Listar clientes
- `POST /api/clientes` - Crear cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `DELETE /api/clientes/{id}` - Eliminar cliente
- `GET /api/productos` - Listar productos
- `GET /api/facturas` - Listar facturas
- `GET /api/stock/bajo` - Productos con stock bajo

### Documentación API
- **Swagger UI**: http://localhost:8080/ferreteria/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/ferreteria/api-docs

## 📊 Base de Datos - Características Avanzadas

### Procedimientos Almacenados Implementados
- **CRUD Completo**: Procedimientos para todas las entidades
- **Validaciones Avanzadas**: Usando expresiones regulares
- **Gestión de Cursores**: 4+ procedimientos con cursores
- **Triggers Automáticos**: 12 triggers para auditoría y validación

### Expresiones Regulares Implementadas
- Validación de emails
- Validación de teléfonos
- Búsqueda de patrones en códigos de producto
- Validación de RUC/cédulas

### Triggers Configurados
- Actualización automática de fechas de modificación
- Validación de formatos de datos
- Generación automática de códigos
- Actualización de totales en facturas y pedidos
- Control automático de stock

## 🎨 Frontend y Experiencia de Usuario

### Características de la Interfaz
- **Diseño Responsivo**: Compatible con dispositivos móviles
- **Tema Moderno**: Gradientes y animaciones suaves
- **Navegación Intuitiva**: Sidebar colapsible y breadcrumbs
- **Feedback Visual**: Alertas, loaders y confirmaciones
- **Búsqueda en Tiempo Real**: Filtros dinámicos en tablas

### Componentes Interactivos
- Modales para formularios
- Tablas con paginación y ordenamiento
- Gráficos de estadísticas (Chart.js)
- Auto-guardado de formularios
- Validación en tiempo real

## 🚀 Funcionalidades Avanzadas

### Seguridad
- Autenticación con Spring Security
- Encriptación de contraseñas con BCrypt
- Protección CSRF
- Control de sesiones
- Timeout por inactividad

### Optimización
- Cache de segundo nivel con Hibernate
- Pool de conexiones HikariCP
- Índices optimizados en base de datos
- Compresión de recursos estáticos

### Monitoreo
- Spring Boot Actuator
- Logs estructurados
- Métricas de rendimiento
- Health checks automáticos

## 📚 Guía de Desarrollo

### Agregar Nueva Entidad
1. Crear entidad JPA en `entity/`
2. Crear repositorio en `repository/`
3. Implementar servicio en `service/`
4. Crear controlador REST en `controller/rest/`
5. Crear controlador web en `controller/web/`
6. Diseñar plantilla HTML en `templates/`

### Agregar Nuevo Rol
1. Insertar rol en tabla `Roles`
2. Actualizar enum en `SecurityConfig`
3. Agregar anotaciones `@PreAuthorize` en controladores
4. Actualizar plantillas con `sec:authorize`

### Personalizar Tema
- Modificar variables CSS en `:root`
- Actualizar componentes en `static/css/styles.css`
- Agregar animaciones personalizadas

## 🧪 Testing

### Ejecutar Pruebas
```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas de integración
mvn integration-test

# Generar reporte de cobertura
mvn jacoco:report
```

### Tipos de Pruebas Incluidas
- Pruebas unitarias de servicios
- Pruebas de integración de repositorios
- Pruebas de controladores REST
- Pruebas de seguridad

## 📦 Deployment

### Construcción para Producción
```bash
# Crear JAR ejecutable
mvn clean package -Pprod

# El JAR se genera en target/sistema-ferreteria-1.0.0.jar
```

### Configuración de Producción
Crear `application-prod.properties`:
```properties
# Base de datos de producción
spring.datasource.url=jdbc:oracle:thin:@prod-server:1521/PROD
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Configuración de seguridad
spring.security.require-ssl=true
server.port=8443

# Configuración de logs
logging.level.com.ferreteria.sistema=INFO
```

### Ejecutar en Producción
```bash
java -jar target/sistema-ferreteria-1.0.0.jar --spring.profiles.active=prod
```

## 🆘 Solución de Problemas

### Problemas Comunes

#### Error de Conexión a BD
```
Error: ORA-01017: invalid username/password
Solución: Verificar credenciales en application.properties
```

#### Puerto en Uso
```
Error: Port 8080 is already in use
Solución: Cambiar puerto con server.port=8081
```

#### Memoria Insuficiente
```bash
# Aumentar memoria JVM
java -Xmx2g -jar sistema-ferreteria-1.0.0.jar
```

### Logs Útiles
```bash
# Ver logs de la aplicación
tail -f logs/ferreteria-sistema.log

# Ver logs de Spring Security
logging.level.org.springframework.security=DEBUG
```

## 🤝 Contribución

### Estándares de Código
- Seguir convenciones de nomenclatura Java
- Documentar métodos públicos con JavaDoc
- Escribir pruebas para nueva funcionalidad
- Mantener cobertura de pruebas > 80%

### Proceso de Contribución
1. Fork del repositorio
2. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -m 'Agregar nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo `LICENSE` para más detalles.

## 👥 Equipo de Desarrollo

- **Arquitectura**: Spring Boot + Oracle Database
- **Frontend**: Bootstrap + Thymeleaf
- **Seguridad**: Spring Security
- **Base de Datos**: Procedimientos almacenados y triggers avanzados

## 📞 Soporte

Para soporte técnico o consultas:
- 📧 Email: soporte@ferreteria-sistema.com
- 📱 WhatsApp: +593 99 123 4567
- 🌐 Web: www.ferreteria-sistema.com

---

**Sistema de Ferretería v1.0.0** - Desarrollado con ❤️ para optimizar la gestión empresarial

