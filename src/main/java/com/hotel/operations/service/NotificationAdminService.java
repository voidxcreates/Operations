
package com.hotel.operations.service;

import com.hotel.operations.domain.HotelChangeNotification;
import com.hotel.operations.repository.HotelChangeNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationAdminService {

    private final HotelChangeNotificationRepository notificationRepository;

    public NotificationAdminService(
            HotelChangeNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<HotelChangeNotification> getPendingNotifications() {
        return notificationRepository.findPendingNotifications();
    }

    @Transactional
    public void acknowledgeNotifications(List<Long> ids, Long adminUserId) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one notification ID is required");
        }

        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException(
                    "Notification IDs must be positive numbers");
        }

        Set<Long> uniqueIds = new HashSet<>(ids);

        List<HotelChangeNotification> notifications =
                notificationRepository.findAllById(uniqueIds);

        if (notifications.size() != uniqueIds.size()) {
            throw new IllegalArgumentException(
                    "One or more notification IDs do not exist");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (HotelChangeNotification notification : notifications) {
            if (notification.getAcknowledgedAt() == null) {
                notification.setAcknowledgedBy(adminUserId);
                notification.setAcknowledgedAt(now);
            }
        }

        notificationRepository.saveAll(notifications);
    }
}
