package com.quizhub.aiagent.dto.internal;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchQuestionRequest {

    private List<UUID> questionIds;

}
