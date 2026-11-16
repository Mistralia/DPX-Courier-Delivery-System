package dpx.service;

import dpx.model.Address;
import dpx.model.Customer;
import dpx.model.Delivery;
import dpx.model.DeliveryPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeliverySorterTest {

    private DeliverySorter sorter;
    private Customer customer;
    private Address address;

    @BeforeEach
    public void setUp() {
        sorter = new DeliverySorter();
        customer = new Customer(1, "Test Customer", "12345678");
        address = new Address("City", "Street", "1", "1001");
    }

    @Test
    public void testSortByDate() {
        ArrayList<Delivery> list = new ArrayList<>();
        Delivery d1 = new Delivery(1001, customer, "R1", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(3));
        Delivery d2 = new Delivery(1002, customer, "R2", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        Delivery d3 = new Delivery(1003, customer, "R3", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(2));
        list.add(d1);
        list.add(d2);
        list.add(d3);
        
        sorter.sortByDate(list);
        
        assertEquals(1002, list.get(0).getId());
        assertEquals(1003, list.get(1).getId());
        assertEquals(1001, list.get(2).getId());
    }

    @Test
    public void testSortByPriority() {
        ArrayList<Delivery> list = new ArrayList<>();
        Delivery d1 = new Delivery(1001, customer, "R1", address, 1.0, DeliveryPriority.LOW, LocalDate.now(), LocalDate.now().plusDays(1));
        Delivery d2 = new Delivery(1002, customer, "R2", address, 1.0, DeliveryPriority.URGENT, LocalDate.now(), LocalDate.now().plusDays(1));
        Delivery d3 = new Delivery(1003, customer, "R3", address, 1.0, DeliveryPriority.HIGH, LocalDate.now(), LocalDate.now().plusDays(1));
        Delivery d4 = new Delivery(1004, customer, "R4", address, 1.0, DeliveryPriority.NORMAL, LocalDate.now(), LocalDate.now().plusDays(1));
        list.add(d1);
        list.add(d2);
        list.add(d3);
        list.add(d4);
        
        sorter.sortByPriority(list);
        
        assertEquals(DeliveryPriority.URGENT, list.get(0).getPriority());
        assertEquals(DeliveryPriority.HIGH, list.get(1).getPriority());
        assertEquals(DeliveryPriority.NORMAL, list.get(2).getPriority());
        assertEquals(DeliveryPriority.LOW, list.get(3).getPriority());
    }
}
