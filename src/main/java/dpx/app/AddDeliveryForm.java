package dpx.app;

import dpx.model.*;
import dpx.persistence.DataFileManager;
import dpx.service.DeliveryManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.UnaryOperator;

public class AddDeliveryForm {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final DeliveryManager manager;
    private final DataFileManager fileManager;
    private final VBox view;
    private final Runnable onCancel;
    private final Runnable onSuccess;

    private TextField idField;
    private TextField customerNameField;
    private TextField customerPhoneField;
    private TextField recipientField;
    private TextField cityField;
    private TextField streetField;
    private TextField houseNumField;
    private TextField postalCodeField;
    private TextField weightField;
    private ComboBox<DeliveryPriority> priorityComboBox;
    private DatePicker datePicker;

    public AddDeliveryForm(DeliveryManager manager, DataFileManager fileManager, Runnable onCancel, Runnable onSuccess) {
        this.manager = manager;
        this.fileManager = fileManager;
        this.onCancel = onCancel;
        this.onSuccess = onSuccess;
        this.view = new VBox(15);
        setupAddEditView();
    }

    public VBox getView() {
        return view;
    }

    private void setupAddEditView() {
        view.setPadding(new Insets(0, 10, 10, 10));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Add Delivery");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button backBtn = new Button("Back to List");
        backBtn.setOnAction(e -> onCancel.run());
        header.getChildren().addAll(title, backBtn);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        Label idLabel = new Label("Delivery ID:");
        GridPane.setHalignment(idLabel, HPos.RIGHT);
        form.add(idLabel, 0, 0);
        idField = new TextField();
        idField.setEditable(false);
        form.add(idField, 1, 0);

        Label customerLabel = new Label("Customer Name:");
        GridPane.setHalignment(customerLabel, HPos.RIGHT);
        form.add(customerLabel, 0, 1);
        customerNameField = new TextField();
        form.add(customerNameField, 1, 1);

        Label customerPhoneLabel = new Label("Mobile Phone:");
        GridPane.setHalignment(customerPhoneLabel, HPos.RIGHT);
        form.add(customerPhoneLabel, 0, 2);
        customerPhoneField = new TextField();

        String phonePrefix = "+371 ";
        customerPhoneField.setText(phonePrefix);
        UnaryOperator<TextFormatter.Change> phoneFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.startsWith(phonePrefix)) {
                String afterPrefix = newText.substring(phonePrefix.length());
                if (afterPrefix.length() <= 8 && afterPrefix.matches("[0-9]*")) {
                    return change;
                }
            }
            return null;
        };
        customerPhoneField.setTextFormatter(new TextFormatter<>(phoneFilter));
        form.add(customerPhoneField, 1, 2);

        Label recipientLabel = new Label("Recipient Name:");
        GridPane.setHalignment(recipientLabel, HPos.RIGHT);
        form.add(recipientLabel, 0, 3);
        recipientField = new TextField();
        form.add(recipientField, 1, 3);

        Label cityLabel = new Label("City:");
        GridPane.setHalignment(cityLabel, HPos.RIGHT);
        form.add(cityLabel, 2, 0);
        cityField = new TextField();
        form.add(cityField, 3, 0);

        Label streetLabel = new Label("Street:");
        GridPane.setHalignment(streetLabel, HPos.RIGHT);
        form.add(streetLabel, 2, 1);
        streetField = new TextField();
        form.add(streetField, 3, 1);

        Label houseNumLabel = new Label("House Number:");
        GridPane.setHalignment(houseNumLabel, HPos.RIGHT);
        form.add(houseNumLabel, 2, 2);
        houseNumField = new TextField();
        form.add(houseNumField, 3, 2);

        Label postalCodeLabel = new Label("Postal Code:");
        GridPane.setHalignment(postalCodeLabel, HPos.RIGHT);
        form.add(postalCodeLabel, 2, 3);
        postalCodeField = new TextField();

        String postalPrefix = "LV-";
        postalCodeField.setText(postalPrefix);
        UnaryOperator<TextFormatter.Change> postalFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.startsWith(postalPrefix)) {
                String afterPrefix = newText.substring(postalPrefix.length());
                if (afterPrefix.length() <= 4 && afterPrefix.matches("[0-9]*")) {
                    return change;
                }
            }
            return null;
        };
        postalCodeField.setTextFormatter(new TextFormatter<>(postalFilter));
        form.add(postalCodeField, 3, 3);

        Label weightLabel = new Label("Weight (kg):");
        GridPane.setHalignment(weightLabel, HPos.RIGHT);
        form.add(weightLabel, 0, 4);
        weightField = new TextField();
        form.add(weightField, 1, 4);

        Label priorityLabel = new Label("Priority:");
        GridPane.setHalignment(priorityLabel, HPos.RIGHT);
        form.add(priorityLabel, 2, 4);
        priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(DeliveryPriority.values());
        form.add(priorityComboBox, 3, 4);

        Label dateLabel = new Label("Planned Delivery Date:");
        GridPane.setHalignment(dateLabel, HPos.RIGHT);
        form.add(dateLabel, 0, 5);
        datePicker = new DatePicker();
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? DATE_FORMATTER.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, DATE_FORMATTER);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });
        form.add(datePicker, 1, 5);

        Button addButton = new Button("Add Delivery");
        form.add(addButton, 1, 6);

        addButton.setOnAction(e -> handleAddDelivery());

        view.getChildren().addAll(header, form);
    }

    private void handleAddDelivery() {
        clearValidationStyles();
        ArrayList<String> errors = new ArrayList<>();

        String custName = customerNameField.getText().trim();
        if (custName.isEmpty()) {
            errors.add("Customer Name is required.");
            customerNameField.setStyle("-fx-border-color: red;");
        }

        String custPhone = customerPhoneField.getText().trim();
        if (!custPhone.startsWith("+371 ") || custPhone.length() != 13 || !custPhone.substring(5).matches("\\d{8}")) {
            errors.add("Mobile Phone must contain 8 digits after +371.");
            customerPhoneField.setStyle("-fx-border-color: red;");
        }

        String recipient = recipientField.getText().trim();
        if (recipient.isEmpty()) {
            errors.add("Recipient Name is required.");
            recipientField.setStyle("-fx-border-color: red;");
        }

        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            errors.add("City is required.");
            cityField.setStyle("-fx-border-color: red;");
        }

        String street = streetField.getText().trim();
        if (street.isEmpty()) {
            errors.add("Street is required.");
            streetField.setStyle("-fx-border-color: red;");
        }

        String houseNum = houseNumField.getText().trim();
        if (houseNum.isEmpty()) {
            errors.add("House Number is required.");
            houseNumField.setStyle("-fx-border-color: red;");
        }

        String postalCode = postalCodeField.getText().trim();
        if (!postalCode.matches("LV-\\d{4}")) {
            errors.add("Postal Code must use the format LV-1234.");
            postalCodeField.setStyle("-fx-border-color: red;");
        }

        double weight = -1;
        try {
            weight = Double.parseDouble(weightField.getText().trim());
            if (weight <= 0) {
                errors.add("Weight must be greater than 0.");
                weightField.setStyle("-fx-border-color: red;");
            }
        } catch (NumberFormatException ex) {
            errors.add("Weight must be a numeric value.");
            weightField.setStyle("-fx-border-color: red;");
        }

        DeliveryPriority priority = priorityComboBox.getValue();
        if (priority == null) {
            errors.add("Priority must be selected.");
            priorityComboBox.setStyle("-fx-border-color: red;");
        }

        LocalDate plannedDate = datePicker.getValue();
        if (plannedDate == null) {
            errors.add("Planned Delivery Date is required.");
            datePicker.setStyle("-fx-border-color: red;");
        } else if (plannedDate.isBefore(LocalDate.now())) {
            errors.add("Planned Delivery Date cannot be earlier than today.");
            datePicker.setStyle("-fx-border-color: red;");
        }

        if (!errors.isEmpty()) {
            StringBuilder message = new StringBuilder("Please correct the following fields:\n\n");
            for (String error : errors) {
                message.append("- ").append(error).append("\n");
            }
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Information", message.toString());
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            Customer customer = null;
            for (Customer c : manager.getCustomers()) {
                if (c.getName().equalsIgnoreCase(custName)) {
                    customer = c;
                    break;
                }
            }

            if (customer == null) {
                customer = new Customer(manager.getNextCustomerId(), custName, custPhone);
                manager.addCustomer(customer);
            } else {
                if (!customer.getPhone().equals(custPhone)) {
                    customer.setPhone(custPhone);
                }
            }

            Address address = new Address(city, street, houseNum, postalCode);
            Delivery delivery = new Delivery(id, customer, recipient, address, weight, priority, LocalDate.now(), plannedDate);

            if (manager.addDelivery(delivery)) {
                saveData();
                onSuccess.run();
                resetForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add delivery", "Check if all fields are valid.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Unexpected Error", ex.getMessage());
        }
    }

    private void clearValidationStyles() {
        customerNameField.setStyle("");
        customerPhoneField.setStyle("");
        recipientField.setStyle("");
        cityField.setStyle("");
        streetField.setStyle("");
        houseNumField.setStyle("");
        postalCodeField.setStyle("");
        weightField.setStyle("");
        priorityComboBox.setStyle("");
        datePicker.setStyle("");
    }

    public void resetForm() {
        clearValidationStyles();
        idField.setText(String.valueOf(manager.getNextDeliveryId()));
        customerNameField.clear();
        customerPhoneField.setText("+371 ");
        recipientField.clear();
        cityField.clear();
        streetField.clear();
        houseNumField.clear();
        postalCodeField.setText("LV-");
        weightField.clear();
        priorityComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
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
