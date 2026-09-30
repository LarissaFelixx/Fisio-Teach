package com.app.fisiotech.auth.repository;
import com.app.fisiotech.auth.entity.AuthEmail;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuthEmailRepository extends JpaRepository<AuthEmail, String> {}
