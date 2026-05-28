-- ============================================================
-- Migración: Módulo Recordatorios
-- Ejecutar en Supabase (schema public)
-- ============================================================

-- 1. Tablas nuevas
CREATE TABLE IF NOT EXISTS categorias_recordatorio (
    id_categoria_recordatorio SERIAL PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL,
    descripcion VARCHAR(255),
    est_activo  BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS recordatorios (
    id_recordatorio SERIAL PRIMARY KEY,
    titulo          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(255),
    fec_hora        TIMESTAMPTZ  NOT NULL,
    recurrencia     VARCHAR(20)  NOT NULL DEFAULT 'NINGUNA',
    id_funcionario  INT          NOT NULL REFERENCES usuarios(id_usuario),
    id_estudiante   INT          REFERENCES usuarios(id_usuario),
    id_categoria    INT          REFERENCES categorias_recordatorio(id_categoria_recordatorio),
    est_activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- 2. Nuevos permisos
INSERT INTO permisos (codigo) VALUES
    ('recordatorios.leer'),
    ('recordatorios.crear'),
    ('recordatorios.modificar'),
    ('recordatorios.eliminar'),
    ('recordatorios.gestionar')
ON CONFLICT DO NOTHING;

-- 3. Asignar a roles (ajustar id_rol si difiere en tu BD)
--    id_rol 1=Administrador, 2=Psicopedagogo, 3=Analista, 4=Responsable

-- Admin: todos los permisos de recordatorios
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 1, id_permiso FROM permisos
WHERE codigo IN (
    'recordatorios.leer','recordatorios.crear',
    'recordatorios.modificar','recordatorios.eliminar',
    'recordatorios.gestionar'
)
ON CONFLICT DO NOTHING;

-- Psicopedagogo: operativo
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 2, id_permiso FROM permisos
WHERE codigo IN (
    'recordatorios.leer','recordatorios.crear',
    'recordatorios.modificar','recordatorios.eliminar'
)
ON CONFLICT DO NOTHING;

-- Analista y Responsable: solo lectura
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r, permisos p
WHERE r.nombre IN ('Analista Educativo', 'Responsable Educativo')
  AND p.codigo = 'recordatorios.leer'
ON CONFLICT DO NOTHING;
