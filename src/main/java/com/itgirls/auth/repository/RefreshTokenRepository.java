package com.itgirls.auth.repository;

import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String token);
    void deleteByUser(User user);
    boolean existsByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
}
