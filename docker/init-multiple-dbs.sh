#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE ai_assistant_db;
    CREATE DATABASE property_db;
EOSQL

# Enable pgvector extension in ai_assistant_db (required for RAG/PGvector)
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname="ai_assistant_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS vector;
EOSQL
