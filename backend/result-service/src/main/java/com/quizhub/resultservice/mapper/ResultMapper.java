package com.quizhub.resultservice.mapper;

import com.quizhub.resultservice.dto.response.ResultResponse;
import com.quizhub.resultservice.entity.Result;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultResponse toResponse(Result result);

    List<ResultResponse> toResponseList(List<Result> results);
}
