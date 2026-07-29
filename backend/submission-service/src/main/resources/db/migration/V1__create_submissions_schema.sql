CREATE TABLE IF NOT EXISTS submissions (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL,
    user_id UUID NOT NULL,
    score DOUBLE PRECISION,
    total_marks INT,
    percentage DOUBLE PRECISION,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    submitted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS submission_answers (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    question_id UUID NOT NULL,
    selected_answer VARCHAR(500),
    correct_answer VARCHAR(500),
    is_correct BOOLEAN,
    marks_awarded DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
