package com.hotel.operations.dto;

import java.util.List;

public class NotificationResponses {
    public record ChangeItem(String field, String from, String to) {}
    public record AdminNotification(
        Long id, Long hotelId, String hotelName, Long changedBy, String changedAt, List<ChangeItem> changes
    ) {}
}