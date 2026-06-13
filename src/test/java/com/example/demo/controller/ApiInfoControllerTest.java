package com.example.demo.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiInfoController.class)
class ApiInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getApiInfo_returnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service", is("springboot-mysql-api")))
                .andExpect(jsonPath("$.database", is("MySQL")))
                .andExpect(jsonPath("$.endpoints.health", is("GET /api/health")))
                .andExpect(jsonPath("$.endpoints.employees.list", is("GET /api/employees")))
                .andExpect(jsonPath("$.endpoints.departments.list", is("GET /api/departments")));
    }
}
