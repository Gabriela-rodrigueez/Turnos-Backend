# 🤖 Directrices para Agentes de IA - Backend (Turnos-Backend)

Este archivo define las reglas obligatorias de comportamiento, seguridad, arquitectura y workflow para cualquier agente de IA que trabaje en este repositorio.

---

## 🌐 1. Idioma e Interacción
- **Español Obligatorio**: Toda la comunicación con el usuario (respuestas, explicaciones, comentarios de código y commits) debe realizarse estrictamente en español.

---

## 🔒 2. Seguridad y Control de Acceso (RBAC y ABAC)

### 2.1 Anotaciones de Roles en Endpoints (RBAC)
- Todo endpoint nuevo debe contar **obligatoriamente** con su anotación de roles permitidos (`@EsPaciente`, `@EsMedico`, `@EsAdministrador`, `@EsPacienteOAdministrador`, `@EsPacienteOMedicoOAdministrador`).
- Se prohíbe el uso de expresiones SpEL crudas en los controladores.
- Si un endpoint requiere solo estar autenticado o ser público, debe configurarse explícitamente en la cadena de seguridad centralizada (`SecurityConfig.java`).

### 2.2 Uso de `AuthenticatedUser` (ABAC)
- Todo servicio o controlador que requiera validar la identidad del usuario en sesión debe utilizar `SecurityUtils.getCurrentUser()`, el cual devuelve un `AuthenticatedUser` con todos los claims del JWT (ID de usuario, email, rol, `pacienteId`, `profesionalId`).

### 2.3 Restricción de Propiedad por Rol (Ownership Checks)
- **Pacientes**: Toda acción realizada por un usuario con rol `PACIENTE` debe estar estrictamente limitada por su propio `pacienteId`. No puede consultar, solicitar, editar ni cancelar turnos pertenecientes a otro paciente.
- **Médicos**: Aplica la misma regla para el rol `MEDICO`; solo puede operar sobre los turnos asociados a su propio `profesionalId`.
- **Administradores**: Tienen privilegios globales para operar con cualquier `pacienteId` o `profesionalId`.

---

## 🛠️ 3. Reglas Técnicas y de Calidad

### 3.1 Verificación Estricta mediante Pruebas Automatizadas
- Ninguna tarea de código o refactorización se considerará finalizada sin haber ejecutado `mvn test` en la terminal y verificado que el **100% de las pruebas pasen (`BUILD SUCCESS`)**.

### 3.2 Cero Hardcoding de Configuración
- Todas las variables y parámetros de configuración (claves secretas de JWT, tiempos de expiración, URLs de bases de datos, perfiles) deben externalizarse en `application.properties` / `application.yml`. No se permiten constantes hardcodeadas en código Java.

### 3.3 Manejo Centralizado de Excepciones
- Todas las excepciones de negocio (ej. `RecursoNoEncontradoException`, `ConflictoHorarioException`, `AccessDeniedException`) deben ser gestionadas mediante la arquitectura `@ControllerAdvice` mapeándolas a un `ErrorResponseDTO` uniforme.

### 3.4 Documentación OpenAPI / Swagger
- Todo nuevo endpoint REST debe documentarse con `@Operation`, `@Parameter` y `@ApiResponses` indicando los posibles códigos HTTP de retorno (200, 201, 400, 401, 403, 404, 409).
- **Sincronización Obligatoria**: Toda modificación en los datos de entrada (Request DTO, Query Params, Path Variables) o salida (Response DTO, códigos HTTP) de un controlador debe incluir **obligatoriamente** la actualización correspondiente en la documentación de Swagger/OpenAPI.

---

## 🗄️ 4. Gestión de Base de Datos y Migraciones (Flyway & Hibernate)

### 4.1 Hibernate en Modo Solo Validación (`ddl-auto=validate`)
- En el entorno principal (`src/main/resources/application.properties`), la propiedad `spring.jpa.hibernate.ddl-auto` debe permanecer **estrictamente en `validate`**.
- Queda **terminantemente prohibido** cambiar esta propiedad a `update`, `create` o `create-drop` en la configuración principal. Hibernate solo valida que las entidades Java coincidan con las tablas creadas por Flyway en MySQL.

### 4.2 Inmutabilidad Estricta de Migraciones Flyway
- Los scripts de migración existentes en `src/main/resources/db/migration/` (`V1__...`, `V2__...`, etc.) son **ESTRICTAMENTE INMUTABLES**.
- **NUNCA** se debe modificar, renombrar ni eliminar una migración ya existente; cualquier alteración provocará un fallo crítico de checksum en Flyway.
- Todo cambio en el esquema (crear tabla, agregar/modificar columna, agregar índice o clave foránea) o en datos semilla debe realizarse creando un **nuevo archivo de migración con el siguiente número secuencial**.

### 4.3 Formato y Convención de Nombres de Migraciones
- Todo nuevo archivo debe respetar rigurosamente la nomenclatura de Flyway con **DOBLE GUION BAJO**:
  `V<NúmeroSecuencial>__<descripcion_en_snake_case>.sql` (Ejemplo: `V3__agregar_columna_telefono_en_sedes.sql`).
- La sintaxis SQL de las migraciones debe ser nativa y compatible con **MySQL 8.x** (`ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`).

### 4.4 Sincronización Obligatoria: Entidad ↔ Migración Flyway ↔ `data.sql` de Tests
- Si un requerimiento exige modificar o crear una entidad JPA (`@Entity`):
  1. **Migración Flyway:** Debe crearse el script `V<N>__...sql` correspondiente con la sentencia DDL/DML.
  2. **Actualización de Tests:** Debe actualizarse de forma sincronizada el archivo de prueba `data.sql` (en `src/test/resources/data.sql`) y los mocks que correspondan.
  3. **Verificación:** Ejecutar `mvn test` y confirmar que Hibernate valida sin errores y el 100% de las pruebas pasa (`BUILD SUCCESS`).

### 4.5 Aislamiento del Entorno de Pruebas
- Las pruebas automatizadas (`src/test/resources/application.properties`) deben mantenerse autónomas y aisladas en memoria (H2 en modo MySQL dialect) para garantizar que `mvn test` se ejecute velozmente sin requerir servicios externos activos.
- El archivo `data.sql` reside **únicamente en `src/test/resources/data.sql`** y no debe colocarse en `src/main/resources/` para evitar colisiones con Flyway en el arranque en MySQL.

---

## 🛑 5. Protocolo de Modificación de Reglas

1. **Aprobación Estricta del Usuario**: Si durante el desarrollo surge un nuevo requerimiento que modifique o contradiga alguna de estas reglas (por ejemplo, permitir que un paciente gestione turnos para su grupo familiar o relajar el modo de validación de Hibernate), el agente de IA **debe detenerse, proponer la actualización de este `AGENTS.md` y solicitar la aprobación explícita del usuario antes de modificar el código**.
2. **Nuevas Reglas de Negocio**: Si el agente identifica la necesidad de añadir una nueva regla no contemplada, deberá solicitar aprobación estricta del usuario antes de modificar este documento.
