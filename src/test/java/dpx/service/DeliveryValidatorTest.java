package dpx.service;

import dpx.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryValidatorTest {

    private DeliveryValidator validator;
    private Customer customer;
    private Address address;

    @BeforeEach
    public void setUp() {
        validator = new DeliveryValidator();
        customer = new Customer(1, "Test Customer", "12345678");
        address = new Address("Riga", "Brivibas", "1", "1001");
    }

    @Test
    public void testIsValidDelivery() {
        Delivery d = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        assertTrue(validator.isValidDelivery(d));

        assertFalse(validator.isValidDelivery(null));
        
        Delivery dNullCustomer = new Delivery(1002, null, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        assertFalse(validator.isValidDelivery(dNullCustomer));

        Delivery dNullAddress = new Delivery(1003, customer, "R", null, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        assertFalse(validator.isValidDelivery(dNullAddress));

        d.setRecipientName(" ");
        assertFalse(validator.isValidDelivery(d));
        d.setRecipientName("");
        assertFalse(validator.isValidDelivery(d));
        d.setRecipientName(null);
        assertFalse(validator.isValidDelivery(d));
        d.setRecipientName("Recipient");

        d.setWeight(0);
        assertFalse(validator.isValidDelivery(d));
        d.setWeight(-1.5);
        assertFalse(validator.isValidDelivery(d));
        d.setWeight(1.0);

        Delivery dNullPriority = new Delivery(1004, customer, "R", address, 1.0, null, LocalDate.now(), LocalDate.now());
        assertFalse(validator.isValidDelivery(dNullPriority));

        Delivery dNullCreationDate = new Delivery(1005, customer, "R", address, 1.0, DeliveryPriority.NORMAL, null, LocalDate.now());
        assertFalse(validator.isValidDelivery(dNullCreationDate));

        Delivery dNullPlannedDate = new Delivery(1006, customer, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), null);
        assertFalse(validator.isValidDelivery(dNullPlannedDate));

        Delivery dInvalidDate = new Delivery(1007, customer, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().minusDays(1));
        assertFalse(validator.isValidDelivery(dInvalidDate));
        
        d.setPlannedDeliveryDate(LocalDate.now());
        assertTrue(validator.isValidDelivery(d));
    }

    @Test
    public void testIsValidStatusTransition() {
        assertTrue(validator.isValidStatusTransition(DeliveryStatus.CREATED, DeliveryStatus.ASSIGNED));
        assertTrue(validator.isValidStatusTransition(DeliveryStatus.CREATED, DeliveryStatus.CANCELLED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.CREATED, DeliveryStatus.IN_TRANSIT));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.CREATED, DeliveryStatus.DELIVERED));

        assertTrue(validator.isValidStatusTransition(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT));
        assertTrue(validator.isValidStatusTransition(DeliveryStatus.ASSIGNED, DeliveryStatus.CANCELLED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.ASSIGNED, DeliveryStatus.CREATED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.ASSIGNED, DeliveryStatus.DELIVERED));

        assertTrue(validator.isValidStatusTransition(DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELIVERED));
        assertTrue(validator.isValidStatusTransition(DeliveryStatus.IN_TRANSIT, DeliveryStatus.CANCELLED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.IN_TRANSIT, DeliveryStatus.CREATED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.IN_TRANSIT, DeliveryStatus.ASSIGNED));

        assertFalse(validator.isValidStatusTransition(DeliveryStatus.DELIVERED, DeliveryStatus.IN_TRANSIT));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.DELIVERED, DeliveryStatus.CREATED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.DELIVERED, DeliveryStatus.ASSIGNED));

        assertTrue(validator.isValidStatusTransition(DeliveryStatus.CANCELLED, DeliveryStatus.CREATED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.CANCELLED, DeliveryStatus.ASSIGNED));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.CANCELLED, DeliveryStatus.IN_TRANSIT));
        assertFalse(validator.isValidStatusTransition(DeliveryStatus.CANCELLED, DeliveryStatus.DELIVERED));
    }
}
