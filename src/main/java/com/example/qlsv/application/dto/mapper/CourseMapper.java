package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.mapper.LecturerMapper;
import com.example.qlsv.application.dto.mapper.SemesterMapper;
import com.example.qlsv.application.dto.mapper.SubjectMapper;
import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.domain.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SubjectMapper.class, SemesterMapper.class, LecturerMapper.class})
public interface CourseMapper {

    @Mapping(source = "lecturer", target = "lecturer") // Map object Lecturer sang SimpleLecturerResponse
    CourseResponse toResponse(Course course);
}