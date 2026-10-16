package com.hotel.operations.repository;
import com.hotel.operations.domain.*;
import org.springframework.data.jpa.repository.*;

public interface HotelChangeNotificationRepository extends JpaRepository<HotelChangeNotification, Long> {
    @Query("SELECT n FROM HotelChangeNotification n WHERE n.acknowledgedAt IS NULL ORDER BY n.createdAt DESC")
    java.util.List<HotelChangeNotification> findPendingNotifications();
}