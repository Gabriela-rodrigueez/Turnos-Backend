-- ========================================================================================
-- MIGRACIÓN FLYWAY V1: CREACIÓN DE ESQUEMA Y TABLAS INICIALES (MYSQL)
-- ========================================================================================

-- 1. TABLA: OBRAS SOCIALES
CREATE TABLE obras_sociales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    codigo VARCHAR(255),
    CONSTRAINT uk_obras_sociales_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. TABLA: ESPECIALIDADES
CREATE TABLE especialidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    CONSTRAINT uk_especialidades_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. TABLA: SEDES
CREATE TABLE sedes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(255),
    ciudad VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. TABLA: PROFESIONALES
CREATE TABLE profesionales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    dni VARCHAR(255) NOT NULL,
    matricula VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(255),
    CONSTRAINT uk_profesionales_dni UNIQUE (dni),
    CONSTRAINT uk_profesionales_matricula UNIQUE (matricula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. TABLA INTERMEDIA: PROFESIONAL - ESPECIALIDAD
CREATE TABLE profesional_especialidad (
    profesional_id BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    PRIMARY KEY (profesional_id, especialidad_id),
    CONSTRAINT fk_pe_profesional FOREIGN KEY (profesional_id) REFERENCES profesionales (id) ON DELETE CASCADE,
    CONSTRAINT fk_pe_especialidad FOREIGN KEY (especialidad_id) REFERENCES especialidades (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. TABLA: PACIENTES
CREATE TABLE pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    dni VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(255),
    fecha_nacimiento DATE,
    obra_social_id BIGINT,
    tutor_id BIGINT,
    CONSTRAINT uk_pacientes_dni UNIQUE (dni),
    CONSTRAINT fk_pacientes_obra_social FOREIGN KEY (obra_social_id) REFERENCES obras_sociales (id),
    CONSTRAINT fk_pacientes_tutor FOREIGN KEY (tutor_id) REFERENCES pacientes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. TABLA: USUARIOS (ACCESO AL SISTEMA Y ROLES)
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    legajo VARCHAR(255),
    activo BIT(1) NOT NULL DEFAULT b'1',
    fecha_creacion DATETIME(6) NOT NULL,
    paciente_id BIGINT,
    profesional_id BIGINT,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_legajo UNIQUE (legajo),
    CONSTRAINT fk_usuarios_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_usuarios_profesional FOREIGN KEY (profesional_id) REFERENCES profesionales (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. TABLA: TURNOS
CREATE TABLE turnos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME(6),
    fecha_hora_inicio DATETIME(6) NOT NULL,
    fecha_hora_fin DATETIME(6) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    paciente_id BIGINT,
    profesional_id BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    sede_id BIGINT NOT NULL,
    motivo_consulta VARCHAR(255),
    observaciones VARCHAR(1000),
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT fk_turnos_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_turnos_profesional FOREIGN KEY (profesional_id) REFERENCES profesionales (id),
    CONSTRAINT fk_turnos_especialidad FOREIGN KEY (especialidad_id) REFERENCES especialidades (id),
    CONSTRAINT fk_turnos_sede FOREIGN KEY (sede_id) REFERENCES sedes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. TABLA: HISTORIA CLÍNICA
CREATE TABLE historia_clinica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    profesional_id BIGINT NOT NULL,
    turno_id BIGINT,
    fecha_atencion DATETIME(6) NOT NULL,
    motivo_consulta VARCHAR(500) NOT NULL,
    diagnostico VARCHAR(2000) NOT NULL,
    tratamiento VARCHAR(2000),
    recetas_prescritas VARCHAR(2000),
    estudios_solicitados VARCHAR(2000),
    CONSTRAINT uk_historia_clinica_turno UNIQUE (turno_id),
    CONSTRAINT fk_hc_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    CONSTRAINT fk_hc_profesional FOREIGN KEY (profesional_id) REFERENCES profesionales (id),
    CONSTRAINT fk_hc_turno FOREIGN KEY (turno_id) REFERENCES turnos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
