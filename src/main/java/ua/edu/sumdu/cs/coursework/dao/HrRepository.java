package ua.edu.sumdu.cs.coursework.dao;

import ua.edu.sumdu.cs.coursework.db.DatabaseConfig;
import ua.edu.sumdu.cs.coursework.exception.HrDatabaseException;
import ua.edu.sumdu.cs.coursework.model.Department;
import ua.edu.sumdu.cs.coursework.model.Employee;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HrRepository {
    private final DatabaseConfig config;

    public HrRepository(DatabaseConfig config) {
        this.config = config;
    }

    public void testConnection() {
        try (Connection conn = config.getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("З'єднання з базою даних закрите.");
            }
        } catch (SQLException e) {
            throw new HrDatabaseException("Не вдалося підключитися до PostgreSQL: " + e.getMessage(), e);
        }
    }

    public List<Department> findAllDepartments() {
        String sql = "SELECT id, name, location FROM departments ORDER BY name";
        List<Department> result = new ArrayList<>();
        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Department(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location")
                ));
            }
            return result;
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка читання підрозділів: " + e.getMessage(), e);
        }
    }

    public void addDepartment(Department department) {
        String sql = "INSERT INTO departments (name, location) VALUES (?, ?)";
        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getName());
            ps.setString(2, department.getLocation());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка додавання підрозділу: " + e.getMessage(), e);
        }
    }

    public List<Employee> findAllEmployees() {
        String sql = "SELECT e.id, e.full_name, e.position, e.salary, e.hire_date, " +
                "e.department_id, d.name AS department_name " +
                "FROM employees e JOIN departments d ON e.department_id = d.id ORDER BY e.id";
        List<Employee> result = new ArrayList<>();
        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapEmployee(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка читання працівників: " + e.getMessage(), e);
        }
    }

    public void addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (full_name, position, salary, hire_date, department_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getPosition());
            ps.setBigDecimal(3, employee.getSalary());
            ps.setDate(4, Date.valueOf(employee.getHireDate()));
            ps.setInt(5, employee.getDepartmentId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка додавання працівника: " + e.getMessage(), e);
        }
    }

    public void updateEmployee(Employee employee) {
        if (employee.getId() == null) {
            throw new HrDatabaseException("Не вказано ID працівника для редагування.", null);
        }
        String sql = "UPDATE employees SET full_name = ?, position = ?, salary = ?, " +
                "hire_date = ?, department_id = ? WHERE id = ?";
        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getPosition());
            ps.setBigDecimal(3, employee.getSalary());
            ps.setDate(4, Date.valueOf(employee.getHireDate()));
            ps.setInt(5, employee.getDepartmentId());
            ps.setInt(6, employee.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка оновлення працівника: " + e.getMessage(), e);
        }
    }

    public void deleteEmployee(int employeeId) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка видалення працівника: " + e.getMessage(), e);
        }
    }

    public int raiseSalaryByExperience(int minExperienceYears, double percent) {
        List<Employee> employees = findAllEmployees();
        LocalDate today = LocalDate.now();
        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100.0), 6, RoundingMode.HALF_UP)
        );

        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        int updatedCount = 0;

        try (Connection conn = config.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Employee emp : employees) {
                if (emp.getExperienceYears(today) >= minExperienceYears) {
                    BigDecimal newSalary = emp.getSalary().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                    ps.setBigDecimal(1, newSalary);
                    ps.setInt(2, emp.getId());
                    ps.addBatch();
                    updatedCount++;
                }
            }
            ps.executeBatch();
            return updatedCount;
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка підвищення заробітної плати: " + e.getMessage(), e);
        }
    }

    public void replaceAllData(List<Department> departments, List<Employee> employees) {
        String deleteEmployeesSql = "DELETE FROM employees";
        String deleteDepartmentsSql = "DELETE FROM departments";
        String insertDepartmentSql = "INSERT INTO departments (id, name, location) VALUES (?, ?, ?)";
        String insertEmployeeSql = "INSERT INTO employees (id, full_name, position, salary, hire_date, department_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = config.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delEmp = conn.prepareStatement(deleteEmployeesSql);
                 PreparedStatement delDept = conn.prepareStatement(deleteDepartmentsSql);
                 PreparedStatement insDept = conn.prepareStatement(insertDepartmentSql);
                 PreparedStatement insEmp = conn.prepareStatement(insertEmployeeSql)) {

                delEmp.executeUpdate();
                delDept.executeUpdate();

                for (Department d : departments) {
                    insDept.setInt(1, d.getId());
                    insDept.setString(2, d.getName());
                    insDept.setString(3, d.getLocation());
                    insDept.addBatch();
                }
                insDept.executeBatch();

                for (Employee e : employees) {
                    insEmp.setInt(1, e.getId());
                    insEmp.setString(2, e.getFullName());
                    insEmp.setString(3, e.getPosition());
                    insEmp.setBigDecimal(4, e.getSalary());
                    insEmp.setDate(5, Date.valueOf(e.getHireDate()));
                    insEmp.setInt(6, e.getDepartmentId());
                    insEmp.addBatch();
                }
                insEmp.executeBatch();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new HrDatabaseException("Помилка імпорту даних у БД: " + e.getMessage(), e);
        }
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("position"),
                rs.getBigDecimal("salary"),
                rs.getDate("hire_date").toLocalDate(),
                rs.getInt("department_id"),
                rs.getString("department_name")
        );
    }
}