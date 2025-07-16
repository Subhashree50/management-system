package application;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    TableView<Product> table;
    TextField nameInput, quantityInput, priceInput, searchField;
    Label totalLabel;
    ObservableList<Product> products;

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage primaryStage) {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");

        Button loginButton = new Button("Login");
        loginButton.setStyle(
            "-fx-background-color: green;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5;"
        );

        VBox loginLayout = new VBox(12, userLabel, userField, passLabel, passField, loginButton);
        loginLayout.setPadding(new Insets(30));
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle("-fx-background-color: #e0ffe0;");

        Scene loginScene = new Scene(loginLayout, 320, 240);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Login");
        primaryStage.show();

        loginButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();

            if (username.equals("admin") && password.equals("sathya")) {
                showMainApp(primaryStage);
            } else {
                showAlert("Invalid username or password.");
            }
        });
    }

    private void showMainApp(Stage primaryStage) {
        nameInput = new TextField();
        nameInput.setPromptText("Name");

        quantityInput = new TextField();
        quantityInput.setPromptText("Quantity");

        priceInput = new TextField();
        priceInput.setPromptText("Price");

        totalLabel = new Label("Total Bill: ₹0.00");

        searchField = new TextField();
        searchField.setPromptText("Search by Name");

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: lightgreen; -fx-text-fill: black;");
        searchButton.setOnAction(e -> filterProducts(searchField.getText()));

        HBox searchLayout = new HBox(10, searchField, searchButton);
        searchLayout.setAlignment(Pos.CENTER_LEFT);
        searchLayout.setStyle("-fx-background-color: blue; -fx-padding: 10;");

        // Add date label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String dateTimeNow = LocalDateTime.now().format(formatter);
        Label dateTimeLabel = new Label("Date: " + dateTimeNow);
        dateTimeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");
        HBox dateBox = new HBox(dateTimeLabel);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.setPadding(new Insets(5, 10, 5, 10));

        TableColumn<Product, String> snCol = new TableColumn<>("S.No");
        snCol.setMinWidth(50);
        snCol.setCellValueFactory(cellData -> {
            int index = table.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setMinWidth(100);
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setMinWidth(100);
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Double> totalCol = new TableColumn<>("Total");
        totalCol.setMinWidth(100);
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        table = new TableView<>();
        table.setStyle("-fx-background-color: lightblue; -fx-table-cell-border-color: blue; -fx-control-inner-background:lightblue;");
        products = FXCollections.observableArrayList();
        table.setItems(products);
        table.getColumns().addAll(snCol, nameCol, quantityCol, priceCol, totalCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameInput.setText(newSel.getName());
                quantityInput.setText(String.valueOf(newSel.getQuantity()));
                priceInput.setText(String.valueOf(newSel.getPrice()));
            }
        });

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addProduct());
        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateProduct());
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteProduct());
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveBillToFile());
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> primaryStage.close());

        for (Button btn : new Button[]{addButton, updateButton, deleteButton, saveButton, exitButton}) {
            btn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        }

        HBox inputLayout = new HBox(10, nameInput, quantityInput, priceInput,
                addButton, updateButton, deleteButton, saveButton, exitButton);
        inputLayout.setAlignment(Pos.CENTER);

        HBox totalLayout = new HBox(10, totalLabel);
        totalLayout.setAlignment(Pos.CENTER_RIGHT);

        VBox mainLayout = new VBox(10, searchLayout, dateBox, table, inputLayout, totalLayout);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f0f8ff;");

        Scene scene = new Scene(mainLayout, 980, 470);
        primaryStage.setTitle("Product Billing System - 'Tata Groups Of Companies'");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addProduct() {
        try {
            String name = nameInput.getText().trim();
            int quantity = Integer.parseInt(quantityInput.getText().trim());
            double price = Double.parseDouble(priceInput.getText().trim());

            if (name.isEmpty()) {
                showAlert("Product name cannot be empty.");
                return;
            }

            for (Product p : products) {
                if (p.getName().equalsIgnoreCase(name)) {
                    showAlert("Product with the same name already exists.");
                    return;
                }
            }

            products.add(new Product(name, quantity, price));
            updateTotalBill();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid input! Quantity and Price must be numbers.");
        }
    }

    private void updateProduct() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String newName = nameInput.getText().trim();
                int newQty = Integer.parseInt(quantityInput.getText().trim());
                double newPrice = Double.parseDouble(priceInput.getText().trim());

                if (newName.isEmpty()) {
                    showAlert("Product name cannot be empty.");
                    return;
                }

                for (Product p : products) {
                    if (p != selected && p.getName().equalsIgnoreCase(newName)) {
                        showAlert("Another product with the same name already exists.");
                        return;
                    }
                }

                selected.setName(newName);
                selected.setQuantity(newQty);
                selected.setPrice(newPrice);
                table.refresh();
                updateTotalBill();
                clearFields();
            } catch (NumberFormatException e) {
                showAlert("Invalid input! Quantity and Price must be numbers.");
            }
        } else {
            showAlert("Please select a product to update.");
        }
    }

    private void deleteProduct() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            products.remove(selected);
            updateTotalBill();
            clearFields();
        } else {
            showAlert("Please select a product to delete.");
        }
    }

    private void updateTotalBill() {
        double total = 0;
        for (Product p : products) {
            total += p.getTotal();
        }
        totalLabel.setText(String.format("Total Bill: ₹%.2f", total));
    }

    private void filterProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(products);
            return;
        }

        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(p);
            }
        }
        table.setItems(filtered);
    }

    private void clearFields() {
        nameInput.clear();
        quantityInput.clear();
        priceInput.clear();
        table.getSelectionModel().clearSelection();
    }

    private void saveBillToFile() {
        try (FileWriter writer = new FileWriter("bill.txt")) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

            writer.write("======= BILL RECEIPT =======\n");
            writer.write("Date: " + now.format(formatter) + "\n\n");

            double total = 0;
            int serial = 1;

            writer.write(String.format("%-5s %-15s %-10s %-10s %-10s%n", "S.No", "Product", "Quantity", "Price", "Total"));
            writer.write("--------------------------------------------------------\n");

            for (Product p : products) {
                double lineTotal = p.getTotal();
                writer.write(String.format("%-5d %-15s %-10d %-10.2f %-10.2f%n",
                        serial++, p.getName(), p.getQuantity(), p.getPrice(), lineTotal));
                total += lineTotal;
            }

            writer.write("--------------------------------------------------------\n");
            writer.write(String.format("Grand Total: ₹%.2f%n", total));
            writer.write("============================\n");

            showAlert("Bill saved to 'bill.txt'");
        } catch (IOException e) {
            showAlert("Error saving the file.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Product {
        private String name;
        private int quantity;
        private double price;

        public Product(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return quantity * price; }

        public void setName(String name) { this.name = name; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setPrice(double price) { this.price = price; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
