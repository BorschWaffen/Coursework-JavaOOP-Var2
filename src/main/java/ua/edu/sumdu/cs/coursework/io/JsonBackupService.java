package ua.edu.sumdu.cs.coursework.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.edu.sumdu.cs.coursework.exception.HrValidationException;
import ua.edu.sumdu.cs.coursework.model.Department;
import ua.edu.sumdu.cs.coursework.model.Employee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonBackupService {
    private final ObjectMapper objectMapper;

    public JsonBackupService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static class HrDataSnapshot {
        public List<Department> departments = new ArrayList<>();
        public List<Employee> employees = new ArrayList<>();
    }

    public void exportToJson(File targetFile, List<Department> departments, List<Employee> employees) throws IOException {
        if (targetFile == null) {
            throw new HrValidationException("Не вказано файл для експорту JSON.");
        }
        HrDataSnapshot snapshot = new HrDataSnapshot();
        snapshot.departments = departments == null ? new ArrayList<>() : departments;
        snapshot.employees = employees == null ? new ArrayList<>() : employees;
        objectMapper.writeValue(targetFile, snapshot);
    }

    public HrDataSnapshot importFromJson(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new HrValidationException("Обраний JSON-файл не існує.");
        }
        HrDataSnapshot snapshot = objectMapper.readValue(sourceFile, HrDataSnapshot.class);
        if (snapshot == null) {
            throw new HrValidationException("JSON-файл порожній або пошкоджений.");
        }
        return snapshot;
    }
}