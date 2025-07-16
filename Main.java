package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {

    private final ObservableList<UserRecord> userRecords = FXCollections.observableArrayList();
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void start(Stage stage) {
        Scene homeScene = buildHomeScene(stage);
        Scene userScene = buildUserScene(stage, homeScene);
        Scene adminScene = buildAdminScene(stage, homeScene);

        ((Button) homeScene.lookup("#userBtn")).setOnAction(e -> stage.setScene(userScene));
        ((Button) homeScene.lookup("#adminBtn")).setOnAction(e -> {
            stage.setScene(adminScene);
            checkOverdue(stage);
        });

        stage.setScene(homeScene);
        stage.setTitle("Library Management System - 'Saraswathi Library' ");
        stage.show();
    }

    private Scene buildHomeScene(Stage stage) {
        Button userBtn = createStyledButton("User");
        userBtn.setId("userBtn");
        userBtn.setPrefWidth(200);

        Button adminBtn = createStyledButton("Administrator");
        adminBtn.setId("adminBtn");
        adminBtn.setPrefWidth(200);

        VBox box = new VBox(20, userBtn, adminBtn);
        box.setStyle("-fx-alignment: center");
        box.setPadding(new Insets(50));

        return new Scene(box, 500, 300);
    }

    private Scene buildUserScene(Stage stage, Scene back) {
        Label dateTimeLabel = createDateTimeLabel();
        HBox dateBox = new HBox(dateTimeLabel);
        dateBox.setPadding(new Insets(5));
        dateBox.setStyle("-fx-alignment: top-right;");

        TextField nameField = new TextField();
        nameField.setPromptText("User Name");
        TextField idField = new TextField();
        idField.setPromptText("User ID");
        TextField bookName = new TextField();
        bookName.setPromptText("Book Name");
        TextField bookId = new TextField();
        bookId.setPromptText("Book ID");
        DatePicker startDate = new DatePicker();
        startDate.setPromptText("Start Date");
        Label expiryLabel = new Label("Expiry: —");

        startDate.valueProperty().addListener((obs, o, n) -> {
            if (n != null) expiryLabel.setText("Expiry: " + n.plusMonths(1).format(DF));
            else expiryLabel.setText("Expiry: —");
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Enter keyword...");
        Button searchBtn = createStyledButton("Search");
        Button resetBtn = createStyledButton("Reset");

        FilteredList<UserRecord> filtered = new FilteredList<>(userRecords, b -> true);

        searchBtn.setOnAction(e -> {
            String term = searchField.getText().toLowerCase();
            filtered.setPredicate(r -> r.getName().toLowerCase().contains(term) ||
                    r.getUserId().toLowerCase().contains(term) ||
                    r.getBookName().toLowerCase().contains(term) ||
                    r.getBookId().toLowerCase().contains(term));
        });

        resetBtn.setOnAction(e -> {
            filtered.setPredicate(r -> true);
            searchField.clear();
        });

        TableView<UserRecord> table = new TableView<>(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                createColumn("Name", "name"),
                createColumn("User ID", "userId"),
                createColumn("Book Name", "bookName"),
                createColumn("Book ID", "bookId"),
                dateColumn("Start Date", UserRecord::getStart),
                dateColumn("Expiry Date", UserRecord::getExpiry)
        );

        Button add = createStyledButton("Add");
        Button update = createStyledButton("Update");
        Button delete = createStyledButton("Delete");
        Button exit = createStyledButton("Exit");

        HBox buttons = new HBox(10, add, update, delete, exit);
        buttons.setPadding(new Insets(10));

        Runnable clear = () -> {
            nameField.clear(); idField.clear(); bookName.clear(); bookId.clear();
            startDate.setValue(null); expiryLabel.setText("Expiry: —");
        };

        add.setOnAction(e -> {
            if (fieldsValid(nameField, idField, bookName, bookId, startDate)) {
                String newUserId = idField.getText().trim();
                boolean exists = userRecords.stream().anyMatch(u -> u.getUserId().equalsIgnoreCase(newUserId));

                if (exists) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Duplicate User ID");
                    alert.setHeaderText("User ID already exists");
                    alert.setContentText("A user with ID '" + newUserId + "' already exists. Please use a different ID.");
                    alert.showAndWait();
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter("admin_alerts.txt", true))) {
                        bw.write("Duplicate User ID: " + newUserId + " by " + nameField.getText() + " on " + LocalDate.now());
                        bw.newLine();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return;
                }

                userRecords.add(new UserRecord(
                        nameField.getText(), newUserId,
                        bookName.getText(), bookId.getText(),
                        startDate.getValue(), startDate.getValue().plusMonths(1)));
                clear.run();
            }
        });

        update.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && fieldsValid(nameField, idField, bookName, bookId, startDate)) {
                selected.setName(nameField.getText());
                selected.setUserId(idField.getText());
                selected.setBookName(bookName.getText());
                selected.setBookId(bookId.getText());
                selected.setStart(startDate.getValue());
                selected.setExpiry(startDate.getValue().plusMonths(1));
                table.refresh();
                clear.run();
            }
        });

        delete.setOnAction(e -> {
            var selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) userRecords.remove(selected);
        });

        exit.setOnAction(e -> stage.setScene(back));

        table.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                idField.setText(newVal.getUserId());
                bookName.setText(newVal.getBookName());
                bookId.setText(newVal.getBookId());
                startDate.setValue(newVal.getStart());
                expiryLabel.setText("Expiry: " + newVal.getExpiry().format(DF));
            }
        });

        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(8);
        form.addRow(0, new Label("Name:"), nameField);
        form.addRow(1, new Label("User ID:"), idField);
        form.addRow(2, new Label("Book Name:"), bookName);
        form.addRow(3, new Label("Book ID:"), bookId);
        form.addRow(4, new Label("Start Date:"), startDate);
        form.addRow(5, expiryLabel);

        HBox searchBox = new HBox(10, searchField, searchBtn, resetBtn);
        searchBox.setPadding(new Insets(5));

        VBox content = new VBox(10, searchBox, form, buttons, table);
        content.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(dateBox);
        root.setCenter(content);

        return new Scene(root, 800, 600);
    }

    private Scene buildAdminScene(Stage stage, Scene back) {
        Label dateTimeLabel = createDateTimeLabel();
        HBox dateBox = new HBox(dateTimeLabel);
        dateBox.setPadding(new Insets(5));
        dateBox.setStyle("-fx-alignment: top-right;");

        TextField titleField = new TextField(); titleField.setPromptText("Title");
        TextField authorField = new TextField(); authorField.setPromptText("Author");
        TextField bookIdField = new TextField(); bookIdField.setPromptText("Book ID");

        TableView<Book> table = new TableView<>(books);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                createColumn("Title", "title"),
                createColumn("Author", "author"),
                createColumn("Book ID", "bookId")
        );

        Button add = createStyledButton("Add Book");
        Button check = createStyledButton("Check Date");
        Button save = createStyledButton("Save to File");
        Button backBtn = createStyledButton("Back");

        add.setOnAction(e -> {
            if (!titleField.getText().isBlank() &&
                    !authorField.getText().isBlank() &&
                    !bookIdField.getText().isBlank()) {
                books.add(new Book(titleField.getText(), authorField.getText(), bookIdField.getText()));
                titleField.clear(); authorField.clear(); bookIdField.clear();
            }
        });

        save.setOnAction(e -> saveBooksToFile());
        check.setOnAction(e -> checkOverdue(stage));
        backBtn.setOnAction(e -> stage.setScene(back));

        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(8);
        form.addRow(0, new Label("Title:"), titleField);
        form.addRow(1, new Label("Author:"), authorField);
        form.addRow(2, new Label("Book ID:"), bookIdField);

        VBox content = new VBox(10, form, new HBox(10, add, check, save), table, backBtn);
        content.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(dateBox);
        root.setCenter(content);

        return new Scene(root, 700, 500);
    }

    private void checkOverdue(Stage stage) {
        LocalDate today = LocalDate.now();
        List<UserRecord> overdue = userRecords.stream()
                .filter(r -> r.getExpiry().isBefore(today))
                .collect(Collectors.toList());

        if (!overdue.isEmpty()) {
            String msg = overdue.stream()
                    .map(r -> r.getName() + " (" + r.getBookName() + ") - expired on " + r.getExpiry().format(DF))
                    .collect(Collectors.joining("\n"));
            new Alert(Alert.AlertType.WARNING, "Overdue Books:\n\n" + msg).showAndWait();
        } else {
            new Alert(Alert.AlertType.INFORMATION, "No overdue records found.").showAndWait();
        }
    }

    private void saveBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Book b : books) {
                writer.write("Title: " + b.getTitle() + ", Author: " + b.getAuthor() + ", Book ID: " + b.getBookId());
                writer.newLine();
            }
            new Alert(Alert.AlertType.INFORMATION, "Book list saved to books.txt").showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error saving file: " + e.getMessage()).showAndWait();
        }
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        button.setOnMousePressed(e ->
                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"));
        button.setOnMouseReleased(e ->
                button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"));
        return button;
    }

    private Label createDateTimeLabel() {
        Label dateTimeLabel = new Label();
        dateTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm:ss");

        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            dateTimeLabel.setText("Date & Time: " + LocalDateTime.now().format(dtf));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        return dateTimeLabel;
    }

    private <T> TableColumn<T, String> createColumn(String title, String prop) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    private <T> TableColumn<T, String> dateColumn(String title, java.util.function.Function<T, LocalDate> getter) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue()).format(DF)));
        return col;
    }

    private boolean fieldsValid(TextField a, TextField b, TextField c, TextField d, DatePicker dp) {
        return !a.getText().isBlank() && !b.getText().isBlank() && !c.getText().isBlank()
                && !d.getText().isBlank() && dp.getValue() != null;
    }

    public static class Book {
        private final SimpleStringProperty title = new SimpleStringProperty();
        private final SimpleStringProperty author = new SimpleStringProperty();
        private final SimpleStringProperty bookId = new SimpleStringProperty();

        public Book(String t, String a, String id) {
            title.set(t);
            author.set(a);
            bookId.set(id);
        }

        public String getTitle() { return title.get(); }
        public String getAuthor() { return author.get(); }
        public String getBookId() { return bookId.get(); }
    }

    public static class UserRecord {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty userId = new SimpleStringProperty();
        private final SimpleStringProperty bookName = new SimpleStringProperty();
        private final SimpleStringProperty bookId = new SimpleStringProperty();
        private final ObjectProperty<LocalDate> start = new SimpleObjectProperty<>();
        private final ObjectProperty<LocalDate> expiry = new SimpleObjectProperty<>();

        public UserRecord(String name, String userId, String bookName, String bookId, LocalDate start, LocalDate expiry) {
            this.name.set(name); this.userId.set(userId); this.bookName.set(bookName);
            this.bookId.set(bookId); this.start.set(start); this.expiry.set(expiry);
        }

        public String getName() { return name.get(); }
        public String getUserId() { return userId.get(); }
        public String getBookName() { return bookName.get(); }
        public String getBookId() { return bookId.get(); }
        public LocalDate getStart() { return start.get(); }
        public LocalDate getExpiry() { return expiry.get(); }

        public void setName(String v) { name.set(v); }
        public void setUserId(String v) { userId.set(v); }
        public void setBookName(String v) { bookName.set(v); }
        public void setBookId(String v) { bookId.set(v); }
        public void setStart(LocalDate v) { start.set(v); }
        public void setExpiry(LocalDate v) { expiry.set(v); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
