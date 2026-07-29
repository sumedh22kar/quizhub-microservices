CREATE TABLE IF NOT EXISTS results (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL UNIQUE,
    quiz_id UUID NOT NULL,
    user_id UUID NOT NULL,
    score DOUBLE PRECISION,
    percentage DOUBLE PRECISION,
    total_questions INT,
    correct_answers INT,
    wrong_answers INT,
    passed BOOLEAN,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_results_user_id ON results(user_id);
CREATE INDEX IF NOT EXISTS idx_results_quiz_id ON results(quiz_id);
CREATE INDEX IF NOT EXISTS idx_results_submission_id ON results(submission_id);
