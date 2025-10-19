package com.bankx.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEventDto {

    private String email;
    private String firstName;
    private String lastName;
    private String emailToken;
}