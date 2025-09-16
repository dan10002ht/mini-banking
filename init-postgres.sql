-- Initialize PostgreSQL for Mini Banking System
-- Database already created by POSTGRES_DB environment variable

-- Create postgres user if not exists
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'postgres') THEN

      CREATE ROLE postgres LOGIN SUPERUSER CREATEDB CREATEROLE PASSWORD 'postgres';
   END IF;
END
$do$;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE mini_banking TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;
