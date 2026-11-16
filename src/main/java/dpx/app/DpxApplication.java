package dpx.app;

import dpx.persistence.DataFileManager;
import dpx.persistence.DpxData;
import dpx.service.DeliveryManager;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DpxApplication extends Application {

    private DeliveryManager manager;
    private DataFileManager fileManager;
    private BorderPane root;

    private DeliveriesView deliveriesView;
    private SearchSortView searchSortView;
    private CourierManagementView courierManagementView;

    @Override
    public void start(Stage stage) {

        manager = new DeliveryManager();
        fileManager = new DataFileManager();

        if (fileManager.dataFileExists()) {
            try {
                DpxData data = fileManager.loadData();
                manager.loadData(data);
            } catch (Exception e) {
                SampleData.create(manager);
                fileManager.saveData(manager.getData());
            }
        } else {
            SampleData.create(manager);
            fileManager.saveData(manager.getData());
        }

        root = new BorderPane();
        root.setPadding(new Insets(20));

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(0, 20, 0, 0));
        sidebar.setPrefWidth(220);

        Button deliveriesNav = new Button("Deliveries");
        Button courierNav = new Button("Courier Management");
        Button searchSortNav = new Button("Search & Sort");

        deliveriesNav.setMaxWidth(Double.MAX_VALUE);
        courierNav.setMaxWidth(Double.MAX_VALUE);
        searchSortNav.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(deliveriesNav, courierNav, searchSortNav);
        root.setLeft(sidebar);

        deliveriesView = new DeliveriesView(manager, fileManager);
        searchSortView = new SearchSortView(manager);
        courierManagementView = new CourierManagementView(manager, fileManager);

        deliveriesNav.setOnAction(e -> {
            deliveriesView.refresh();
            root.setCenter(deliveriesView.getView());
        });
        courierNav.setOnAction(e -> {
            courierManagementView.refresh();
            root.setCenter(courierManagementView.getView());
        });
        searchSortNav.setOnAction(e -> root.setCenter(searchSortView.getView()));

        deliveriesView.refresh();
        root.setCenter(deliveriesView.getView());

        Scene scene = new Scene(root, 1280, 680);

        stage.setTitle("DPX Courier Delivery System");
        stage.setScene(scene);
        stage.show();
    }

}