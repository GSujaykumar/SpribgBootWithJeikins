package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.DepartmentRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Department;
import com.example.demo.repository.DepartmentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void getAllDepartments_returnsAllFromRepository() {
        Department department = new Department("Engineering", "Dev team");
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        assertThat(departmentService.getAllDepartments()).containsExactly(department);
    }

    @Test
    void createDepartment_savesWhenNameIsUnique() {
        DepartmentRequest request = departmentRequest("Engineering", "Dev team");
        when(departmentRepository.existsByName("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Department created = departmentService.createDepartment(request);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Engineering");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_throwsWhenNameAlreadyExists() {
        DepartmentRequest request = departmentRequest("Engineering", "Dev team");
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void getDepartmentById_returnsDepartment() {
        Department department = new Department("Engineering", "Dev team");
        department.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        assertThat(departmentService.getDepartmentById(1L)).isEqualTo(department);
    }

    @Test
    void getDepartmentById_throwsWhenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchDepartments_delegatesToRepository() {
        Department department = new Department("Engineering", "Dev team");
        when(departmentRepository.findByNameContainingIgnoreCase("eng")).thenReturn(List.of(department));

        assertThat(departmentService.searchDepartments("eng")).containsExactly(department);
    }

    @Test
    void updateDepartment_updatesExistingDepartment() {
        Department existing = new Department("Engineering", "Dev team");
        existing.setId(1L);
        DepartmentRequest request = departmentRequest("Product Engineering", "Product dev");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("Product Engineering")).thenReturn(Optional.empty());
        when(departmentRepository.save(existing)).thenReturn(existing);

        Department updated = departmentService.updateDepartment(1L, request);

        assertThat(updated.getName()).isEqualTo("Product Engineering");
        assertThat(updated.getDescription()).isEqualTo("Product dev");
    }

    @Test
    void updateDepartment_throwsWhenNameBelongsToAnotherDepartment() {
        Department existing = new Department("Engineering", "Dev team");
        existing.setId(1L);
        Department other = new Department("Human Resources", "HR");
        other.setId(2L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("Human Resources")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> departmentService.updateDepartment(1L,
                departmentRequest("Human Resources", "Dev team")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteDepartment_deletesExistingDepartment() {
        Department department = new Department("Engineering", "Dev team");
        department.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(department);
    }

    private DepartmentRequest departmentRequest(String name, String description) {
        DepartmentRequest request = new DepartmentRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }
}
