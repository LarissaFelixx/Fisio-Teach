package com.app.fisiotech.auth.repository;

import com.app.fisiotech.auth.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    long countBySubjectAndCreatedAtAfter(String subject, Instant after);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.tokenHash = :hash")
    Optional<PasswordResetToken> findLocked(@Param("hash") String hash);
    @Modifying
    @Query("update PasswordResetToken t set t.consumed = true where t.subject = :subject and t.consumed = false")
    void consumeAllBySubject(@Param("subject") String subject);
}
