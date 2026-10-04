# 🤖 Directrices para Agentes de IA - Backend (Turnos-Backend)

Este archivo define las reglas obligatorias de comportamiento, seguridad, arquitectura y workflow para cualquier agente de IA que trabaje en este repositorio.

---

## 🌐 1. Idioma e Interacción
- **Español Obligatorio**: Toda la comunicación con el usuario (respuestas, explicaciones, comentarios de código y commits) debe realizarse strictly en español.

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

## 🛑 4. Protocolo de Modificación de Reglas

1. **Aprobación Estricta del Usuario**: Si durante el desarrollo surge un nuevo requerimiento que modifique o contradiga alguna de estas reglas (por ejemplo, permitir que un paciente gestione turnos para su grupo familiar), el agente de IA **debe detenerse, proponer la actualización de este `AGENTS.md` y solicitar la aprobación explícita del usuario antes de modificar el código**.
2. **Nuevas Reglas de Negocio**: Si el agente identifica la necesidad de añadir una nueva regla no contemplada, deberá solicitar aprobación estricta del usuario antes de modificar este documento.
