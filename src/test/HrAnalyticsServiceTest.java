package ua.edu.sumdu.cs.coursework;

import org.junit.jupiter.api.Test;
import ua.edu.sumdu.cs.coursework.model.Employee;
import ua.edu.sumdu.cs.coursework.service.HrAnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HrAnalyticsServiceTest {

    @Test
    void shouldFilterByDepartmentSearchByPositionAndSortByHireDate() {
        HrAnalyticsService service = new HrAnalyticsService();
        Employee e1 = new Employee(1, "Іванов І.І.", "Java Developer", new BigDecimal("50000"),
                LocalDate.of(2022, 5, 10), 1, "Розробка ПЗ");
        Employee e2 = new Employee(2, "Петров П.П.", "Senior Java Developer", new BigDecimal("70000"),
                LocalDate.of(2019, 1, 15), 1, "Розробка ПЗ");
        Employee e3 = new Employee(3, "Сидорова С.С.", "QA Engineer", new BigDecimal("45000"),
                LocalDate.of(2021, 3, 20), 2, "QA");

        List<Employee> all = List.of(e1, e2, e3);

        List<Employee> dept1 = service.filterByDepartment(all, 1);
        assertEquals(2, dept1.size());

        List<Employee> javaDevs = service.searchByPosition(all, "java");
        assertEquals(2, javaDevs.size());

        List<Employee> sortedAsc = service.sortByHireDate(dept1, true);
        assertEquals(2, sortedAsc.get(0).getId());
        assertEquals(1, sortedAsc.get(1).getId());

        Employee mostExperienced = service.findMostExperiencedInDepartment(all, 1);
        assertNotNull(mostExperienced);
        assertEquals(2, mostExperienced.getId());
    }
}