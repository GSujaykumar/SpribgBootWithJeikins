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

import com.example.demo.dto.DepartmentRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Department;
import com.example.demo.service.DepartmentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DepartmentController.class)
@Import(GlobalExceptionHandler.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void getAllDepartments_returnsDepartments() throws Exception {
        Department department = new Department("Engineering", "Dev team");
        department.setId(1L);
        when(departmentService.getAllDepartments()).thenReturn(List.of(department));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Engineering")));
    }

    @Test
    void createDepartment_returnsCreatedDepartment() throws Exception {
        Department department = new Department("Engineering", "Dev team");
        department.setId(1L);
        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(department);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Engineering",
                                  "description": "Dev team"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Engineering")));
    }

    @Test
    void createDepartment_returnsBadRequestWhenInvalid() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "Dev team"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation failed")));
    }

    @Test
    void createDepartment_returnsConflictWhenNameExists() throws Exception {
        when(departmentService.createDepartment(any(DepartmentRequest.class)))
                .thenThrow(new DuplicateResourceException("Department already exists with name: Engineering"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Engineering",
                                  "description": "Dev team"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Engineering")));
    }

    @Test
    void getDepartmentById_returnsDepartment() throws Exception {
        Department department = new Department("Engineering", "Dev team");
        department.setId(1L);
        when(departmentService.getDepartmentById(1L)).thenReturn(department);

        mockMvc.perform(get("/api/departments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Engineering")));
    }

    @Test
    void getDepartmentById_returnsNotFound() throws Exception {
        when(departmentService.getDepartmentById(99L))
                .thenThrow(new ResourceNotFoundException("Department not found with id: 99"));

        mockMvc.perform(get("/api/departments/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchDepartments_returnsMatches() throws Exception {
        Department department = new Department("Engineering", "Dev team");
        when(departmentService.searchDepartments("eng")).thenReturn(List.of(department));

        mockMvc.perform(get("/api/departments/search").param("name", "eng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateDepartment_returnsUpdatedDepartment() throws Exception {
        Department department = new Department("Product Engineering", "Product dev");
        department.setId(1L);
        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class))).thenReturn(department);

        mockMvc.perform(put("/api/departments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Product Engineering",
                                  "description": "Product dev"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Product Engineering")));
    }

    @Test
    void deleteDepartment_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", 1))
                .andExpect(status().isNoContent());

        verify(departmentService).deleteDepartment(1L);
    }

    @Test
    void deleteDepartment_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Department not found with id: 99"))
                .when(departmentService).deleteDepartment(99L);

        mockMvc.perform(delete("/api/departments/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
