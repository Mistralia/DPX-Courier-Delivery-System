package dpx.app;

import dpx.model.Courier;
import dpx.model.Delivery;
import dpx.model.DeliveryPriority;
import dpx.model.DeliveryStatus;
import dpx.persistence.DataFileManager;
import dpx.service.DeliveryManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CourierManagementView {

    private final DeliveryManager manager;
    private final DataFileManager fileManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private VBox mainView;
    private final AddCourierForm addCourierForm;
    private StackPane viewStack;

    private TableView<Courier> courierTable;
    private TableView<Delivery> assignedDeliveriesTable;
    private ComboBox<Delivery> unassignedDeliveryComboBox;
    private Courier selectedCourier;

    public CourierManagementView(DeliveryManager manager, DataFileManager fileManager) {
        this.manager = manager;
        this.fileManager = fileManager;
        this.addCourierForm = new AddCourierForm(manager, fileManager,
                () -> viewStack.getChildren().setAll(mainView),
                () -> {
                    refresh();
                    viewStack.getChildren().setAll(mainView);
                }
        );
        initViews();
        refresh();
    }

    private void initViews() {
        setupCourierDashboard();
        viewStack = new StackPane(mainView);
        viewStack.setAlignment(Pos.TOP_LEFT);
    }

    private void setupCourierDashboard() {
        mainView = new VBox(15);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(0, 10, 10, 10));

        Label title = new Label("Courier Management Dashboard");
        title.setPadding(new Insets(0, 0, 5, 0));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button addCourierBtn = new Button("Add Courier");
        addCourierBtn.setOnAction(e -> {
            showAddCourierForm();
        });

        courierTable = new TableView<>();
        courierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        courierTable.setPrefHeight(200);

        TableColumn<Courier, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Courier, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Courier, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Courier, Integer> activeCol = new TableColumn<>("Active Deliveries");
        activeCol.setCellValueFactory(cellData -> {
            int active = manager.countActiveDeliveriesForCourier(cellData.getValue().getId());
            return new SimpleIntegerProperty(active).asObject();
        });
        activeCol.setCellFactory(col -> new TableCell<Courier, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " / 10");
                }
            }
        });

        TableColumn<Courier, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setSortable(false);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Courier courier = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete courier " + courier.getName() + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            if (manager.removeCourier(courier.getId())) {
                                saveData();
                                selectedCourier = null;
                                refresh();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Deletion Failed", "Cannot delete courier with IN_TRANSIT deliveries.");
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        courierTable.getColumns().addAll(idCol, nameCol, phoneCol, activeCol, actionCol);

        assignedDeliveriesTable = new TableView<>();
        assignedDeliveriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        assignedDeliveriesTable.setPrefHeight(200);

        TableColumn<Delivery, String> delIdCol = new TableColumn<>("Delivery ID");
        delIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Delivery, String> delRecCol = new TableColumn<>("Recipient");
        delRecCol.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
        TableColumn<Delivery, DeliveryStatus> delStatCol = new TableColumn<>("Status");
        delStatCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Delivery, DeliveryPriority> delPrioCol = new TableColumn<>("Priority");
        delPrioCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Delivery, LocalDate> delDateCol = new TableColumn<>("Planned Date");
        delDateCol.setCellValueFactory(new PropertyValueFactory<>("plannedDeliveryDate"));
        delDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(item));
                }
            }
        });

        TableColumn<Delivery, Void> delActionCol = new TableColumn<>("Actions");
        delActionCol.setSortable(false);
        delActionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    Delivery delivery = getTableView().getItems().get(getIndex());
                    if (manager.unassignCourier(delivery.getId())) {
                        saveData();
                        refresh();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Unassignment Failed", "Cannot unassign IN_TRANSIT delivery.");
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Delivery delivery = getTableView().getItems().get(getIndex());
                    if (delivery.getStatus() == DeliveryStatus.ASSIGNED) {
                        setGraphic(removeBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        assignedDeliveriesTable.getColumns().addAll(delIdCol, delRecCol, delStatCol, delPrioCol, delDateCol, delActionCol);

        courierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedCourier = newSelection;
            refreshSelectedCourierDetails();
        });

        HBox assignControls = new HBox(10);
        assignControls.setPadding(new Insets(10, 0, 0, 0));
        unassignedDeliveryComboBox = new ComboBox<>();
        setupDeliveryComboBoxConverter(unassignedDeliveryComboBox);
        unassignedDeliveryComboBox.setPromptText("Select unassigned delivery");
        unassignedDeliveryComboBox.setPrefWidth(300);

        Button assignBtn = new Button("Assign Delivery");
        assignBtn.setOnAction(e -> {
            Courier courier = courierTable.getSelectionModel().getSelectedItem();
            Delivery delivery = unassignedDeliveryComboBox.getValue();
            if (courier == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "No Selection", "Please select a courier first.");
                return;
            }
            if (delivery == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "No Selection", "Please select a delivery to assign.");
                return;
            }
            if (manager.assignCourier(delivery.getId(), courier.getId())) {
                saveData();
                refresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Assignment Failed", "Courier workload limit reached. A courier can have a maximum of 10 active deliveries.");
            }
        });

        assignControls.getChildren().addAll(unassignedDeliveryComboBox, assignBtn);

        mainView.getChildren().addAll(title, addCourierBtn, new Label("Couriers:"), courierTable, new Label("Assigned Deliveries:"), assignedDeliveriesTable, assignControls);
    }

    private void showAddCourierForm() {
        addCourierForm.resetForm();
        viewStack.getChildren().setAll(addCourierForm.getView());
    }

    public StackPane getView() {
        return viewStack;
    }

    public void refresh() {
        if (courierTable != null) {
            Courier selected = courierTable.getSelectionModel().getSelectedItem();
            courierTable.getItems().setAll(manager.getCouriers());

            courierTable.sort();

            if (selected != null) {
                for (Courier c : courierTable.getItems()) {
                    if (c.getId() == selected.getId()) {
                        courierTable.getSelectionModel().select(c);
                        break;
                    }
                }
            }
            refreshSelectedCourierDetails();
        }
    }

    private void refreshSelectedCourierDetails() {
        if (selectedCourier != null) {
            ArrayList<Delivery> assigned = new ArrayList<>();
            for (Delivery d : manager.getDeliveries()) {
                if (d.getCourier() != null && d.getCourier().getId() == selectedCourier.getId()) {
                    if (d.getStatus() == DeliveryStatus.ASSIGNED || d.getStatus() == DeliveryStatus.IN_TRANSIT) {
                        assigned.add(d);
                    }
                }
            }
            assignedDeliveriesTable.getItems().setAll(assigned);
        } else {
            assignedDeliveriesTable.getItems().clear();
        }

        ArrayList<Delivery> unassigned = new ArrayList<>();
        for (Delivery d : manager.getDeliveries()) {
            if (d.getCourier() == null && d.getStatus() == DeliveryStatus.CREATED) {
                unassigned.add(d);
            }
        }
        unassignedDeliveryComboBox.getItems().setAll(unassigned);
    }

    private void setupDeliveryComboBoxConverter(ComboBox<Delivery> comboBox) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Delivery delivery) {
                return delivery == null ? "" : delivery.getId() + " - " + delivery.getRecipientName();
            }

            @Override
            public Delivery fromString(String string) {
                return null;
            }
        });
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
