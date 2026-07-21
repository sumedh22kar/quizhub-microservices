package com.quizhub.authservice.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;
}