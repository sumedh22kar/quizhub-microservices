package com.quizhub.questionservice.mapper;

import com.quizhub.questionservice.dto.internal.InternalQuestionResponse;
import com.quizhub.questionservice.dto.request.CreateQuestionRequest;
import com.quizhub.questionservice.dto.request.UpdateQuestionRequest;
import com.quizhub.questionservice.dto.response.QuestionResponse;
import com.quizhub.questionservice.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    Question toEntity(CreateQuestionRequest request);

    QuestionResponse toResponse(Question question);

    InternalQuestionResponse toInternal(Question question);

    void updateEntity(UpdateQuestionRequest request, @MappingTarget Question question);
}
