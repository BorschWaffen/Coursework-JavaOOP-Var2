package ua.edu.sumdu.cs.coursework.ui;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ua.edu.sumdu.cs.coursework.dao.HrRepository;
import ua.edu.sumdu.cs.coursework.db.DatabaseConfig;
import ua.edu.sumdu.cs.coursework.exception.HrDatabaseException;
import ua.edu.sumdu.cs.coursework.exception.HrValidationException;
import ua.edu.sumdu.cs.coursework.io.JsonBackupService;
import ua.edu.sumdu.cs.coursework.model.Department;
import ua.edu.sumdu.cs.coursework.model.Employee;
import ua.edu.sumdu.cs.coursework.service.HrAnalyticsService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CourseworkApp extends Application {
    private HrRepository repository;
    private final HrAnalyticsService analyticsService = new HrAnalyticsService();
    private final JsonBackupService jsonBackupService = new JsonBackupService();

    private final ObservableList<Department> departmentsList = FXCollections.observableArrayList();
    private final ObservableList<Employee> employeesList = FXCollections.observableArrayList();

    private TableView<Employee> employeeTable;
    private TextField hostField;
    private TextField portField;
    private TextField dbNameField;
    private TextField userField;
    private PasswordField passwordField;

    private TextField fullNameField;
    private TextField positionField;
    private TextField salaryField;
    private DatePicker hireDatePicker;
    private ComboBox<Department> formDepartmentCombo;

    private TextField searchPositionField;
    private ComboBox<Department> filterDepartmentCombo;

    private TextField minExperienceField;
    private TextField raisePercentField;
    private ComboBox<Department> topExpDepartmentCombo;
    private Label topEmployeeLabel;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        statusLabel = new Label("Введіть параметри підключення до PostgreSQL та натисніть «Підключитися».");

        VBox connectionBox = buildConnectionPanel();
        VBox formBox = buildEmployeeFormPanel();
        VBox filterSortBox = buildSearchFilterSortPanel();
        VBox tableBox = buildTablePanel();
        VBox analyticsJsonBox = buildAnalyticsAndJsonPanel(primaryStage);

        VBox mainLayout = new VBox(10, connectionBox, formBox, filterSortBox, tableBox, analyticsJsonBox);
        mainLayout.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(mainLayout);
        root.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(4, 12, 8, 12));

        Scene scene = new Scene(root, 1080, 820);
        primaryStage.setTitle("Курсова робота (Варіант 2) — Облік працівників та підрозділів");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox buildConnectionPanel() {
        hostField = new TextField("localhost");
        hostField.setPrefWidth(100);
        portField = new TextField("5432");
        portField.setPrefWidth(65);
        dbNameField = new TextField("coursework_hr");
        dbNameField.setPrefWidth(130);
        userField = new TextField("postgres");
        userField.setPrefWidth(100);
        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль БД");
        passwordField.setPrefWidth(120);

        Button connectBtn = new Button("Підключитися");
        connectBtn.setOnAction(e -> connectAndReload());

        HBox row = new HBox(8,
                new Label("Хост:"), hostField,
                new Label("Порт:"), portField,
                new Label("БД:"), dbNameField,
                new Label("Користувач:"), userField,
                new Label("Пароль:"), passwordField,
                connectBtn
        );
        return new VBox(4, new Label("Підключення до PostgreSQL"), row);
    }

    private VBox buildEmployeeFormPanel() {
        fullNameField = new TextField();
        fullNameField.setPromptText("ПІБ працівника");
        positionField = new TextField();
        positionField.setPromptText("Посада");
        salaryField = new TextField();
        salaryField.setPromptText("Зарплата (напр. 52000)");
        hireDatePicker = new DatePicker(LocalDate.now());

        formDepartmentCombo = new ComboBox<>(departmentsList);
        formDepartmentCombo.setPromptText("Оберіть підрозділ");

        Button addBtn = new Button("Додати");
        addBtn.setOnAction(e -> handleAddEmployee());

        Button updateBtn = new Button("Оновити вибраного");
        updateBtn.setOnAction(e -> handleUpdateEmployee());

        Button deleteBtn = new Button("Видалити вибраного");
        deleteBtn.setOnAction(e -> handleDeleteEmployee());

        Button clearBtn = new Button("Очистити поля");
        clearBtn.setOnAction(e -> clearFormFields());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.add(new Label("ПІБ:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Посада:"), 2, 0);
        grid.add(positionField, 3, 0);
        grid.add(new Label("Підрозділ:"), 4, 0);
        grid.add(formDepartmentCombo, 5, 0);

        grid.add(new Label("Зарплата:"), 0, 1);
        grid.add(salaryField, 1, 1);
        grid.add(new Label("Дата прийому:"), 2, 1);
        grid.add(hireDatePicker, 3, 1);

        HBox actions = new HBox(8, addBtn, updateBtn, deleteBtn, clearBtn);
        return new VBox(6, new Label("Дані працівника"), grid, actions);
    }

    private VBox buildSearchFilterSortPanel() {
        searchPositionField = new TextField();
        searchPositionField.setPromptText("Пошук за посадою...");

        Button searchBtn = new Button("Знайти за посадою");
        searchBtn.setOnAction(e -> handleSearchByPosition());

        filterDepartmentCombo = new ComboBox<>(departmentsList);
        filterDepartmentCombo.setPromptText("Фільтр за підрозділом");

        Button filterBtn = new Button("Фільтрувати");
        filterBtn.setOnAction(e -> handleFilterByDepartment());

        Button sortHireDateAscBtn = new Button("Сортувати за датою прийому (↑)");
        sortHireDateAscBtn.setOnAction(e -> handleSortByHireDate(true));

        Button sortHireDateDescBtn = new Button("Сортувати за датою прийому (↓)");
        sortHireDateDescBtn.setOnAction(e -> handleSortByHireDate(false));

        Button resetBtn = new Button("Скинути фільтр / Оновити з БД");
        resetBtn.setOnAction(e -> reloadFromDatabase());

        HBox row1 = new HBox(8, searchPositionField, searchBtn, filterDepartmentCombo, filterBtn, resetBtn);
        HBox row2 = new HBox(8, sortHireDateAscBtn, sortHireDateDescBtn);
        return new VBox(6, new Label("Пошук, фільтрація та сортування"), row1, row2);
    }

    private VBox buildTablePanel() {
        employeeTable = new TableView<>(employeesList);
        employeeTable.setPrefHeight(220);

        TableColumn<Employee, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));

        TableColumn<Employee, String> nameCol = new TableColumn<>("ПІБ");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        nameCol.setPrefWidth(220);

        TableColumn<Employee, String> posCol = new TableColumn<>("Посада");
        posCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPosition()));
        posCol.setPrefWidth(180);

        TableColumn<Employee, BigDecimal> salCol = new TableColumn<>("Зарплата");
        salCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSalary()));

        TableColumn<Employee, LocalDate> dateCol = new TableColumn<>("Дата прийому");
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getHireDate()));

        TableColumn<Employee, String> deptCol = new TableColumn<>("Підрозділ");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartmentName()));
        deptCol.setPrefWidth(160);

        employeeTable.getColumns().addAll(idCol, nameCol, posCol, salCol, dateCol, deptCol);

        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fullNameField.setText(newSel.getFullName());
                positionField.setText(newSel.getPosition());
                salaryField.setText(newSel.getSalary().toPlainString());
                hireDatePicker.setValue(newSel.getHireDate());
                selectDepartmentInCombo(formDepartmentCombo, newSel.getDepartmentId());
            }
        });

        return new VBox(4, employeeTable);
    }

    private VBox buildAnalyticsAndJsonPanel(Stage stage) {
        minExperienceField = new TextField();
        minExperienceField.setPromptText("Стаж від (років)");
        minExperienceField.setPrefWidth(110);

        raisePercentField = new TextField();
        raisePercentField.setPromptText("Відсоток (напр. 10)");
        raisePercentField.setPrefWidth(120);

        Button raiseBtn = new Button("Підвищити зарплату за стажем");
        raiseBtn.setOnAction(e -> handleRaiseSalary());

        topExpDepartmentCombo = new ComboBox<>(departmentsList);
        topExpDepartmentCombo.setPromptText("Підрозділ");

        Button topExpBtn = new Button("Найбільший стаж у підрозділі");
        topExpBtn.setOnAction(e -> handleFindMostExperienced());

        topEmployeeLabel = new Label("Найдосвідченіший працівник підрозділу: —");

        Button exportJsonBtn = new Button("Експорт у JSON");
        exportJsonBtn.setOnAction(e -> handleExportJson(stage));

        Button importJsonBtn = new Button("Імпорт з JSON");
        importJsonBtn.setOnAction(e -> handleImportJson(stage));

        HBox raiseRow = new HBox(8,
                new Label("Стаж >= "), minExperienceField,
                new Label("На %:"), raisePercentField,
                raiseBtn
        );
        HBox topExpRow = new HBox(8, topExpDepartmentCombo, topExpBtn, topEmployeeLabel);
        HBox jsonRow = new HBox(8, exportJsonBtn, importJsonBtn);

        return new VBox(6, new Label("Аналітика та обмін JSON"), raiseRow, topExpRow, jsonRow);
    }

    private void connectAndReload() {
        try {
            DatabaseConfig cfg = new DatabaseConfig(
                    hostField.getText(),
                    portField.getText(),
                    dbNameField.getText(),
                    userField.getText(),
                    passwordField.getText()
            );
            HrRepository repo = new HrRepository(cfg);
            repo.testConnection();
            this.repository = repo;
            reloadFromDatabase();
            statusLabel.setText("Підключено до PostgreSQL (" + dbNameField.getText().trim() + ").");
        } catch (HrDatabaseException | HrValidationException ex) {
            showError("Помилка підключення", ex.getMessage());
        }
    }

    private void reloadFromDatabase() {
        ensureConnected();
        try {
            List<Department> depts = repository.findAllDepartments();
            departmentsList.setAll(depts);

            List<Employee> emps = repository.findAllEmployees();
            employeesList.setAll(emps);
            statusLabel.setText("Завантажено підрозділів: " + depts.size() + ", працівників: " + emps.size() + ".");
        } catch (HrDatabaseException ex) {
            showError("Помилка БД", ex.getMessage());
        }
    }

    private void handleAddEmployee() {
        ensureConnected();
        try {
            Employee emp = readEmployeeFromForm(null);
            repository.addEmployee(emp);
            reloadFromDatabase();
            clearFormFields();
            statusLabel.setText("Працівника успішно додано.");
        } catch (HrValidationException | HrDatabaseException ex) {
            showError("Помилка додавання", ex.getMessage());
        }
    }

    private void handleUpdateEmployee() {
        ensureConnected();
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Не обрано запис", "Оберіть працівника у таблиці для редагування.");
            return;
        }
        try {
            Employee updated = readEmployeeFromForm(selected.getId());
            repository.updateEmployee(updated);
            reloadFromDatabase();
            statusLabel.setText("Дані працівника оновлено.");
        } catch (HrValidationException | HrDatabaseException ex) {
            showError("Помилка редагування", ex.getMessage());
        }
    }

    private void handleDeleteEmployee() {
        ensureConnected();
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Не обрано запис", "Оберіть працівника у таблиці для видалення.");
            return;
        }
        try {
            repository.deleteEmployee(selected.getId());
            reloadFromDatabase();
            clearFormFields();
            statusLabel.setText("Працівника видалено.");
        } catch (HrDatabaseException ex) {
            showError("Помилка видалення", ex.getMessage());
        }
    }

    private void handleSearchByPosition() {
        List<Employee> matches = analyticsService.searchByPosition(employeesList, searchPositionField.getText());
        employeesList.setAll(matches);
        statusLabel.setText("Знайдено за посадою: " + matches.size() + ".");
    }

    private void handleFilterByDepartment() {
        Department dept = filterDepartmentCombo.getValue();
        if (dept == null) {
            showError("Фільтрація", "Оберіть підрозділ зі списку.");
            return;
        }
        List<Employee> filtered = analyticsService.filterByDepartment(employeesList, dept.getId());
        employeesList.setAll(filtered);
        statusLabel.setText("Відфільтровано за підрозділом «" + dept.getName() + "»: " + filtered.size() + ".");
    }

    private void handleSortByHireDate(boolean ascending) {
        List<Employee> sorted = analyticsService.sortByHireDate(employeesList, ascending);
        employeesList.setAll(sorted);
        statusLabel.setText("Відсортовано за датою прийняття на роботу (" + (ascending ? "за зростанням" : "за спаданням") + ").");
    }

    private void handleRaiseSalary() {
        ensureConnected();
        try {
            int minYears = Integer.parseInt(minExperienceField.getText().trim());
            double percent = Double.parseDouble(raisePercentField.getText().trim().replace(',', '.'));
            if (minYears < 0 || percent <= 0) {
                throw new HrValidationException("Стаж має бути >= 0, а відсоток підвищення > 0.");
            }
            int count = repository.raiseSalaryByExperience(minYears, percent);
            reloadFromDatabase();
            statusLabel.setText("Підвищено зарплату на " + percent + "% працівникам зі стажем >= " + minYears + " р. (оновлено: " + count + ").");
        } catch (NumberFormatException ex) {
            showError("Некоректний ввід", "Введіть коректні числові значення для стажу та відсотка.");
        } catch (HrValidationException | HrDatabaseException ex) {
            showError("Помилка оновлення зарплати", ex.getMessage());
        }
    }

    private void handleFindMostExperienced() {
        Department dept = topExpDepartmentCombo.getValue();
        try {
            Employee best = analyticsService.findMostExperiencedInDepartment(employeesList, dept == null ? null : dept.getId());
            if (best == null) {
                topEmployeeLabel.setText("Найдосвідченіший працівник підрозділу: записів немає.");
            } else {
                int years = best.getExperienceYears(LocalDate.now());
                topEmployeeLabel.setText("Найдосвідченіший: " + best.getFullName()
                        + " (" + best.getPosition() + ", стаж: " + years + " р., дата прийому: " + best.getHireDate() + ")");
            }
        } catch (HrValidationException ex) {
            showError("Аналітика підрозділу", ex.getMessage());
        }
    }

    private void handleExportJson(Stage stage) {
        ensureConnected();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Експорт даних у JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            jsonBackupService.exportToJson(file, repository.findAllDepartments(), repository.findAllEmployees());
            statusLabel.setText("Дані експортовано у файл: " + file.getName());
        } catch (IOException | HrDatabaseException | HrValidationException ex) {
            showError("Помилка експорту JSON", ex.getMessage());
        }
    }

    private void handleImportJson(Stage stage) {
        ensureConnected();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Імпорт даних із JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            JsonBackupService.HrDataSnapshot snapshot = jsonBackupService.importFromJson(file);
            repository.replaceAllData(snapshot.departments, snapshot.employees);
            reloadFromDatabase();
            statusLabel.setText("Дані імпортовано з файлу: " + file.getName());
        } catch (IOException | HrDatabaseException | HrValidationException ex) {
            showError("Помилка імпорту JSON", ex.getMessage());
        }
    }

    private Employee readEmployeeFromForm(Integer id) {
        Department selectedDept = formDepartmentCombo.getValue();
        if (selectedDept == null) {
            throw new HrValidationException("Оберіть підрозділ для працівника.");
        }
        String rawSalary = salaryField.getText() == null ? "" : salaryField.getText().trim().replace(',', '.');
        BigDecimal salary;
        try {
            salary = new BigDecimal(rawSalary);
        } catch (NumberFormatException ex) {
            throw new HrValidationException("Некоректний формат заробітної плати.");
        }
        LocalDate hireDate = hireDatePicker.getValue();
        return new Employee(
                id,
                fullNameField.getText(),
                positionField.getText(),
                salary,
                hireDate,
                selectedDept.getId(),
                selectedDept.getName()
        );
    }

    private void selectDepartmentInCombo(ComboBox<Department> combo, Integer departmentId) {
        if (departmentId == null) {
            return;
        }
        for (Department d : combo.getItems()) {
            if (departmentId.equals(d.getId())) {
                combo.setValue(d);
                return;
            }
        }
    }

    private void clearFormFields() {
        fullNameField.clear();
        positionField.clear();
        salaryField.clear();
        hireDatePicker.setValue(LocalDate.now());
        formDepartmentCombo.getSelectionModel().clearSelection();
        employeeTable.getSelectionModel().clearSelection();
    }

    private void ensureConnected() {
        if (repository == null) {
            throw new HrValidationException("Спочатку підключіться до бази даних PostgreSQL.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}