package dpx.service;

import dpx.model.Courier;
import dpx.model.Customer;
import dpx.model.Delivery;
import dpx.model.DeliveryStatus;
import dpx.model.DeliveryPriority;
import dpx.model.Address;
import dpx.persistence.DpxData;

import java.util.ArrayList;
import java.time.LocalDate;

public class DeliveryManager {

    private ArrayList<Delivery> deliveries;
    private ArrayList<Customer> customers;
    private ArrayList<Courier> couriers;
    private final DeliveryValidator deliveryValidator;

    public DeliveryManager() {
        deliveries = new ArrayList<>();
        customers = new ArrayList<>();
        couriers = new ArrayList<>();
        deliveryValidator = new DeliveryValidator();
    }

    public void loadData(DpxData data) {
        if (data == null) {
            return;
        }

        if (data.getCustomers() != null) {
            this.customers = new ArrayList<>(data.getCustomers());
        } else {
            this.customers = new ArrayList<>();
        }

        if (data.getCouriers() != null) {
            this.couriers = new ArrayList<>(data.getCouriers());
        } else {
            this.couriers = new ArrayList<>();
        }

        if (data.getDeliveries() != null) {
            this.deliveries = new ArrayList<>(data.getDeliveries());
        } else {
            this.deliveries = new ArrayList<>();
        }
    }

    public DpxData getData() {
        return new DpxData(
                new ArrayList<>(customers),
                new ArrayList<>(couriers),
                new ArrayList<>(deliveries)
        );
    }

    public boolean addDelivery(Delivery delivery) {
        if (!isValidDelivery(delivery)) {
            return false;
        }

        if (findDeliveryById(delivery.getId()) != null) {
            return false;
        }

        deliveries.add(delivery);
        return true;
    }

    public boolean addCustomer(Customer customer) {
        if (customer == null) {
            return false;
        }

        if (customer.getName() == null
                || customer.getName().isBlank()) {
            return false;
        }

        if (findCustomerById(customer.getId()) != null) {
            return false;
        }

        customers.add(customer);
        return true;
    }

    public boolean addCourier(Courier courier) {
        if (courier == null) {
            return false;
        }

        if (courier.getName() == null
                || courier.getName().isBlank()) {
            return false;
        }

        if (findCourierById(courier.getId()) != null) {
            return false;
        }

        couriers.add(courier);
        return true;
    }

    public ArrayList<Delivery> getDeliveries() {
        return deliveries;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public ArrayList<Courier> getCouriers() {
        return couriers;
    }

    public Delivery findDeliveryById(int id) {
        for (Delivery delivery : deliveries) {
            if (delivery.getId() == id) {
                return delivery;
            }
        }

        return null;
    }

    public int getNextDeliveryId() {
        int maxId = 1000;
        for (Delivery delivery : deliveries) {
            if (delivery.getId() > maxId) {
                maxId = delivery.getId();
            }
        }
        return maxId + 1;
    }

    public Customer findCustomerById(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return customer;
            }
        }

