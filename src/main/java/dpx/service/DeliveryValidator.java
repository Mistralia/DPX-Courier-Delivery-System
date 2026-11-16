package dpx.service;

import dpx.model.Delivery;
import dpx.model.DeliveryStatus;

public class DeliveryValidator {

    public boolean isValidDelivery(Delivery delivery) {
        if (delivery == null) {
            return false;
        }

        if (delivery.getCustomer() == null) {
            return false;
        }

        if (delivery.getRecipientName() == null
                || delivery.getRecipientName().isBlank()) {
            return false;
        }

        if (delivery.getAddress() == null) {
            return false;
        }

        if (delivery.getWeight() <= 0) {
            return false;
        }

        if (delivery.getPriority() == null) {
            return false;
        }

        if (delivery.getCreationDate() == null
                || delivery.getPlannedDeliveryDate() == null) {
            return false;
        }

        if (delivery.getPlannedDeliveryDate()
                .isBefore(delivery.getCreationDate())) {
            return false;
        }

        return true;
    }

    public boolean isValidStatusTransition(
            DeliveryStatus currentStatus,
            DeliveryStatus newStatus) {

        if (currentStatus == DeliveryStatus.CREATED) {
            return newStatus == DeliveryStatus.ASSIGNED
                    || newStatus == DeliveryStatus.CANCELLED;
        }

        if (currentStatus == DeliveryStatus.ASSIGNED) {
            return newStatus == DeliveryStatus.IN_TRANSIT
                    || newStatus == DeliveryStatus.CANCELLED;
        }

        if (currentStatus == DeliveryStatus.IN_TRANSIT) {
            return newStatus == DeliveryStatus.DELIVERED
                    || newStatus == DeliveryStatus.CANCELLED;
        }

        if (currentStatus == DeliveryStatus.CANCELLED) {
            return newStatus == DeliveryStatus.CREATED;
        }

        return false;
    }
}
