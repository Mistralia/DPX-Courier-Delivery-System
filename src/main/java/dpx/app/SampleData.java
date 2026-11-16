package dpx.app;

import dpx.model.*;
import dpx.service.DeliveryManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SampleData {

    public static void create(DeliveryManager manager) {
        String[] courierNames = {
                "Jānis Liepiņš", "Mārtiņš Kalniņš", "Andris Bērziņš", "Edgars Ozols", "Kristaps Jansons",
                "Rihards Krūmiņš", "Artūrs Vītols", "Māris Zariņš", "Laura Liepa", "Anna Kalniņa",
                "Elīna Ozoliņa", "Līga Bērziņa", "Kristīne Jansone", "Ilze Vītola", "Dace Krūmiņa"
        };
        for (int i = 0; i < courierNames.length; i++) {
            manager.addCourier(new Courier(i + 1, courierNames[i], "+371 291111" + (10 + i)));
        }

        String[] customerNames = {
                "SIA Baltic Cargo", "SIA Rīgas Birojs", "SIA Ziemeļu Tirdzniecība", "SIA Green Market", "LV Logistics",
                "SIA Daugava Solutions", "SIA Amber Trade", "Jānis Bērziņš", "Laura Kalniņa", "Mārtiņš Ozoliņš"
        };
        for (int i = 0; i < customerNames.length; i++) {
            manager.addCustomer(new Customer(i + 1, customerNames[i], "+371 280000" + (10 + i)));
        }

        String[] addressPool = {
                "Brīvības iela 55, Rīga, LV-1010",
                "Dzirnavu iela 67, Rīga, LV-1011",
                "Krišjāņa Barona iela 25, Rīga, LV-1011",
                "Mūkusalas iela 71, Rīga, LV-1004",
                "Skanstes iela 50, Rīga, LV-1013",
                "Lielirbes iela 29, Rīga, LV-1046",
                "Vienības gatve 109, Rīga, LV-1058",
                "Jomas iela 46, Jūrmala, LV-2015",
                "Rīgas iela 10, Jelgava, LV-3001",
                "Lielā iela 13, Liepāja, LV-3401",
                "Rīgas iela 9, Daugavpils, LV-5401",
                "Rīgas iela 22, Valmiera, LV-4201",
                "Pils iela 12, Cēsis, LV-4101",
                "Atbrīvošanas aleja 95, Rēzekne, LV-4601",
                "Lielā iela 2, Ventspils, LV-3601"
        };

        Address[] customerAddresses = new Address[customerNames.length];
        String[] recipientNames = {
                "Agnese Bērziņa", "Mārtiņš Liepiņš", "Laura Ozoliņa", "Edgars Kalniņš", "Kristīne Jansone",
                "Rihards Vītols", "Ilze Krūmiņa", "Jānis Bērziņš", "Laura Kalniņa", "Mārtiņš Ozoliņš"
        };
        for (int i = 0; i < customerNames.length; i++) {
            String addrStr = addressPool[i % addressPool.length];
            String[] parts = addrStr.split(", ");
            String streetAndNum = parts[0];
            String city = parts[1];
            String postalCode = parts[2];

            int lastSpace = streetAndNum.lastIndexOf(" ");
            String street = streetAndNum.substring(0, lastSpace);
            String houseNum = streetAndNum.substring(lastSpace + 1);

            customerAddresses[i] = new Address(city, street, houseNum, postalCode);
        }

        DeliveryPriority[] priorities = DeliveryPriority.values();
        
        ArrayList<Customer> customers = manager.getCustomers();
        ArrayList<Courier> couriers = manager.getCouriers();
        Random random = new Random(42);

        for (int i = 0; i < 100; i++) {
            int id = 1001 + i;
            int customerIdx = i % customers.size();
            Customer customer = customers.get(customerIdx);
            String recipient = recipientNames[customerIdx];

            Address address = customerAddresses[customerIdx];

            double weight = 1.0 + (i % 10);
            DeliveryPriority priority = priorities[i % priorities.length];

            LocalDate plannedDate = LocalDate.of(2026, 11, 24).plusDays(random.nextInt(7));
            LocalDate createdDate = plannedDate.minusDays(2);

            Delivery delivery = new Delivery(id, customer, recipient, address, weight, priority, createdDate, plannedDate);
            manager.addDelivery(delivery);
        }
        
        ArrayList<Delivery> allDeliveries = manager.getDeliveries();
        ArrayList<Delivery> deliveriesToAssign = new ArrayList<>(allDeliveries);
        Collections.shuffle(deliveriesToAssign, new Random(42));
        
        int deliveryIdx = 0;

        for (int j = 0; j < 10; j++) {
            manager.assignCourier(deliveriesToAssign.get(deliveryIdx++).getId(), couriers.get(0).getId());
        }

        for (int j = 0; j < 7; j++) {
            manager.assignCourier(deliveriesToAssign.get(deliveryIdx++).getId(), couriers.get(1).getId());
        }

        for (int j = 0; j < 5; j++) {
            manager.assignCourier(deliveriesToAssign.get(deliveryIdx++).getId(), couriers.get(2).getId());
        }

        for (int i = 3; i < 13; i++) {
            int count = (i % 2 == 0) ? 3 : 2;
            for (int j = 0; j < count; j++) {
                manager.assignCourier(deliveriesToAssign.get(deliveryIdx++).getId(), couriers.get(i).getId());
            }
        }

        for (int i = 0; i < deliveryIdx; i += 4) {
            manager.updateDeliveryStatus(deliveriesToAssign.get(i).getId(), DeliveryStatus.IN_TRANSIT);
        }
    }
}
