package com.quizhub.aiagent.prompt;

import com.quizhub.aiagent.dto.InternalQuestionResponse;
import org.springframework.stereotype.Component;
import com.quizhub.aiagent.dto.internal.InternalSubmissionAnswerResponse;
import java.util.List;
@Component
public class PromptBuilder {

    public String buildExplainPrompt(InternalQuestionResponse question) {

        return """
                You are an expert programming instructor.

                Explain this question for a student.

                Question:
                %s

                A. %s
                B. %s
                C. %s
                D. %s

                Correct Answer:
                %s

                Existing Explanation:
                %s

                Rules:

                - Explain the concept.
                - Explain why the correct option is correct.
                - Explain why the other options are wrong.
                - Use beginner friendly language.
                - Do not simply repeat the answer.
                - Maximum 250 words.
                """
                .formatted(
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getCorrectAnswer(),
                        question.getExplanation()
                );
    }
    public String buildHintPrompt(InternalQuestionResponse question) {

        return """
            You are an experienced teacher.

            Give ONLY a helpful hint.

            Never reveal the correct answer.

            Never mention which option is correct.

            Question:

            %s

            Options:

            A. %s

            B. %s

            C. %s

            D. %s

            Existing Explanation:

            %s

            Rules:

            - Don't reveal the answer.
            - Give one conceptual hint.
            - Keep it under 80 words.
            - Encourage the student to think.
            """
                .formatted(
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getExplanation()
                );
    }
    public String buildAnalysisPrompt(InternalQuestionResponse question) {

        return """
        You are an expert Java educator.

        Analyze this question.

        Question:
        %s

        Option A:
        %s

        Option B:
        %s

        Option C:
        %s

        Option D:
        %s

        Return ONLY valid JSON.

        {
          "difficulty":"",
          "estimatedTime":"",
          "concepts":[],
          "commonMistakes":[],
          "recommendedTopics":[]
        }

        Do not return markdown.
        Do not explain anything outside JSON.
        """
                .formatted(
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD()
                );
    }
    public String buildReviewSubmissionPrompt(
            List<InternalQuestionResponse> questions,
            List<InternalSubmissionAnswerResponse> answers
    ) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
You are an expert programming instructor.

Review the student's quiz submission.

For every question:

1. State whether the student's answer is correct.
2. Explain the correct concept.
3. Explain why the selected answer is correct or incorrect.
4. Give one improvement tip.

Finally provide:

- Overall performance
- Strengths
- Weaknesses
- Recommended study topics

""");

        for (InternalQuestionResponse question : questions) {

            var answer = answers.stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);

            prompt.append("\n-------------------------\n");
            prompt.append("Question: ")
                    .append(question.getQuestionText())
                    .append("\n");

            prompt.append("Correct Answer: ")
                    .append(question.getCorrectAnswer())
                    .append("\n");

            if (answer != null) {
                prompt.append("Student Answer: ")
                        .append(answer.getSelectedAnswer())
                        .append("\n");
            }
        }

        return prompt.toString();
    }
    public String buildStudyPlanPrompt(
            List<InternalQuestionResponse> questions,
            List<InternalSubmissionAnswerResponse> answers
    ) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
You are an expert programming mentor.

Analyze the student's quiz performance and create a structured study plan.

Instructions:

- Identify weak areas based on incorrect answers
- Identify strong areas
- Create a 2–3 week study plan
- Suggest topics and subtopics
- Recommend learning strategy
- Keep it practical and structured

Format:

Week 1:
- Topic
- Topic

Week 2:
- Topic

Week 3:
- Topic

Also include:
- Strengths
- Weaknesses
- Final advice

""");

        for (InternalQuestionResponse question : questions) {

            var answer = answers.stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);

            prompt.append("\n-----------------\n");
            prompt.append("Question: ").append(question.getQuestionText()).append("\n");
            prompt.append("Correct Answer: ").append(question.getCorrectAnswer()).append("\n");

            if (answer != null) {
                prompt.append("Student Answer: ")
                        .append(answer.getSelectedAnswer())
                        .append("\n");

                prompt.append("Is Correct: ")
                        .append(answer.getIsCorrect())
                        .append("\n");
            }
        }

        return prompt.toString();
    }
}