package com.app.fisiotech.auth.repository;
import com.app.fisiotech.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {}
