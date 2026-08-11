package com.quizhub.questionservice.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdatedEvent {

    private UUID questionId;
}