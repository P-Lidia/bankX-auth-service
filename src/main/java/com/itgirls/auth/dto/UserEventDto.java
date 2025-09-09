package com.itgirls.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEventDto {

    private String email;
    private String firstName;
    private String lastName;
    private UUID activationKey;
}
