package dpx.app;

import dpx.model.Delivery;
import dpx.service.DeliveryManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SearchSortView {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private final DeliveryManager manager;
    private final VBox view;
    private final TableView<Delivery> resultTable;

    public SearchSortView(DeliveryManager manager) {
        this.manager = manager;
        this.resultTable = new TableView<>();
        this.view = createViewContent();
    }

    public VBox getView() {
        return view;
    }

    private VBox createViewContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(0, 10, 10, 10));
        
        Label title = new Label("Search & Sort");
        title.setPadding(new Insets(0, 0, 5, 0));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        TextField searchField = new TextField();
        searchField.setPromptText("Enter delivery ID or customer name");
        searchField.setPrefWidth(300);
        Button searchButton = new Button("Search");

        Label searchLabel = new Label("Search:");
        GridPane.setHalignment(searchLabel, HPos.RIGHT);
        grid.add(searchLabel, 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(searchButton, 2, 0);

        Label searchInfoLabel = new Label("Enter a delivery ID or customer name. Partial customer names are accepted.");
        searchInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        grid.add(searchInfoLabel, 1, 1, 2, 1);

        Label sortTipLabel = new Label("Tip: Click any column heading to sort the search results.");
        sortTipLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Delivery, String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(65);
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        TableColumn<Delivery, String> custCol = new TableColumn<>("Customer");
        custCol.setPrefWidth(130);
        custCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getName()));

        TableColumn<Delivery, String> recCol = new TableColumn<>("Recipient");
        recCol.setPrefWidth(120);
        recCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRecipientName()));

        TableColumn<Delivery, String> statCol = new TableColumn<>("Status");
        statCol.setPrefWidth(100);
        statCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        TableColumn<Delivery, String> prioCol = new TableColumn<>("Priority");
        prioCol.setPrefWidth(85);
        prioCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPriority().toString()));

        TableColumn<Delivery, String> courCol = new TableColumn<>("Courier");
        courCol.setPrefWidth(130);
        courCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCourier() == null ? "Not Assigned" : cellData.getValue().getCourier().getName()));

        TableColumn<Delivery, java.time.LocalDate> dateCol = new TableColumn<>("Planned Date");
        dateCol.setPrefWidth(115);
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("plannedDeliveryDate"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(item));
                }
            }
        });

        TableColumn<Delivery, String> addrCol = new TableColumn<>("Address");
        addrCol.setPrefWidth(260);
        addrCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress().toString()));

        resultTable.getColumns().addAll(idCol, custCol, recCol, addrCol, statCol, prioCol, courCol, dateCol);

        searchButton.setOnAction(e -> {
            String input = searchField.getText();
            if (input == null || input.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Empty Field", "Please enter a delivery ID or customer name to search.");
                return;
            }

            String searchText = input.trim();
            boolean isNumeric = true;
            for (int i = 0; i < searchText.length(); i++) {
                if (!Character.isDigit(searchText.charAt(i))) {
                    isNumeric = false;
                    break;
                }
            }

            if (isNumeric) {
                try {
                    int id = Integer.parseInt(searchText);
                    Delivery found = manager.findDeliveryById(id);
                    if (found != null) {
                        resultTable.getItems().setAll(found);
                    } else {
                        resultTable.getItems().clear();
                        showAlert(Alert.AlertType.INFORMATION, "Search Result", null, "No delivery found with ID: " + id);
                    }
                } catch (NumberFormatException ex) {
                    resultTable.getItems().clear();
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", "The numeric ID is too large.");
                }
            } else {
                ArrayList<Delivery> found = manager.findDeliveriesByCustomerName(searchText);
                if (found != null && !found.isEmpty()) {
                    resultTable.getItems().setAll(found);
                } else {
                    resultTable.getItems().clear();
                    showAlert(Alert.AlertType.INFORMATION, "Search Result", null, "No deliveries found for customer matching: " + searchText);
                }
            }
        });

        searchField.setOnAction(e -> searchButton.fire());

        content.getChildren().addAll(grid, sortTipLabel, resultTable);
        VBox.setVgrow(resultTable, Priority.ALWAYS);
        container.getChildren().addAll(title, content);
        
        return container;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
