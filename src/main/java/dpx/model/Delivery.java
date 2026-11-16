package dpx.model;

import java.time.LocalDate;

public class Delivery {

    private int id;
    private Customer customer;
    private String recipientName;
    private Address address;
    private double weight;
    private DeliveryPriority priority;
    private DeliveryStatus status;
    private Courier courier;
    private LocalDate creationDate;
    private LocalDate plannedDeliveryDate;

    public Delivery(
            int id,
            Customer customer,
            String recipientName,
            Address address,
            double weight,
            DeliveryPriority priority,
            LocalDate creationDate,
            LocalDate plannedDeliveryDate
    ) {
        this.id = id;
        this.customer = customer;
        this.recipientName = recipientName;
        this.address = address;
        this.weight = weight;
        this.priority = priority;
        this.creationDate = creationDate;
        this.plannedDeliveryDate = plannedDeliveryDate;

        this.status = DeliveryStatus.CREATED;
        this.courier = null;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public Address getAddress() {
        return address;
    }

    public double getWeight() {
        return weight;
    }

    public DeliveryPriority getPriority() {
        return priority;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Courier getCourier() {
        return courier;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getPlannedDeliveryDate() {
        return plannedDeliveryDate;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setPriority(DeliveryPriority priority) {
        this.priority = priority;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setPlannedDeliveryDate(LocalDate plannedDeliveryDate) {
        this.plannedDeliveryDate = plannedDeliveryDate;
    }

    @Override
    public String toString() {
        return "Delivery ID: " + id
                + ", Customer: " + customer.getName()
                + ", Recipient: " + recipientName
                + ", Status: " + status
                + ", Priority: " + priority;
    }
}