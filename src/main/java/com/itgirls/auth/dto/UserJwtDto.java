package com.itgirls.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserJwtDto {
    private Long id;
    private String name;
    private String role; // role.getCode в маппинге
}
