package dpx.app;

import dpx.model.*;
import dpx.persistence.DataFileManager;
import dpx.service.DeliveryManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class DeliveriesView {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final DeliveryManager manager;
    private final DataFileManager fileManager;
    private final StackPane container;
    
    private VBox deliveriesListView;
    private TableView<Delivery> deliveryTable;
    private final AddDeliveryForm addDeliveryForm;

    public DeliveriesView(DeliveryManager manager, DataFileManager fileManager) {
        this.manager = manager;
        this.fileManager = fileManager;
        this.container = new StackPane();
        
        setupDeliveriesListView();
        this.addDeliveryForm = new AddDeliveryForm(manager, fileManager, 
            () -> container.getChildren().setAll(deliveriesListView),
            () -> {
                refresh();
                container.getChildren().setAll(deliveriesListView);
            }
        );

        container.getChildren().add(deliveriesListView);
    }

    public StackPane getView() {
        return container;
    }

    public void showAddDeliveryForm() {
        addDeliveryForm.resetForm();
        container.getChildren().setAll(addDeliveryForm.getView());
    }

    public void refresh() {
        if (deliveryTable != null) {
            Delivery selectedDelivery = deliveryTable.getSelectionModel().getSelectedItem();
            deliveryTable.getItems().setAll(manager.getDeliveries());
            if (selectedDelivery != null) {
                deliveryTable.getSelectionModel().select(selectedDelivery);
            }
        }
    }

    private void setupDeliveriesListView() {
        deliveriesListView = new VBox(15);
        deliveriesListView.setPadding(new Insets(0, 10, 10, 10));
        
        Label title = new Label("Deliveries");
        title.setPadding(new Insets(0, 0, 5, 0));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button addDeliveryBtn = new Button("Add Delivery");
        addDeliveryBtn.setOnAction(e -> showAddDeliveryForm());

        Label deliverySortTipLabel = new Label("Tip: Click any column heading to sort the deliveries.");
        deliverySortTipLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        deliveryTable = new TableView<>();
        deliveryTable.setEditable(true);
        deliveryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Delivery, String> idColumn = new TableColumn<>("ID");
        idColumn.setPrefWidth(65);
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        TableColumn<Delivery, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setPrefWidth(130);
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));
        customerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        customerColumn.setOnEditCommit(event -> {
            String newName = event.getNewValue();
            if (newName == null || newName.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Customer name cannot be empty.");
                refresh();
                return;
            }
            if (manager.updateDeliveryCustomer(event.getRowValue().getId(), newName)) {
                saveData();
                refresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Could not update customer.");
                refresh();
            }
        });

        TableColumn<Delivery, String> recipientColumn = new TableColumn<>("Recipient");
        recipientColumn.setPrefWidth(120);
        recipientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRecipientName()));
        recipientColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        recipientColumn.setOnEditCommit(event -> {
            String newName = event.getNewValue();
            if (newName == null || newName.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Recipient name cannot be empty.");
                refresh();
                return;
            }
            if (manager.updateRecipient(event.getRowValue().getId(), newName)) {
                saveData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Could not update recipient.");
            }
            refresh();
        });

        TableColumn<Delivery, DeliveryStatus> statusColumn = new TableColumn<>("Status");
        statusColumn.setPrefWidth(100);
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(DeliveryStatus.values()));
        statusColumn.setOnEditCommit(event -> {
            Delivery delivery = event.getRowValue();
            DeliveryStatus newStatus = event.getNewValue();

            if (newStatus == DeliveryStatus.CANCELLED) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Cancel Delivery");
                confirmAlert.setHeaderText("Are you sure you want to cancel this delivery?");
                confirmAlert.setContentText("This action will mark the delivery as cancelled.");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (!manager.updateDeliveryStatus(delivery.getId(), newStatus)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid Status Transition", "The transition from " + delivery.getStatus() + " to " + newStatus + " is not allowed.");
                    } else {
                        saveData();
                    }
                }
                refresh();
            } else {
                if (!manager.updateDeliveryStatus(delivery.getId(), newStatus)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid Status Transition", "The transition from " + delivery.getStatus() + " to " + newStatus + " is not allowed.");
                    refresh();
                } else {
                    saveData();
                }
            }
        });

        TableColumn<Delivery, DeliveryPriority> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setPrefWidth(85);
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setCellFactory(ComboBoxTableCell.forTableColumn(DeliveryPriority.values()));
        priorityColumn.setOnEditCommit(event -> {
            Delivery delivery = event.getRowValue();
            DeliveryPriority newPriority = event.getNewValue();
            if (!manager.updateDeliveryPriority(delivery.getId(), newPriority)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Priority could not be updated.");
                refresh();
            } else {
                saveData();
            }
        });

        TableColumn<Delivery, LocalDate> dateColumn = new TableColumn<>("Planned Date");
        dateColumn.setPrefWidth(115);
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("plannedDeliveryDate"));
        dateColumn.setCellFactory(column -> new TextFieldTableCell<>(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE_FORMATTER.format(date);
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    return LocalDate.parse(string, DATE_FORMATTER);
                } catch (Exception e) {
                    return null;
                }
            }
        }));
        dateColumn.setOnEditCommit(event -> {
            LocalDate newDate = event.getNewValue();
            if (newDate != null) {
                if (manager.updatePlannedDate(event.getRowValue().getId(), newDate)) {
                    saveData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid Date", "Planned date cannot be earlier than creation date (" + DATE_FORMATTER.format(event.getRowValue().getCreationDate()) + ").");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Format", "Enter the date in dd.MM.yyyy format, for example 28.11.2026.");
            }
            refresh();
        });

        TableColumn<Delivery, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setPrefWidth(260);
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress().toString()));
        addressColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        addressColumn.setOnEditCommit(event -> {
            String input = event.getNewValue();
            try {
                String[] parts = input.split(", ");
                if (parts.length != 3) {
                    throw new Exception("Invalid format. Use: Street house, City, PostalCode");
                }
                String streetAndNum = parts[0].trim();
                String city = parts[1].trim();
                String postalCode = parts[2].trim();

                int lastSpace = streetAndNum.lastIndexOf(" ");
                if (lastSpace == -1) {
                    throw new Exception("Street and house number must be separated by a space.");
                }
                String street = streetAndNum.substring(0, lastSpace).trim();
                String houseNum = streetAndNum.substring(lastSpace + 1).trim();

                if (street.isEmpty() || houseNum.isEmpty() || city.isEmpty() || postalCode.isEmpty()) {
                    throw new Exception("All address parts must be non-empty.");
                }

                Address newAddress = new Address(city, street, houseNum, postalCode);
                if (manager.updateAddress(event.getRowValue().getId(), newAddress)) {
                    saveData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", "Could not update address.");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Address", ex.getMessage());
            }
            refresh();
        });

        TableColumn<Delivery, Courier> courierColumn = new TableColumn<>("Courier");
        courierColumn.setPrefWidth(130);
        courierColumn.setCellValueFactory(new PropertyValueFactory<>("courier"));
        
        courierColumn.setCellFactory(column -> new ComboBoxTableCell<Delivery, Courier>(new StringConverter<Courier>() {
            @Override
            public String toString(Courier courier) {
                return courier == null ? "Not Assigned" : courier.getName();
            }

            @Override
            public Courier fromString(String string) {
                return null;
            }
        }) {
            @Override
            public void updateItem(Courier item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setEditable(false);
                    return;
                }

                Delivery delivery = getTableRow().getItem();
                DeliveryStatus status = delivery.getStatus();

                boolean canEdit = status == DeliveryStatus.CREATED || status == DeliveryStatus.ASSIGNED;
                setEditable(canEdit);

                if (canEdit) {
                    ArrayList<Courier> currentCouriers = new ArrayList<>();
                    currentCouriers.add(null);
                    currentCouriers.addAll(manager.getCouriers());
                    getItems().setAll(currentCouriers);
                }
            }
        });

        courierColumn.setOnEditCommit(event -> {
            Delivery delivery = event.getRowValue();
            Courier newCourier = event.getNewValue();
            boolean success;
            if (newCourier == null) {
                success = manager.unassignCourier(delivery.getId());
                if (!success) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Unassignment Failed", "A courier can only be unassigned from a delivery with ASSIGNED status.");
                } else {
                    saveData();
                }
            } else {
                success = manager.assignCourier(delivery.getId(), newCourier.getId());
                if (!success) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Assignment Failed", "Check workload limit (max 10) and delivery status.");
                } else {
                    saveData();
                }
            }
            refresh();
        });

        TableColumn<Delivery, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setSortable(false);
        actionColumn.setPrefWidth(85);
        Callback<TableColumn<Delivery, Void>, TableCell<Delivery, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Delivery, Void> call(final TableColumn<Delivery, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setOnAction(event -> {
                            Delivery delivery = getTableView().getItems().get(getIndex());
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete delivery " + delivery.getId() + "?", ButtonType.YES, ButtonType.NO);
                            confirm.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.YES) {
                                    if (manager.removeDelivery(delivery.getId())) {
                                        saveData();
                                        refresh();
                                    } else {
                                        showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", "Delivery could not be deleted.");
                                    }
                                }
                            });
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);

        deliveryTable.getColumns().addAll(idColumn, customerColumn, recipientColumn, addressColumn, statusColumn, priorityColumn, dateColumn, courierColumn, actionColumn);
        deliveryTable.getItems().addAll(manager.getDeliveries());

        deliveriesListView.getChildren().addAll(title, addDeliveryBtn, deliverySortTipLabel, deliveryTable);
    }

    private void saveData() {
        try {
            fileManager.saveData(manager.getData());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Persistence Error", "Could not save data", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