        return null;
    }

    public Courier findCourierById(int id) {
        for (Courier courier : couriers) {
            if (courier.getId() == id) {
                return courier;
            }
        }

        return null;
    }

    public int getNextCourierId() {
        int maxId = 0;
        for (Courier courier : couriers) {
            if (courier.getId() > maxId) {
                maxId = courier.getId();
            }
        }
        return (maxId == 0) ? 1 : maxId + 1;
    }

    public ArrayList<Delivery> findDeliveriesByCustomer(int customerId) {
        ArrayList<Delivery> customerDeliveries = new ArrayList<>();

        for (Delivery delivery : deliveries) {
            if (delivery.getCustomer().getId() == customerId) {
                customerDeliveries.add(delivery);
            }
        }

        return customerDeliveries;
    }

    public boolean assignCourier(int deliveryId, int courierId) {
        Delivery delivery = findDeliveryById(deliveryId);
        Courier courier = findCourierById(courierId);

        if (delivery == null || courier == null) {
            return false;
        }

        if (delivery.getCourier() != null && delivery.getCourier().getId() == courierId) {
            return true;
        }

        if (delivery.getStatus() != DeliveryStatus.CREATED && delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            return false;
        }

        if (countActiveDeliveriesForCourier(courierId) >= 10) {
            return false;
        }

        delivery.setCourier(courier);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        return true;
    }

    public boolean updateDeliveryStatus(int deliveryId, DeliveryStatus newStatus) {
        Delivery delivery = findDeliveryById(deliveryId);

        if (delivery == null) {
            return false;
        }

        if (!deliveryValidator.isValidStatusTransition(delivery.getStatus(), newStatus)) {
            return false;
        }

        if ((newStatus == DeliveryStatus.ASSIGNED || newStatus == DeliveryStatus.IN_TRANSIT)
                && delivery.getCourier() == null) {
            return false;
        }

        if (newStatus == DeliveryStatus.CREATED) {
            delivery.setCourier(null);
        }

        delivery.setStatus(newStatus);
        return true;
    }

    public boolean updateDeliveryPriority(int deliveryId, DeliveryPriority newPriority) {
        Delivery delivery = findDeliveryById(deliveryId);
        if (delivery == null || newPriority == null) {
            return false;
        }
        delivery.setPriority(newPriority);
        return true;
    }

    public boolean updateRecipient(int deliveryId, String recipientName) {
        if (recipientName == null || recipientName.trim().isEmpty()) {
            return false;
        }
        Delivery delivery = findDeliveryById(deliveryId);
        if (delivery != null) {
            delivery.setRecipientName(recipientName.trim());
            return true;
        }
        return false;
    }

    public boolean updatePlannedDate(int deliveryId, LocalDate plannedDate) {
        Delivery delivery = findDeliveryById(deliveryId);
        if (delivery != null && plannedDate != null) {
            if (plannedDate.isBefore(delivery.getCreationDate())) {
                return false;
            }
            delivery.setPlannedDeliveryDate(plannedDate);
            return true;
        }
        return false;
    }

    public boolean updateAddress(int deliveryId, Address address) {
        if (address == null) {
            return false;
        }
        Delivery delivery = findDeliveryById(deliveryId);
        if (delivery != null) {
            delivery.setAddress(address);
            return true;
        }
        return false;
    }

    public boolean updateDeliveryCustomer(int deliveryId, String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return false;
        }
        String trimmedName = customerName.trim();
        Delivery delivery = findDeliveryById(deliveryId);
        if (delivery == null) {
            return false;
        }

        Customer existingCustomer = null;
        for (Customer c : getCustomers()) {
            if (c.getName().equalsIgnoreCase(trimmedName)) {
                existingCustomer = c;
                break;
            }
        }

        if (existingCustomer != null) {
            delivery.setCustomer(existingCustomer);
        } else {
            Customer newCustomer = new Customer(
                    getNextCustomerId(),
                    trimmedName,
                    ""
            );
            addCustomer(newCustomer);
            delivery.setCustomer(newCustomer);
        }
        return true;
    }

    public boolean unassignCourier(int deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        if (delivery == null) {
            return false;
        }

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            return false;
        }

        delivery.setCourier(null);
        delivery.setStatus(DeliveryStatus.CREATED);

        return true;
    }

    public boolean removeDelivery(int id) {
        Delivery delivery = findDeliveryById(id);

        if (delivery != null) {
            deliveries.remove(delivery);
            return true;
        }

        return false;
    }

    public int getNextCustomerId() {
        int maxId = 0;
        for (Customer customer : customers) {
            if (customer.getId() > maxId) {
                maxId = customer.getId();
            }
        }
        return (maxId == 0) ? 1 : maxId + 1;
    }

    public boolean removeCourier(int courierId) {
        Courier courier = findCourierById(courierId);
        if (courier == null) {
            return false;
        }

        for (Delivery delivery : deliveries) {
            if (delivery.getCourier() != null && delivery.getCourier().getId() == courierId) {
                if (delivery.getStatus() == DeliveryStatus.IN_TRANSIT) {
                    return false;
                }
            }
        }

        for (Delivery delivery : deliveries) {
            if (delivery.getCourier() != null && delivery.getCourier().getId() == courierId) {
                delivery.setCourier(null);
                if (delivery.getStatus() == DeliveryStatus.ASSIGNED) {
                    delivery.setStatus(DeliveryStatus.CREATED);
                }
            }
        }

        couriers.remove(courier);
        return true;
    }

    public ArrayList<Delivery> findDeliveriesByCustomerName(String customerName) {
        ArrayList<Delivery> result = new ArrayList<>();
        if (customerName == null || customerName.trim().isEmpty()) {
            return result;
        }
        String lowerSearch = customerName.trim().toLowerCase();
        for (Delivery d : deliveries) {
            if (d.getCustomer() != null && d.getCustomer().getName() != null) {
                if (d.getCustomer().getName().toLowerCase().contains(lowerSearch)) {
                    result.add(d);
                }
            }
        }
        return result;
    }

    public int countActiveDeliveriesForCourier(int courierId) {
        int count = 0;
        for (Delivery delivery : deliveries) {
            if (delivery.getCourier() != null
                    && delivery.getCourier().getId() == courierId
                    && (delivery.getStatus() == DeliveryStatus.ASSIGNED
                    || delivery.getStatus() == DeliveryStatus.IN_TRANSIT)) {
                count++;
            }
        }
        return count;
    }

    public boolean isValidDelivery(Delivery delivery) {
        return deliveryValidator.isValidDelivery(delivery);
    }
}