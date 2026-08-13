package com.quizhub.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import com.quizhub.aiagent.exception.AiResponseParsingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public QuestionAnalysisResponse parse(String rawResponse) {
        return parseAnalysis(rawResponse);
    }

    public QuestionAnalysisResponse parseAnalysis(String rawResponse) {

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AiResponseParsingException(
                    "AI returned an empty response."
            );
        }

        try {

            String json = extractJson(rawResponse);

            JsonNode root = objectMapper.readTree(json);

            validateAnalysis(root);

            return objectMapper.treeToValue(
                    root,
                    QuestionAnalysisResponse.class
            );

        } catch (JsonProcessingException e) {

            throw new AiResponseParsingException(
                    "AI returned invalid JSON.",
                    e
            );
        }
    }

    private String extractJson(String response) {

        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {

            int firstNewLine = cleaned.indexOf('\n');

            int lastFence = cleaned.lastIndexOf("```");

            if (firstNewLine != -1 && lastFence > firstNewLine) {

                cleaned = cleaned.substring(
                        firstNewLine + 1,
                        lastFence
                ).trim();
            }
        }

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1) {

            throw new AiResponseParsingException(
                    "AI response does not contain a JSON object."
            );
        }

        return cleaned.substring(
                firstBrace,
                lastBrace + 1
        );
    }

    private void validateAnalysis(JsonNode root) {

        if (!root.isObject()) {
            throw new AiResponseParsingException(
                    "AI analysis response must be a JSON object."
            );
        }

        if (!root.has("difficulty")) {
            throw new AiResponseParsingException(
                    "AI analysis response missing 'difficulty'."
            );
        }

        if (!root.has("estimatedTime")) {
            throw new AiResponseParsingException(
                    "AI analysis response missing 'estimatedTime'."
            );
        }

        if (!root.has("concepts")) {
            throw new AiResponseParsingException(
                    "AI analysis response missing 'concepts'."
            );
        }

        if (!root.has("commonMistakes")) {
            throw new AiResponseParsingException(
                    "AI analysis response missing 'commonMistakes'."
            );
        }

        if (!root.has("recommendedTopics")) {
            throw new AiResponseParsingException(
                    "AI analysis response missing 'recommendedTopics'."
            );
        }
    }
}