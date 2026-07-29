package com.quizhub.submissionservice.controller;

import com.quizhub.submissionservice.dto.internal.InternalSubmissionResponse;
import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.exception.ResourceNotFoundException;
import com.quizhub.submissionservice.mapper.SubmissionMapper;
import com.quizhub.submissionservice.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/submissions")
@RequiredArgsConstructor
public class InternalSubmissionController {

    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;

    @GetMapping("/{id}")
    public ResponseEntity<InternalSubmissionResponse> getSubmissionById(@PathVariable("id") UUID id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with ID: " + id));
        return ResponseEntity.ok(submissionMapper.toInternalResponse(submission));
    }
}
