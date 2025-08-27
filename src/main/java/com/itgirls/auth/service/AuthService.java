package com.itgirls.auth.service;


import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.entity.User;

public interface AuthService {
    User login(LoginRequestDto loginRequestDto);
    void logout(String refreshToken);
}
