-- ============================================================
-- SCRIPT DE POBLACIÓN DE DATOS DE PRUEBA (MENDOZA TURNOS MVP)
-- Incluye reinicio de secuencias H2 para compatibilidad total con JPA
-- ============================================================
-- 1. OBRAS SOCIALES
INSERT IGNORE INTO obras_sociales (id, nombre, codigo) VALUES (1, 'OSEP Mendoza', 'OSEP-001');
INSERT IGNORE INTO obras_sociales (id, nombre, codigo) VALUES (2, 'PAMI', 'PAMI-190');
INSERT IGNORE INTO obras_sociales (id, nombre, codigo) VALUES (3, 'Swiss Medical', 'SM-500');
INSERT IGNORE INTO obras_sociales (id, nombre, codigo) VALUES (4, 'Particular', 'PART-000');

-- 2. ESPECIALIDADES
INSERT IGNORE INTO especialidades (id, nombre, descripcion) VALUES (1, 'Cardiología', 'Diagnóstico y tratamiento de afecciones del sistema cardiovascular.');
INSERT IGNORE INTO especialidades (id, nombre, descripcion) VALUES (2, 'Pediatría', 'Atención médica integral para lactantes, niños y adolescentes.');
INSERT IGNORE INTO especialidades (id, nombre, descripcion) VALUES (3, 'Traumatología', 'Prevención, diagnóstico y tratamiento de lesiones del aparato locomotor.');
INSERT IGNORE INTO especialidades (id, nombre, descripcion) VALUES (4, 'Clínica Médica', 'Atención clínica general y diagnóstico preventivo en adultos.');

-- 3. SEDES HOSPITALARIAS
INSERT IGNORE INTO sedes (id, nombre, direccion, telefono, ciudad) VALUES (1, 'Hospital Central de Mendoza', 'Alem 450', '0261-4490000', 'Mendoza Capital');
INSERT IGNORE INTO sedes (id, nombre, direccion, telefono, ciudad) VALUES (2, 'Hospital Pediátrico Humberto Notti', 'Av. Bandera de los Andes 2603', '0261-4132000', 'Guaymallén');
INSERT IGNORE INTO sedes (id, nombre, direccion, telefono, ciudad) VALUES (3, 'Sede Luján de Cuyo', 'San Martín 1200', '0261-4980000', 'Luján de Cuyo');

-- 4. PROFESIONALES
INSERT IGNORE INTO profesionales (id, nombre, apellido, dni, matricula, email, telefono) VALUES (1, 'Roberto', 'Pérez', '20999888', 'M-12345', 'roberto.perez@hospital.com', '2615556661');
INSERT IGNORE INTO profesionales (id, nombre, apellido, dni, matricula, email, telefono) VALUES (2, 'María', 'Gómez', '25888777', 'M-54321', 'maria.gomez@notti.gob.ar', '2615556662');
INSERT IGNORE INTO profesionales (id, nombre, apellido, dni, matricula, email, telefono) VALUES (3, 'Carlos', 'Rodríguez', '18777666', 'M-98765', 'carlos.rodriguez@central.gob.ar', '2615556663');

-- 5. RELACIÓN PROFESIONAL - ESPECIALIDAD
INSERT IGNORE INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (1, 1);
INSERT IGNORE INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (2, 2);
INSERT IGNORE INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (3, 3);
INSERT IGNORE INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (3, 4);

-- 6. PACIENTES (Actualizado a DNI de 8 dígitos para coincidir con la entidad)
INSERT IGNORE INTO pacientes (id, nombre, apellido, dni, email, telefono, fecha_nacimiento, obra_social_id, tutor_id) VALUES (1, 'Juan', 'González', '35111222', 'juan.gonzalez@gmail.com', '2614001111', '1990-05-15', 1, NULL);
INSERT IGNORE INTO pacientes (id, nombre, apellido, dni, email, telefono, fecha_nacimiento, obra_social_id, tutor_id) VALUES (2, 'Lucía', 'Martínez', '40222333', 'lucia.martinez@hotmail.com', '2614002222', '1997-11-20', 3, NULL);
INSERT IGNORE INTO pacientes (id, nombre, apellido, dni, email, telefono, fecha_nacimiento, obra_social_id, tutor_id) VALUES (3, 'Mateo', 'González', '55333444', 'tutor.juan@gmail.com', '2614001111', '2018-08-10', 1, 1);

-- 7. TURNOS INICIALES DE PRUEBA
INSERT IGNORE INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (1, '2026-10-05 09:00:00', 'DISPONIBLE', NULL, 1, 1, 1, NULL, 'Turno libre para agendar', CURRENT_TIMESTAMP);

INSERT IGNORE INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (2, '2026-10-05 10:30:00', 'DISPONIBLE', NULL, 1, 1, 1, NULL, 'Turno libre para agendar', CURRENT_TIMESTAMP);

INSERT IGNORE INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (3, '2026-10-05 11:30:00', 'RESERVADO', 1, 1, 1, 1, 'Control hipertensión arterial', 'Paciente registrado con OSEP', CURRENT_TIMESTAMP);

INSERT IGNORE INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (4, '2026-10-06 10:00:00', 'DISPONIBLE', NULL, 2, 2, 2, NULL, 'Turno libre pediatría', CURRENT_TIMESTAMP);

-- 8. USUARIOS INICIALES (Contraseña por defecto: Password123)
INSERT IGNORE INTO administradores (id, nombre, apellido, legajo) 
VALUES (1, 'Super', 'Admin', 'A-001');

INSERT IGNORE INTO usuarios (id, email, dni, password_hash, rol, activo, fecha_creacion, paciente_id, profesional_id, administrador_id)
VALUES (1, 'admin@saludmza.gob.ar', '11111111', '$2a$10$07LDPxeYRjCcPwFPY1Tl.um26a2P4mNYec5Q.MiEzscijy1nLJRqO', 'ADMINISTRADOR', true, CURRENT_TIMESTAMP, NULL, NULL, 1);

INSERT IGNORE INTO usuarios (id, email, dni, password_hash, rol, activo, fecha_creacion, paciente_id, profesional_id, administrador_id)
VALUES (2, 'juan.gonzalez@gmail.com', '35111222', '$2a$10$07LDPxeYRjCcPwFPY1Tl.um26a2P4mNYec5Q.MiEzscijy1nLJRqO', 'PACIENTE', true, CURRENT_TIMESTAMP, 1, NULL, NULL);

INSERT IGNORE INTO usuarios (id, email, dni, password_hash, rol, activo, fecha_creacion, paciente_id, profesional_id, administrador_id)
VALUES (3, 'roberto.perez@hospital.com', '20999888', '$2a$10$07LDPxeYRjCcPwFPY1Tl.um26a2P4mNYec5Q.MiEzscijy1nLJRqO', 'MEDICO', true, CURRENT_TIMESTAMP, NULL, 1, NULL);