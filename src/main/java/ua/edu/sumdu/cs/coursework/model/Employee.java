package ua.edu.sumdu.cs.coursework.model;

import ua.edu.sumdu.cs.coursework.exception.HrValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

public class Employee implements Comparable<Employee> {
    private Integer id;
    private String fullName;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private Integer departmentId;
    private String departmentName;

    public Employee() {
    }

    public Employee(Integer id, String fullName, String position, BigDecimal salary,
                    LocalDate hireDate, Integer departmentId, String departmentName) {
        this.id = id;
        setFullName(fullName);
        setPosition(position);
        setSalary(salary);
        setHireDate(hireDate);
        setDepartmentId(departmentId);
        this.departmentName = departmentName;
    }

    public int getExperienceYears(LocalDate referenceDate) {
        if (hireDate == null || referenceDate == null || hireDate.isAfter(referenceDate)) {
            return 0;
        }
        return Period.between(hireDate, referenceDate).getYears();
    }

    @Override
    public int compareTo(Employee other) {
        if (other == null) {
            return 1;
        }
        if (this.hireDate == null && other.hireDate == null) {
            return 0;
        }
        if (this.hireDate == null) {
            return -1;
        }
        if (other.hireDate == null) {
            return 1;
        }
        return this.hireDate.compareTo(other.hireDate);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new HrValidationException("ПІБ працівника не може бути порожнім.");
        }
        this.fullName = fullName.trim();
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            throw new HrValidationException("Посада працівника не може бути порожньою.");
        }
        this.position = position.trim();
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        if (salary == null || salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new HrValidationException("Заробітна плата не може бути від'ємною.");
        }
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        if (hireDate == null || hireDate.isAfter(LocalDate.now())) {
            throw new HrValidationException("Некоректна дата прийняття на роботу.");
        }
        this.hireDate = hireDate;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        if (departmentId == null || departmentId <= 0) {
            throw new HrValidationException("Необхідно обрати коректний підрозділ.");
        }
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}