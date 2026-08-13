package com.quizhub.aiagent.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class AiExceptionHandler {

    @ExceptionHandler(AiResponseParsingException.class)
    public ResponseEntity<Map<String, Object>> handleAiResponseParsing(
            AiResponseParsingException ex
    ) {

        log.error("AI response parsing failed", ex);

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "AI service returned an invalid response."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex
    ) {

        log.error("Unexpected AI Agent error", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing the AI request."
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message
    ) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", false);
        body.put("message", message);
        body.put("timestamp", Instant.now());

        return ResponseEntity
                .status(status)
                .body(body);
    }
}