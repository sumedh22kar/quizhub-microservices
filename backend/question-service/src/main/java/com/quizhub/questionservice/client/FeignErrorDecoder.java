package com.quizhub.questionservice.client;

import com.quizhub.questionservice.exception.AccessDeniedException;
import com.quizhub.questionservice.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            String url = response.request() != null ? response.request().url() : null;
            String quizId = extractQuizIdFromUrl(url);
            if (quizId != null) {
                return new ResourceNotFoundException("Quiz not found with id: " + quizId);
            }
            return new ResourceNotFoundException("Quiz not found.");
        } else if (response.status() == 403) {
            return new AccessDeniedException("You are not authorized to modify questions for this quiz.");
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }

    private String extractQuizIdFromUrl(String url) {
        if (url != null && url.contains("/quizzes/")) {
            try {
                int start = url.indexOf("/quizzes/") + 9;
                int end = url.indexOf("/", start);
                if (end == -1) {
                    end = url.indexOf("?", start);
                }
                if (end > start) {
                    return url.substring(start, end);
                } else if (start < url.length()) {
                    return url.substring(start);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}