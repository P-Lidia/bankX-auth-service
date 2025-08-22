package com.itgirls.auth.service;

import com.itgirls.auth.dto.AuthRequestDto;
import com.itgirls.auth.entity.User;

public interface AuthService {
    User register(AuthRequestDto authRequestDto);
    User activateAccount(String token);
}