package com.cedarco.repository;

import com.cedarco.entity.FcmToken;
import com.cedarco.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByUser(User user);
    Optional<FcmToken> findByToken(String token);
    void deleteByToken(String token);
}
