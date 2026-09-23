-- ============================================================
-- SCRIPT DE POBLACIÓN DE DATOS DE PRUEBA (MENDOZA TURNOS MVP)
-- Incluye reinicio de secuencias H2 para compatibilidad total con JPA
-- ============================================================

-- 1. OBRAS SOCIALES
INSERT INTO obras_sociales (id, nombre, codigo) VALUES (1, 'OSEP Mendoza', 'OSEP-001');
INSERT INTO obras_sociales (id, nombre, codigo) VALUES (2, 'PAMI', 'PAMI-190');
INSERT INTO obras_sociales (id, nombre, codigo) VALUES (3, 'Swiss Medical', 'SM-500');
INSERT INTO obras_sociales (id, nombre, codigo) VALUES (4, 'Particular', 'PART-000');

-- 2. ESPECIALIDADES
INSERT INTO especialidades (id, nombre, descripcion) VALUES (1, 'Cardiología', 'Diagnóstico y tratamiento de afecciones del sistema cardiovascular.');
INSERT INTO especialidades (id, nombre, descripcion) VALUES (2, 'Pediatría', 'Atención médica integral para lactantes, niños y adolescentes.');
INSERT INTO especialidades (id, nombre, descripcion) VALUES (3, 'Traumatología', 'Prevención, diagnóstico y tratamiento de lesiones del aparato locomotor.');
INSERT INTO especialidades (id, nombre, descripcion) VALUES (4, 'Clínica Médica', 'Atención clínica general y diagnóstico preventivo en adultos.');

-- 3. SEDES HOSPITALARIAS
INSERT INTO sedes (id, nombre, direccion, telefono, ciudad) VALUES (1, 'Hospital Central de Mendoza', 'Alem 450', '0261-4490000', 'Mendoza Capital');
INSERT INTO sedes (id, nombre, direccion, telefono, ciudad) VALUES (2, 'Hospital Pediátrico Humberto Notti', 'Av. Bandera de los Andes 2603', '0261-4132000', 'Guaymallén');
INSERT INTO sedes (id, nombre, direccion, telefono, ciudad) VALUES (3, 'Sede Luján de Cuyo', 'San Martín 1200', '0261-4980000', 'Luján de Cuyo');

-- 4. PROFESIONALES
INSERT INTO profesionales (id, nombre, apellido, dni, matricula, email, telefono) VALUES (1, 'Roberto', 'Pérez', '20999888', 'M-12345', 'roberto.perez@hospital.com', '2615556661');
INSERT INTO profesionales (id, nombre, apellido, dni, matricula, email, telefono) VALUES (2, 'María', 'Gómez', '25888777', 'M-54321', 'maria.gomez@notti.gob.ar', '2615556662');
INSERT INTO profesionales (id, nombre, apellido, dni, matricula, email, telefono) VALUES (3, 'Carlos', 'Rodríguez', '18777666', 'M-98765', 'carlos.rodriguez@central.gob.ar', '2615556663');

-- 5. RELACIÓN PROFESIONAL - ESPECIALIDAD
INSERT INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (1, 1);
INSERT INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (2, 2);
INSERT INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (3, 3);
INSERT INTO profesional_especialidad (profesional_id, especialidad_id) VALUES (3, 4);

-- 6. PACIENTES
INSERT INTO pacientes (id, nombre, apellido, dni, email, telefono, fecha_nacimiento, obra_social_id, tutor_id) VALUES (1, 'Juan', 'González', '35111222', 'juan.gonzalez@gmail.com', '2614001111', '1990-05-15', 1, NULL);
INSERT INTO pacientes (id, nombre, apellido, dni, email, telefono, fecha_nacimiento, obra_social_id, tutor_id) VALUES (2, 'Lucía', 'Martínez', '40222333', 'lucia.martinez@hotmail.com', '2614002222', '1997-11-20', 3, NULL);
INSERT INTO pacientes (id, nombre, apellido, dni, email, telefono, fecha_nacimiento, obra_social_id, tutor_id) VALUES (3, 'Mateo', 'González', '55333444', 'tutor.juan@gmail.com', '2614001111', '2018-08-10', 1, 1);

-- 7. TURNOS INICIALES DE PRUEBA
INSERT INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (1, '2026-10-05 09:00:00', 'DISPONIBLE', NULL, 1, 1, 1, NULL, 'Turno libre para agendar', CURRENT_TIMESTAMP);

INSERT INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (2, '2026-10-05 10:30:00', 'DISPONIBLE', NULL, 1, 1, 1, NULL, 'Turno libre para agendar', CURRENT_TIMESTAMP);

INSERT INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (3, '2026-10-05 11:30:00', 'RESERVADO', 1, 1, 1, 1, 'Control hipertensión arterial', 'Paciente registrado con OSEP', CURRENT_TIMESTAMP);

INSERT INTO turnos (id, fecha_hora, estado, paciente_id, profesional_id, especialidad_id, sede_id, motivo_consulta, observaciones, fecha_creacion)
VALUES (4, '2026-10-06 10:00:00', 'DISPONIBLE', NULL, 2, 2, 2, NULL, 'Turno libre pediatría', CURRENT_TIMESTAMP);


-- REINICIO DE SECUENCIAS H2 PARA PREVENIR COLISIONES DE ID
ALTER TABLE obras_sociales ALTER COLUMN id RESTART WITH 10;
ALTER TABLE especialidades ALTER COLUMN id RESTART WITH 10;
ALTER TABLE sedes ALTER COLUMN id RESTART WITH 10;
ALTER TABLE profesionales ALTER COLUMN id RESTART WITH 10;
ALTER TABLE pacientes ALTER COLUMN id RESTART WITH 10;
ALTER TABLE turnos ALTER COLUMN id RESTART WITH 10;
