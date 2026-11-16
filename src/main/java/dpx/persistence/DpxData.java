package dpx.persistence;

import dpx.model.Courier;
import dpx.model.Customer;
import dpx.model.Delivery;
import java.util.List;

public class DpxData {
    private List<Customer> customers;
    private List<Courier> couriers;
    private List<Delivery> deliveries;

    public DpxData() {
    }

    public DpxData(List<Customer> customers, List<Courier> couriers, List<Delivery> deliveries) {
        this.customers = customers;
        this.couriers = couriers;
        this.deliveries = deliveries;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Courier> getCouriers() {
        return couriers;
    }

    public void setCouriers(List<Courier> couriers) {
        this.couriers = couriers;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }
}
