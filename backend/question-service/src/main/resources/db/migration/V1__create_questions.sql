CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL,
    question_text VARCHAR(1000) NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    option_a VARCHAR(500),
    option_b VARCHAR(500),
    option_c VARCHAR(500),
    option_d VARCHAR(500),
    correct_answer VARCHAR(500) NOT NULL,
    marks INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
