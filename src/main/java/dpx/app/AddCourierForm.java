package dpx.app;

import dpx.model.Courier;
import dpx.persistence.DataFileManager;
import dpx.service.DeliveryManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.UnaryOperator;

public class AddCourierForm {

    private final DeliveryManager manager;
    private final DataFileManager fileManager;
    private final Runnable onCancel;
    private final Runnable onSuccess;

    private final VBox view;
    private TextField courierIdField;
    private TextField courierNameField;
    private TextField courierPhoneField;

    public AddCourierForm(DeliveryManager manager, DataFileManager fileManager, Runnable onCancel, Runnable onSuccess) {
        this.manager = manager;
        this.fileManager = fileManager;
        this.onCancel = onCancel;
        this.onSuccess = onSuccess;
        this.view = setupView();
    }

    private VBox setupView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        Label title = new Label("Add New Courier");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        courierIdField = new TextField();
        courierIdField.setEditable(false);
        courierNameField = new TextField();
        courierPhoneField = new TextField();

        String prefix = "+371 ";
        courierPhoneField.setText(prefix);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.startsWith(prefix)) {
                String afterPrefix = newText.substring(prefix.length());
                if (afterPrefix.length() <= 8 && afterPrefix.matches("[0-9]*")) {
                    return change;
                }
            }
            return null;
        };
        courierPhoneField.setTextFormatter(new TextFormatter<>(filter));

        grid.add(new Label("Courier ID:"), 0, 0);
        grid.add(courierIdField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(courierNameField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(courierPhoneField, 1, 2);

        Button saveBtn = new Button("Add Courier");
        saveBtn.setOnAction(e -> handleAddCourier());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> onCancel.run());

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        grid.add(buttons, 1, 3);

        container.getChildren().addAll(title, grid);
        return container;
    }

    private void handleAddCourier() {
        try {
            int id = Integer.parseInt(courierIdField.getText());
            String name = courierNameField.getText();
            String phone = courierPhoneField.getText();

            String latvianPrefix = "+371 ";
            if (name.isEmpty() || phone.isEmpty() || phone.equals(latvianPrefix)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Name and Phone cannot be empty.");
                return;
            }

            String digitsAfterPrefix = phone.substring(latvianPrefix.length());
            if (digitsAfterPrefix.length() != 8) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Enter a valid Latvian phone number in the format +371 29123456.");
                return;
            }

            if (manager.addCourier(new Courier(id, name, phone))) {
                saveData();
                onSuccess.run();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed", "Could not add courier.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "Please check your input.");
        }
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

    public VBox getView() {
        return view;
    }

    public void resetForm() {
        courierIdField.setText(String.valueOf(manager.getNextCourierId()));
        courierNameField.clear();
        courierPhoneField.setText("+371 ");
    }
}
