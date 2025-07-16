package application;
import javafx.application.Application;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main extends Application {
    private TableView<Student> table = new TableView<>();
    private ObservableList<Student> data = FXCollections.observableArrayList();

    private TextField tfName = new TextField(),
            tfDept = new TextField(),
            tfYear = new TextField(),
            tfCourse = new TextField();

    private Stage primaryStage;
    private static final String FILENAME = "students.txt";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Student Management System");
        loadFromFile();             
        showLoginScene();
    }

    private void loadFromFile() {
        Path path = Paths.get(FILENAME);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String ln : lines) {
                    String[] parts = ln.split(",");
                    if (parts.length == 4) {
                        data.add(new Student(parts[0], parts[1], parts[2], parts[3]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showLoginScene() {
        Label lblUser = new Label("Username:");
        TextField tfUser = new TextField();
        Label lblPass = new Label("Password:");
        PasswordField pfPass = new PasswordField();
        Button btnLogin = new Button("Login");
        Label lblMessage = new Label();
        lblMessage.setTextFill(Color.RED);

        GridPane loginPane = new GridPane();
        loginPane.setPadding(new Insets(20));
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setAlignment(Pos.CENTER);

        loginPane.add(lblUser, 0, 0);
        loginPane.add(tfUser, 1, 0);
        loginPane.add(lblPass, 0, 1);
        loginPane.add(pfPass, 1, 1);
        loginPane.add(btnLogin, 1, 2);
        loginPane.add(lblMessage, 1, 3);

        btnLogin.setOnAction(e -> {
            if ("admin".equals(tfUser.getText().trim()) && "admin".equals(pfPass.getText())) {
                lblMessage.setText("");
                showMainScene();
            } else {
                lblMessage.setText("Invalid username or password");
            }
        });

        Scene loginScene = new Scene(loginPane, 300, 200);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void showMainScene() {
        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> deptCol = new TableColumn<>("Department");
        deptCol.setMinWidth(100);
        deptCol.setCellValueFactory(new PropertyValueFactory<>("dept"));

        TableColumn<Student, String> yearCol = new TableColumn<>("Year");
        yearCol.setMinWidth(60);
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Student, String> courseCol = new TableColumn<>("Course");
        courseCol.setMinWidth(100);
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));

        table.getColumns().setAll(nameCol, deptCol, yearCol, courseCol);

        FilteredList<Student> filtered = new FilteredList<>(data, s -> true);
        SortedList<Student> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Search...");
        Button btnSearch = new Button("Search");
        btnSearch.setOnAction(e -> {
            String txt = tfSearch.getText().toLowerCase().trim();
            filtered.setPredicate(s -> txt.isEmpty() ||
                    s.getName().toLowerCase().contains(txt) ||
                    s.getDept().toLowerCase().contains(txt) ||
                    s.getYear().toLowerCase().contains(txt) ||
                    s.getCourse().toLowerCase().contains(txt));
        });
        tfSearch.textProperty().addListener((obs, old, nw) -> btnSearch.fire());

        HBox searchBox = new HBox(10, tfSearch, btnSearch);
        searchBox.setPadding(new Insets(10));
        searchBox.setStyle("-fx-background-color: #e0f7fa;");

        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> addStudent());
        btnAdd.setStyle("-fx-background-color: blue;");
        Button btnUpdate = new Button("Update");
        btnUpdate.setOnAction(e -> updateStudent());
        btnUpdate.setStyle("-fx-background-color: blue;");
        Button btnDelete = new Button("Delete");
        btnDelete.setOnAction(e -> deleteStudent());
        btnDelete.setStyle("-fx-background-color: blue;");
        Button btnSave = new Button("Save");
        btnSave.setOnAction(e -> saveToFile());
        btnSave.setStyle("-fx-background-color: blue;");

        tfName.setPromptText("Name");
        tfDept.setPromptText("Department");
        tfYear.setPromptText("Year");
        tfCourse.setPromptText("Course");

        HBox form = new HBox(10, tfName, tfDept, tfYear, tfCourse, btnAdd, btnUpdate, btnDelete, btnSave);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: lightblue;");

        VBox root = new VBox(10, searchBox, table, form);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: lightblue;");

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                tfName.setText(sel.getName());
                tfDept.setText(sel.getDept());
                tfYear.setText(sel.getYear());
                tfCourse.setText(sel.getCourse());
            }
        });

        Scene mainScene = new Scene(root, 700, 500);
        primaryStage.setScene(mainScene);
    }

    private void addStudent() {
        if (tfName.getText().trim().isEmpty() || tfDept.getText().trim().isEmpty() ||
            tfYear.getText().trim().isEmpty() || tfCourse.getText().trim().isEmpty()) {
            alert("All fields are required!");
            return;
        }
        data.add(new Student(tfName.getText().trim(), tfDept.getText().trim(),
                             tfYear.getText().trim(), tfCourse.getText().trim()));
        clear();
    }

    private void updateStudent() {
        Student s = table.getSelectionModel().getSelectedItem();
        if (s == null) {
            alert("Select a student!");
            return;
        }
        s.setName(tfName.getText().trim());
        s.setDept(tfDept.getText().trim());
        s.setYear(tfYear.getText().trim());
        s.setCourse(tfCourse.getText().trim());
        table.refresh();
        clear();
    }

    private void deleteStudent() {
        Student s = table.getSelectionModel().getSelectedItem();
        if (s == null) {
            alert("Select a student to delete!");
        } else {
            data.remove(s);
            clear();
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILENAME))) {
            for (Student s : data) {
                writer.write(String.join(",", s.getName(), s.getDept(), s.getYear(), s.getCourse()));
                writer.newLine();
            }
            alert("Data saved to " + FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
            alert("Failed to save data: " + e.getMessage());
        }
    }

    private void clear() {
        tfName.clear();
        tfDept.clear();
        tfYear.clear();
        tfCourse.clear();
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public static class Student {
        private String name, dept, year, course;
        public Student(String n, String d, String y, String c) {
            name = n; dept = d; year = y; course = c;
        }
        public String getName() { return name; }
        public void setName(String n) { name = n; }
        public String getDept() { return dept; }
        public void setDept(String d) { dept = d; }
        public String getYear() { return year; }
        public void setYear(String y) { year = y; }
        public String getCourse() { return course; }
        public void setCourse(String c) { course = c; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
