package dpx.service;

import dpx.model.*;
import dpx.persistence.DpxData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;

public class DeliveryManagerTest {

    private DeliveryManager manager;
    private Customer customer;
    private Address address;
    private Courier courier;

    @BeforeEach
    public void setUp() {
        manager = new DeliveryManager();
        customer = new Customer(1, "Test Customer", "123456");
        address = new Address("Test City", "Test Street", "1", "1234");
        courier = new Courier(1, "Test Courier", "654321");
        
        manager.addCustomer(customer);
        manager.addCourier(courier);
    }

    @Test
    public void testAddValidDelivery() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        assertTrue(manager.addDelivery(delivery), "Valid delivery should be added");
        assertEquals(1, manager.getDeliveries().size());
    }

    @Test
    public void testAddDuplicateDeliveryId() {
        Delivery delivery1 = new Delivery(1001, customer, "Recipient 1", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        Delivery delivery2 = new Delivery(1001, customer, "Recipient 2", address, 3.0, DeliveryPriority.HIGH, LocalDate.now(), LocalDate.now().plusDays(2));
        
        assertTrue(manager.addDelivery(delivery1));
        assertFalse(manager.addDelivery(delivery2), "Duplicate delivery ID should be rejected");
    }

    @Test
    public void testAddDeliveryWithNegativeWeight() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, -1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        assertFalse(manager.addDelivery(delivery), "Delivery with negative weight should be rejected");
    }

    @Test
    public void testAddDeliveryWithInvalidDate() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().minusDays(1));
        assertFalse(manager.addDelivery(delivery), "Delivery with planned date before creation date should be rejected");
    }

    @Test
    public void testFindDeliveryById() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(delivery);
        
        Delivery found = manager.findDeliveryById(1001);
        assertNotNull(found);
        assertEquals("Recipient", found.getRecipientName());
        
        assertNull(manager.findDeliveryById(9999), "Should return null for unknown ID");
    }

    @Test
    public void testStatusTransitions() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(delivery);
        
        assertEquals(DeliveryStatus.CREATED, delivery.getStatus());
        
        manager.assignCourier(1001, 1);
        assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());
        
        assertTrue(manager.updateDeliveryStatus(1001, DeliveryStatus.IN_TRANSIT));
        
        assertTrue(manager.updateDeliveryStatus(1001, DeliveryStatus.DELIVERED));
        
        assertFalse(manager.updateDeliveryStatus(1001, DeliveryStatus.CANCELLED));

        assertFalse(manager.updateDeliveryStatus(1001, DeliveryStatus.IN_TRANSIT));
    }

    @Test
    public void testStatusTransitionsWithCourierRequirement() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(delivery);

        assertFalse(manager.updateDeliveryStatus(1001, DeliveryStatus.ASSIGNED));

        assertTrue(manager.assignCourier(1001, 1));
        assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());
        assertNotNull(delivery.getCourier());

        assertTrue(manager.updateDeliveryStatus(1001, DeliveryStatus.IN_TRANSIT));

        Delivery d2 = new Delivery(1002, customer, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d2);
        
        Delivery dNoCourier = new Delivery(1003, customer, "R3", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        dNoCourier.setStatus(DeliveryStatus.ASSIGNED);
        manager.addDelivery(dNoCourier);
        
        assertFalse(manager.updateDeliveryStatus(1003, DeliveryStatus.IN_TRANSIT));
    }

    @Test
    public void testFindDeliveriesByCustomer() {
        Customer c2 = new Customer(2, "Customer 2", "222");
        manager.addCustomer(c2);

        manager.addDelivery(new Delivery(1001, customer, "R1", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now()));
        manager.addDelivery(new Delivery(1002, c2, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now()));
        manager.addDelivery(new Delivery(1003, customer, "R3", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now()));

        ArrayList<Delivery> found = manager.findDeliveriesByCustomer(1);
        assertEquals(2, found.size());
        assertEquals(1001, found.get(0).getId());
        assertEquals(1003, found.get(1).getId());
    }

    @Test
    public void testCourierAssignment() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(delivery);
        
        assertTrue(manager.assignCourier(1001, 1));
        assertEquals(courier, delivery.getCourier());
        assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());

        Courier courierB = new Courier(2, "Courier B", "222");
        manager.addCourier(courierB);
        assertTrue(manager.assignCourier(1001, 2));
        assertEquals(courierB, delivery.getCourier());

        assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());

        assertTrue(manager.assignCourier(1001, 2));

        manager.updateDeliveryStatus(1001, DeliveryStatus.IN_TRANSIT);
        assertFalse(manager.assignCourier(1001, 1));

        Delivery delivery2 = new Delivery(1002, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(delivery2);
        manager.updateDeliveryStatus(1002, DeliveryStatus.CANCELLED);
        assertFalse(manager.assignCourier(1002, 1));
    }

    @Test
    public void testCourierWorkloadLimit() {
        Courier courierLimit = new Courier(2, "Limit Courier", "000");
        manager.addCourier(courierLimit);
        
        for (int i = 1; i <= 10; i++) {
            Delivery d = new Delivery(2000 + i, customer, "R" + i, address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
            manager.addDelivery(d);
            assertTrue(manager.assignCourier(d.getId(), 2));
        }
        
        Delivery d11 = new Delivery(2011, customer, "R11", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(d11);
        assertFalse(manager.assignCourier(2011, 2));

        Delivery dToReassign = new Delivery(3000, customer, "R3000", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(dToReassign);
        manager.assignCourier(3000, 1);
        assertFalse(manager.assignCourier(3000, 2));
        
        manager.updateDeliveryStatus(2001, DeliveryStatus.IN_TRANSIT);
        manager.updateDeliveryStatus(2001, DeliveryStatus.DELIVERED);
        
        assertTrue(manager.assignCourier(2011, 2));
    }

    @Test
    public void testUpdateRecipient() {
        Delivery d = new Delivery(1001, customer, "Original", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d);
        
        assertTrue(manager.updateRecipient(1001, "  New Name  "));
        assertEquals("New Name", d.getRecipientName());
        
        assertFalse(manager.updateRecipient(1001, ""));
        assertFalse(manager.updateRecipient(1001, null));
    }

    @Test
    public void testUpdatePlannedDate() {
        LocalDate creationDate = LocalDate.now();
        Delivery d = new Delivery(1001, customer, "R", address, 1.0, DeliveryPriority.NORMAL, creationDate, creationDate.plusDays(1));
        manager.addDelivery(d);
        
        assertTrue(manager.updatePlannedDate(1001, creationDate.plusDays(2)));
        assertEquals(creationDate.plusDays(2), d.getPlannedDeliveryDate());
        
        assertFalse(manager.updatePlannedDate(1001, creationDate.minusDays(1)), "Date before creation should be rejected");
    }

    @Test
    public void testUpdateAddress() {
        Delivery d = new Delivery(1001, customer, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d);
        
        Address newAddr = new Address("New City", "New Street", "99", "1010");
        assertTrue(manager.updateAddress(1001, newAddr));
        assertEquals(newAddr, d.getAddress());
        
        assertFalse(manager.updateAddress(1001, null));
    }

    @Test
    public void testUpdateDeliveryCustomer() {
        Delivery d = new Delivery(1001, customer, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d);
        
        Customer existing = new Customer(2, "Existing", "000");
        manager.addCustomer(existing);
        
        assertTrue(manager.updateDeliveryCustomer(1001, "EXISTING"));
        assertEquals(existing, d.getCustomer());
        
        assertTrue(manager.updateDeliveryCustomer(1001, "Brand New"));
        assertEquals("Brand New", d.getCustomer().getName());
        assertEquals(3, d.getCustomer().getId());
        
        Delivery d2 = new Delivery(1002, existing, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d2);
        
        manager.updateDeliveryCustomer(1001, "Something Else");
        assertEquals("Something Else", d.getCustomer().getName());
        assertEquals("Existing", d2.getCustomer().getName());
    }

    @Test
    public void testGetNextCourierId() {
        assertEquals(2, manager.getNextCourierId());

        manager.addCourier(new Courier(10, "Ten", "101010"));
        assertEquals(11, manager.getNextCourierId());

        DeliveryManager emptyManager = new DeliveryManager();
        assertEquals(1, emptyManager.getNextCourierId());
    }

    @Test
    public void testRemoveCourierEligible() {
        Courier c2 = new Courier(2, "Courier 2", "222");
        manager.addCourier(c2);
        assertTrue(manager.removeCourier(2), "Eligible courier should be removed");
        assertNull(manager.findCourierById(2));
    }

    @Test
    public void testRemoveCourierWithInTransitDelivery() {
        Delivery d = new Delivery(1001, customer, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(d);
        manager.assignCourier(1001, 1);
        manager.updateDeliveryStatus(1001, DeliveryStatus.IN_TRANSIT);

        assertFalse(manager.removeCourier(1), "Should not remove courier with IN_TRANSIT delivery");
        assertNotNull(manager.findCourierById(1));
    }

    @Test
    public void testRemoveCourierResetsAssignedDeliveries() {
        Delivery d1 = new Delivery(1001, customer, "R1", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        Delivery d2 = new Delivery(1002, customer, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(d1);
        manager.addDelivery(d2);
        
        manager.assignCourier(1001, 1);
        manager.assignCourier(1002, 1);

        assertTrue(manager.removeCourier(1));

        assertEquals(DeliveryStatus.CREATED, d1.getStatus());
        assertNull(d1.getCourier());
        assertEquals(DeliveryStatus.CREATED, d2.getStatus());
        assertNull(d2.getCourier());
    }

    @Test
    public void testUnassignCourier() {
        Delivery delivery = new Delivery(1001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(delivery);
        manager.assignCourier(1001, 1);
        
        assertTrue(manager.unassignCourier(1001));
        assertNull(delivery.getCourier());
        assertEquals(DeliveryStatus.CREATED, delivery.getStatus());

        manager.assignCourier(1001, 1);
        manager.updateDeliveryStatus(1001, DeliveryStatus.IN_TRANSIT);
        assertFalse(manager.unassignCourier(1001));

        manager.updateDeliveryStatus(1001, DeliveryStatus.DELIVERED);
        assertFalse(manager.unassignCourier(1001));

        Delivery d2 = new Delivery(1002, customer, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d2);
        manager.updateDeliveryStatus(1002, DeliveryStatus.CANCELLED);
        assertFalse(manager.unassignCourier(1002));

        Delivery d3 = new Delivery(1003, customer, "R3", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d3);
        assertFalse(manager.unassignCourier(1003));
    }

    @Test
    public void testRemoveCourierDoesNotDeleteDeliveries() {
        Delivery d = new Delivery(1001, customer, "R", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        manager.addDelivery(d);
        manager.assignCourier(1001, 1);

        assertTrue(manager.removeCourier(1));

        assertNotNull(manager.findDeliveryById(1001), "Delivery should still exist after courier removal");
    }


    @Test
    public void testFindDeliveriesByCustomerName() {
        Customer c1 = new Customer(2, "Mark Green", "111");
        Customer c2 = new Customer(3, "Alice Brown", "222");
        manager.addCustomer(c1);
        manager.addCustomer(c2);

        manager.addDelivery(new Delivery(1001, c1, "R1", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now()));
        manager.addDelivery(new Delivery(1002, c2, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now()));
        manager.addDelivery(new Delivery(1003, c1, "R3", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now()));

        ArrayList<Delivery> found = manager.findDeliveriesByCustomerName("Mark Green");
        assertEquals(2, found.size());

        found = manager.findDeliveriesByCustomerName("mark");
        assertEquals(2, found.size());

        found = manager.findDeliveriesByCustomerName("GREEN");
        assertEquals(2, found.size());

        found = manager.findDeliveriesByCustomerName("Alice");
        assertEquals(1, found.size());
        assertEquals(1002, found.get(0).getId());

        found = manager.findDeliveriesByCustomerName("Unknown");
        assertTrue(found.isEmpty());

        found = manager.findDeliveriesByCustomerName("  ");
        assertTrue(found.isEmpty());
    }

    @Test
    public void testPersistenceIntegration() {
        DeliveryManager sourceManager = new DeliveryManager();
        Customer c = new Customer(100, "Source Customer", "123");
        Courier cr = new Courier(200, "Source Courier", "321");
        Delivery d = new Delivery(5000, c, "Recipient", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        
        sourceManager.addCustomer(c);
        sourceManager.addCourier(cr);
        sourceManager.addDelivery(d);

        DpxData data = sourceManager.getData();
        assertNotNull(data);
        assertEquals(1, data.getCustomers().size());
        assertEquals(1, data.getCouriers().size());
        assertEquals(1, data.getDeliveries().size());

        DeliveryManager targetManager = new DeliveryManager();
        targetManager.loadData(data);

        assertEquals(1, targetManager.getCustomers().size());
        assertEquals(1, targetManager.getCouriers().size());
        assertEquals(1, targetManager.getDeliveries().size());
        assertEquals("Source Customer", targetManager.getCustomers().get(0).getName());
        assertEquals(5000, targetManager.getDeliveries().get(0).getId());

        targetManager.addCustomer(new Customer(101, "New", "0"));
        assertEquals(2, targetManager.getCustomers().size());
        assertEquals(1, data.getCustomers().size());
    }

    @Test
    public void testGetNextCustomerId() {
        assertEquals(2, manager.getNextCustomerId());

        manager.addCustomer(new Customer(10, "Ten", "101010"));
        assertEquals(11, manager.getNextCustomerId());

        DeliveryManager emptyManager = new DeliveryManager();
        assertEquals(1, emptyManager.getNextCustomerId());
    }

    @Test
    public void testRestoreCancelledDelivery() {
        Delivery d = new Delivery(2001, customer, "Recipient", address, 2.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now());
        manager.addDelivery(d);
        manager.assignCourier(2001, 1);

        assertTrue(manager.updateDeliveryStatus(2001, DeliveryStatus.CANCELLED));
        assertEquals(DeliveryStatus.CANCELLED, d.getStatus());
        assertNotNull(d.getCourier());

        assertTrue(manager.updateDeliveryStatus(2001, DeliveryStatus.CREATED));
        assertEquals(DeliveryStatus.CREATED, d.getStatus());
        assertNull(d.getCourier(), "Restored delivery should have no courier");
    }

    @Test
    public void testAddCustomerNonDuplication() {
        Customer c2 = new Customer(2, "C2", "222");
        assertTrue(manager.addCustomer(c2));
        
        Customer c2_dup = new Customer(2, "C2_DUP", "333");
        assertFalse(manager.addCustomer(c2_dup), "Customer with duplicate ID should be rejected");
    }
}
