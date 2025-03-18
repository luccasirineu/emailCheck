package com.emailCheck.repositories;

import com.emailCheck.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);
    List<UserSession> findByUserId(Long userId);
    Optional<UserSession> findByUserIdAndIsActiveFalse(Long userId);

}
