package com.quizhub.aiagent.prompt;

import com.quizhub.aiagent.dto.InternalQuestionResponse;
import org.springframework.stereotype.Component;

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
}