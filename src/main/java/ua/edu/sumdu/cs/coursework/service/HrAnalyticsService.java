package ua.edu.sumdu.cs.coursework.service;

import ua.edu.sumdu.cs.coursework.exception.HrValidationException;
import ua.edu.sumdu.cs.coursework.model.Employee;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HrAnalyticsService {

    public List<Employee> searchByPosition(List<Employee> employees, String positionQuery) {
        if (employees == null) {
            return new ArrayList<>();
        }
        String normalized = positionQuery == null ? "" : positionQuery.trim().toLowerCase();
        List<Employee> matches = new ArrayList<>();
        for (Employee emp : employees) {
            if (emp != null && emp.getPosition() != null
                    && emp.getPosition().toLowerCase().contains(normalized)) {
                matches.add(emp);
            }
        }
        return matches;
    }

    public List<Employee> filterByDepartment(List<Employee> employees, Integer departmentId) {
        if (employees == null) {
            return new ArrayList<>();
        }
        if (departmentId == null) {
            return new ArrayList<>(employees);
        }
        List<Employee> filtered = new ArrayList<>();
        for (Employee emp : employees) {
            if (emp != null && departmentId.equals(emp.getDepartmentId())) {
                filtered.add(emp);
            }
        }
        return filtered;
    }

    public List<Employee> sortByHireDate(List<Employee> employees, boolean ascending) {
        List<Employee> sorted = employees == null ? new ArrayList<>() : new ArrayList<>(employees);
        Collections.sort(sorted);
        if (!ascending) {
            Comparator<Employee> descComparator = (a, b) -> {
                if (a == b) {
                    return 0;
                }
                if (a == null) {
                    return 1;
                }
                if (b == null) {
                    return -1;
                }
                return b.compareTo(a);
            };
            sorted.sort(descComparator);
        }
        return sorted;
    }

    public Employee findMostExperiencedInDepartment(List<Employee> employees, Integer departmentId) {
        if (departmentId == null) {
            throw new HrValidationException("Оберіть підрозділ для визначення працівника з найбільшим стажем.");
        }
        List<Employee> inDepartment = filterByDepartment(employees, departmentId);
        if (inDepartment.isEmpty()) {
            return null;
        }

        LocalDate today = LocalDate.now();
        Employee best = inDepartment.get(0);
        int bestExperience = best.getExperienceYears(today);

        for (int i = 1; i < inDepartment.size(); i++) {
            Employee candidate = inDepartment.get(i);
            int candidateExperience = candidate.getExperienceYears(today);
            if (candidateExperience > bestExperience) {
                best = candidate;
                bestExperience = candidateExperience;
            } else if (candidateExperience == bestExperience
                    && candidate.getHireDate() != null
                    && best.getHireDate() != null
                    && candidate.getHireDate().isBefore(best.getHireDate())) {
                best = candidate;
            }
        }
        return best;
    }
}