package com.itgirls.auth.mapper;

import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.UserJwtDto;
import com.itgirls.auth.dto.UserResponseDto;
import com.itgirls.auth.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(userCreateDto.getPassword()))")
    User toEntity(RegistrationRequestDto registrationRequestDto, @Context PasswordEncoder passwordEncoder);

    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(LoginRequestDto loginRequestDto);

    @Mapping(source = "role.code", target = "role")  // берем поле code из Role
    UserJwtDto toUserJwtDto(User user);
}