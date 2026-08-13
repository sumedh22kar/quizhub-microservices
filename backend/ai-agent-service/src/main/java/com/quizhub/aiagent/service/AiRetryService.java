package com.quizhub.aiagent.service;

import com.quizhub.aiagent.exception.AiResponseParsingException;
import com.quizhub.aiagent.infrastructure.llm.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRetryService {

    private static final int MAX_RETRIES = 1;

    private final LLMService llmService;
    private final AiResponseParser aiResponseParser;

    public <T> T executeWithRetry(
            String prompt,
            AiResponseParserFunction<T> parser
    ) {

        String currentPrompt = prompt;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {

            try {

                log.info(
                        "AI generation attempt {}/{}",
                        attempt + 1,
                        MAX_RETRIES + 1
                );

                String rawResponse =
                        llmService.chat(currentPrompt);

                return parser.parse(rawResponse);

            } catch (AiResponseParsingException ex) {

                log.warn(
                        "AI response parsing failed on attempt {}: {}",
                        attempt + 1,
                        ex.getMessage()
                );

                if (attempt == MAX_RETRIES) {
                    throw ex;
                }

                currentPrompt = prompt + """


                        IMPORTANT:
                        Your previous response was invalid.

                        Return ONLY valid JSON.
                        Do not use markdown.
                        Do not use ``` fences.
                        Do not add explanations before or after the JSON.
                        """
                        ;
            }
        }

        throw new IllegalStateException(
                "AI generation failed unexpectedly."
        );
    }

    @FunctionalInterface
    public interface AiResponseParserFunction<T> {
        T parse(String rawResponse);
    }
}