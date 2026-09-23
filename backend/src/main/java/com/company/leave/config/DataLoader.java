package com.company.leave.config;

import com.company.leave.entity.Employee;
import com.company.leave.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Inserts sample employees when the app starts (only when DB is empty, never in tests). */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    public DataLoader(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            employeeRepository.save(new Employee("Ravi Kumar", "ravi@company.com", "Engineering"));
            employeeRepository.save(new Employee("Priya Sharma", "priya@company.com", "HR"));
            employeeRepository.save(new Employee("Arjun Nair", "arjun@company.com", "Finance"));
        }
    }
}
