package com.quizhub.submissionservice.mapper;

import com.quizhub.submissionservice.dto.internal.InternalSubmissionAnswerResponse;
import com.quizhub.submissionservice.dto.internal.InternalSubmissionResponse;
import com.quizhub.submissionservice.dto.response.SubmissionAnswerResponse;
import com.quizhub.submissionservice.dto.response.SubmissionResponse;
import com.quizhub.submissionservice.entity.Submission;
import com.quizhub.submissionservice.entity.SubmissionAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {

    @Mapping(target = "submissionId", source = "submission.id")
    SubmissionAnswerResponse toAnswerResponse(SubmissionAnswer answer);

    List<SubmissionAnswerResponse> toAnswerResponseList(List<SubmissionAnswer> answers);

    SubmissionResponse toResponse(Submission submission);

    List<SubmissionResponse> toResponseList(List<Submission> submissions);

    InternalSubmissionAnswerResponse toInternalAnswerResponse(SubmissionAnswer answer);

    InternalSubmissionResponse toInternalResponse(Submission submission);
}
