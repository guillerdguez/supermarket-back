-- El proyecto no usa Flyway/Liquibase (ddl-auto=none, schema en data.sql via
-- CREATE TABLE IF NOT EXISTS). En bases ya existentes ese CREATE no agrega
-- columnas nuevas, asi que hay que correr esto a mano una sola vez por base.
--
-- Bases nuevas (creadas desde cero) ya salen completas via data.sql y NO
-- necesitan este script.

ALTER TABLE notifications
  ADD COLUMN reference_type VARCHAR(20) NULL,
  ADD COLUMN reference_id BIGINT NULL;
