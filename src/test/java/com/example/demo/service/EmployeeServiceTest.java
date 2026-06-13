package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.EmployeeRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void getAllEmployees_returnsAllFromRepository() {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        assertThat(employeeService.getAllEmployees()).containsExactly(employee);
    }

    @Test
    void createEmployee_savesWhenEmailIsUnique() {
        EmployeeRequest request = employeeRequest("Alice", "alice@example.com", "IT");
        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Employee created = employeeService.createEmployee(request);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Alice");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_throwsWhenEmailAlreadyExists() {
        EmployeeRequest request = employeeRequest("Alice", "alice@example.com", "IT");
        when(employeeRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void getEmployeeById_returnsEmployee() {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThat(employeeService.getEmployeeById(1L)).isEqualTo(employee);
    }

    @Test
    void getEmployeeById_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getEmployeeByEmail_returnsEmployee() {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        when(employeeRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(employee));

        assertThat(employeeService.getEmployeeByEmail("alice@example.com")).isEqualTo(employee);
    }

    @Test
    void getEmployeesByDepartment_delegatesToRepository() {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        when(employeeRepository.findByDepartmentIgnoreCase("IT")).thenReturn(List.of(employee));

        assertThat(employeeService.getEmployeesByDepartment("IT")).containsExactly(employee);
    }

    @Test
    void searchEmployeesByName_delegatesToRepository() {
        Employee employee = new Employee("Alice Johnson", "alice@example.com", "IT");
        when(employeeRepository.findByNameContainingIgnoreCase("alice")).thenReturn(List.of(employee));

        assertThat(employeeService.searchEmployeesByName("alice")).containsExactly(employee);
    }

    @Test
    void updateEmployee_updatesExistingEmployee() {
        Employee existing = new Employee("Alice", "alice@example.com", "IT");
        existing.setId(1L);
        EmployeeRequest request = employeeRequest("Alice Updated", "alice.updated@example.com", "HR");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByEmail("alice.updated@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.save(existing)).thenReturn(existing);

        Employee updated = employeeService.updateEmployee(1L, request);

        assertThat(updated.getName()).isEqualTo("Alice Updated");
        assertThat(updated.getEmail()).isEqualTo("alice.updated@example.com");
        assertThat(updated.getDepartment()).isEqualTo("HR");
    }

    @Test
    void updateEmployee_throwsWhenEmailBelongsToAnotherEmployee() {
        Employee existing = new Employee("Alice", "alice@example.com", "IT");
        existing.setId(1L);
        Employee other = new Employee("Bob", "bob@example.com", "HR");
        other.setId(2L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> employeeService.updateEmployee(1L,
                employeeRequest("Alice", "bob@example.com", "IT")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteEmployee_deletesExistingEmployee() {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(employee);
    }

    private EmployeeRequest employeeRequest(String name, String email, String department) {
        EmployeeRequest request = new EmployeeRequest();
        request.setName(name);
        request.setEmail(email);
        request.setDepartment(department);
        return request;
    }
}
