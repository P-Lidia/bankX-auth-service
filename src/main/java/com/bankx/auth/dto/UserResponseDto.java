package com.bankx.auth.dto;

import com.bankx.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String name;
    private String surname;
    private String email;
    private User.Status status;
}