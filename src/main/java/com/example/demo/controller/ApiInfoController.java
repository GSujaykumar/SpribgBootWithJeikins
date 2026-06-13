package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", "springboot-mysql-api");
        info.put("database", "MySQL");
        info.put("endpoints", Map.of(
                "health", "GET /api/health",
                "employees", Map.of(
                        "list", "GET /api/employees",
                        "getById", "GET /api/employees/{id}",
                        "getByEmail", "GET /api/employees/email/{email}",
                        "byDepartment", "GET /api/employees/department/{department}",
                        "search", "GET /api/employees/search?name={name}",
                        "create", "POST /api/employees",
                        "update", "PUT /api/employees/{id}",
                        "delete", "DELETE /api/employees/{id}"
                ),
                "departments", Map.of(
                        "list", "GET /api/departments",
                        "getById", "GET /api/departments/{id}",
                        "search", "GET /api/departments/search?name={name}",
                        "create", "POST /api/departments",
                        "update", "PUT /api/departments/{id}",
                        "delete", "DELETE /api/departments/{id}"
                )
        ));
        return info;
    }
}
