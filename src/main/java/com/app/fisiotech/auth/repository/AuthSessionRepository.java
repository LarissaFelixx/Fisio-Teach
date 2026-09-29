package com.app.fisiotech.auth.repository;

import com.app.fisiotech.auth.entity.AuthSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AuthSession s where s.id = :id")
    Optional<AuthSession> findLockedById(@Param("id") String id);

    @Modifying
    @Query("update AuthSession s set s.revoked = true where s.subject = :subject")
    void revokeBySubject(@Param("subject") String subject);
}
