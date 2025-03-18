package com.emailCheck.services;

import com.emailCheck.models.User;
import com.emailCheck.models.UserSession;
import com.emailCheck.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    public void createSession(User user, String token) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setToken(token);
        session.setActive(true);
        sessionRepository.save(session);
    }

    public boolean isSessionValid(String token) {
        Optional<UserSession> session = sessionRepository.findByToken(token);
        return session.isPresent() && session.get().isActive();
    }

    public void invalidateSession(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }

    public void invalidateAllSessions(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);
        for (UserSession session : sessions) {
            session.setActive(false);
        }
        sessionRepository.saveAll(sessions);
    }

    public void updateSession(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);
        if (!sessions.isEmpty()) {
            UserSession session = sessions.getFirst();
            session.setActive(true);
            sessionRepository.save(session);
        }
    }

    // Atualiza o status do token para ativo
    public void updateSessionStatusToActive(String token) {
        Optional<UserSession> session = sessionRepository.findByToken(token);
        session.ifPresent(s -> {
            if (!s.isActive()) {
                s.setActive(true);  // Atualiza o status para ativo
                sessionRepository.save(s);  // Salva no banco
            }
        });
    }

    public Optional<UserSession> findInactiveSessionByUserId(Long userId) {
        return sessionRepository.findByUserIdAndIsActiveFalse(userId);
    }


}
