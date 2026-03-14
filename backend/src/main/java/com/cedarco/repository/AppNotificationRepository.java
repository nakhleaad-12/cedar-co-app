package com.cedarco.repository;

import com.cedarco.entity.AppNotification;
import com.cedarco.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    long countByUserAndIsReadFalse(User user);

    @Modifying
    @Query("UPDATE AppNotification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadForUser(User user);
}
