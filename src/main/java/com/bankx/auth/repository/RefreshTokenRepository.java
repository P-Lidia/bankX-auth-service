package com.bankx.auth.repository;

import com.bankx.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByTokenValue(String tokenValue);
    boolean existsByTokenValue(String tokenValue);
    Optional<RefreshToken> findByTokenValue(String tokenValue);
}