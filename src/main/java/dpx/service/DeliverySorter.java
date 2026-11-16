package dpx.service;

import dpx.model.Delivery;
import dpx.model.DeliveryPriority;

import java.util.ArrayList;

public class DeliverySorter {

    public void sortByDate(ArrayList<Delivery> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).getPlannedDeliveryDate().isAfter(list.get(j + 1).getPlannedDeliveryDate())) {
                    Delivery temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    public void sortByPriority(ArrayList<Delivery> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (getPriorityValue(list.get(j).getPriority()) < getPriorityValue(list.get(j + 1).getPriority())) {
                    Delivery temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    private int getPriorityValue(DeliveryPriority priority) {
        switch (priority) {
            case LOW:
                return 1;
            case NORMAL:
                return 2;
            case HIGH:
                return 3;
            case URGENT:
                return 4;
            default:
                return 0;
        }
    }
}
