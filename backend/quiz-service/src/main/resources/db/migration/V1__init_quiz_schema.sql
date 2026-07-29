-- V1__init_quiz_schema.sql

-- Enable UUID extension if PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create quizzes table
CREATE TABLE IF NOT EXISTS quizzes (
    id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    duration_minutes INT,
    total_marks INT,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index for fast lookup by owner
CREATE INDEX IF NOT EXISTS idx_quizzes_owner_id ON quizzes(owner_id);
-- Index for filtering by status
CREATE INDEX IF NOT EXISTS idx_quizzes_status ON quizzes(status);
