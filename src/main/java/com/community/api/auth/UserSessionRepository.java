
package com.community.api.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository
        extends JpaRepository<UserSession, String> {

    List<UserSession> findByUserId(Long userId);
}
