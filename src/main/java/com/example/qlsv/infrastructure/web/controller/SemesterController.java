package com.example.qlsv.infrastructure.web.controller;

import com.example.qlsv.application.dto.SemesterDTO;
import com.example.qlsv.application.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ Admin được quản lý học kỳ
public class SemesterController {

    private final SemesterService semesterService;

    @PostMapping
    public ResponseEntity<SemesterDTO> createSemester(@Valid @RequestBody SemesterDTO semesterDTO) {
        SemesterDTO createdSemester = semesterService.createSemester(semesterDTO);
        return new ResponseEntity<>(createdSemester, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SemesterDTO> getSemesterById(@PathVariable Long id) {
        SemesterDTO semester = semesterService.getSemesterById(id);
        return ResponseEntity.ok(semester);
    }

    @GetMapping
    public ResponseEntity<List<SemesterDTO>> getAllSemesters() {
        List<SemesterDTO> semesters = semesterService.getAllSemesters();
        return ResponseEntity.ok(semesters);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SemesterDTO> updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterDTO semesterDTO
    ) {
        SemesterDTO updatedSemester = semesterService.updateSemester(id, semesterDTO);
        return ResponseEntity.ok(updatedSemester);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.noContent().build();
    }
}