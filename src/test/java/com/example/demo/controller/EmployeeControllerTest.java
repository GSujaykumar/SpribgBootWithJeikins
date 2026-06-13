package com.example.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.EmployeeRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAllEmployees_returnsEmployees() throws Exception {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        employee.setId(1L);
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void createEmployee_returnsCreatedEmployee() throws Exception {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        employee.setId(1L);
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice",
                                  "email": "alice@example.com",
                                  "department": "IT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    void createEmployee_returnsBadRequestWhenInvalid() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email",
                                  "department": "IT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation failed")));
    }

    @Test
    void createEmployee_returnsConflictWhenEmailExists() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateResourceException("Employee already exists with email: alice@example.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice",
                                  "email": "alice@example.com",
                                  "department": "IT"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("alice@example.com")));
    }

    @Test
    void getEmployeeById_returnsEmployee() throws Exception {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        employee.setId(1L);
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(get("/api/employees/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice")));
    }

    @Test
    void getEmployeeById_returnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/employees/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeByEmail_returnsEmployee() throws Exception {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        when(employeeService.getEmployeeByEmail("alice@example.com")).thenReturn(employee);

        mockMvc.perform(get("/api/employees/email/{email}", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice")));
    }

    @Test
    void getEmployeesByDepartment_returnsEmployees() throws Exception {
        Employee employee = new Employee("Alice", "alice@example.com", "IT");
        when(employeeService.getEmployeesByDepartment("IT")).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees/department/{department}", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchEmployeesByName_returnsMatches() throws Exception {
        Employee employee = new Employee("Alice Johnson", "alice@example.com", "IT");
        when(employeeService.searchEmployeesByName("alice")).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees/search").param("name", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateEmployee_returnsUpdatedEmployee() throws Exception {
        Employee employee = new Employee("Alice Updated", "alice.updated@example.com", "HR");
        employee.setId(1L);
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(employee);

        mockMvc.perform(put("/api/employees/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alice Updated",
                                  "email": "alice.updated@example.com",
                                  "department": "HR"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Updated")));
    }

    @Test
    void deleteEmployee_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 1))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Employee not found with id: 99"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
