package com.quizhub.aiagent.service;

import com.quizhub.aiagent.dto.response.QuestionAnalysisResponse;
import com.quizhub.aiagent.exception.AiResponseParsingException;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRetryServiceTest {

    @Mock
    private LLMService llmService;

    @Mock
    private AiResponseParser aiResponseParser;

    @Test
    void shouldRetryWhenFirstResponseIsInvalid() {

        String prompt = "Analyze this question.";

        String invalidResponse =
                "This is not valid JSON";

        String validResponse = """
                {
                  "difficulty": "basic",
                  "estimatedTime": "2-5 minutes",
                  "concepts": ["Spring Boot"],
                  "commonMistakes": ["Confusing Spring with Spring Boot"],
                  "recommendedTopics": ["Spring Boot basics"]
                }
                """;

        QuestionAnalysisResponse expected =
                QuestionAnalysisResponse.builder()
                        .difficulty("basic")
                        .estimatedTime("2-5 minutes")
                        .concepts(
                                java.util.List.of("Spring Boot")
                        )
                        .commonMistakes(
                                java.util.List.of(
                                        "Confusing Spring with Spring Boot"
                                )
                        )
                        .recommendedTopics(
                                java.util.List.of(
                                        "Spring Boot basics"
                                )
                        )
                        .build();

        when(llmService.chat(anyString()))
                .thenReturn(invalidResponse)
                .thenReturn(validResponse);

        when(aiResponseParser.parse(invalidResponse))
                .thenThrow(
                        new AiResponseParsingException(
                                "Invalid JSON"
                        )
                );

        when(aiResponseParser.parse(validResponse))
                .thenReturn(expected);

        AiRetryService retryService =
                new AiRetryService(
                        llmService,
                        aiResponseParser
                );

        QuestionAnalysisResponse result =
                retryService.executeWithRetry(
                        prompt,
                        aiResponseParser::parse
                );

        assertNotNull(result);
        assertEquals("basic", result.getDifficulty());

        verify(llmService, times(2))
                .chat(anyString());

        verify(aiResponseParser, times(2))
                .parse(anyString());
    }

    @Test
    void shouldFailAfterRetryIsExhausted() {

        String prompt = "Analyze this question.";

        String invalidResponse =
                "Not JSON";

        when(llmService.chat(anyString()))
                .thenReturn(invalidResponse);

        when(aiResponseParser.parse(anyString()))
                .thenThrow(
                        new AiResponseParsingException(
                            "Invalid JSON"
                        )
                );

        AiRetryService retryService =
                new AiRetryService(
                        llmService,
                        aiResponseParser
                );

        assertThrows(
                AiResponseParsingException.class,
                () -> retryService.executeWithRetry(
                        prompt,
                        aiResponseParser::parse
                )
        );

        verify(llmService, times(2))
                .chat(anyString());

        verify(aiResponseParser, times(2))
                .parse(anyString());
    }
}
