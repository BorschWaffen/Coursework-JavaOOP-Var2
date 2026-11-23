package ua.edu.sumdu.cs.coursework.model;

import ua.edu.sumdu.cs.coursework.exception.HrValidationException;
import java.util.Objects;

public class Department {
    private Integer id;
    private String name;
    private String location;

    public Department() {
    }

    public Department(Integer id, String name, String location) {
        this.id = id;
        setName(name);
        setLocation(location);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new HrValidationException("Назва підрозділу не може бути порожньою.");
        }
        this.name = name.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new HrValidationException("Розташування підрозділу не може бути порожнім.");
        }
        this.location = location.trim();
    }

    @Override
    public String toString() {
        return name + " (" + location + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}