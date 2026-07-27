-- Create per-service databases and roles
CREATE USER identity_svc WITH PASSWORD 'identity_svc_dev';
CREATE USER masterdata_svc WITH PASSWORD 'masterdata_svc_dev';
CREATE USER pricing_svc WITH PASSWORD 'pricing_svc_dev';
CREATE USER rfq_svc WITH PASSWORD 'rfq_svc_dev';
CREATE USER quote_svc WITH PASSWORD 'quote_svc_dev';
CREATE USER notification_svc WITH PASSWORD 'notification_svc_dev';
CREATE USER mail_svc WITH PASSWORD 'mail_svc_dev';
CREATE USER ai_svc WITH PASSWORD 'ai_svc_dev';

CREATE DATABASE identity_db OWNER identity_svc;
CREATE DATABASE masterdata_db OWNER masterdata_svc;
CREATE DATABASE pricing_db OWNER pricing_svc;
CREATE DATABASE rfq_db OWNER rfq_svc;
CREATE DATABASE quote_db OWNER quote_svc;
CREATE DATABASE notification_db OWNER notification_svc;
CREATE DATABASE mail_db OWNER mail_svc;
CREATE DATABASE ai_db OWNER ai_svc;

\c masterdata_db
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS vector;

\c identity_db
CREATE EXTENSION IF NOT EXISTS pgcrypto;
