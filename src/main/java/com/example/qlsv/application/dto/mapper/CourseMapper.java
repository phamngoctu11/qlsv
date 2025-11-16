package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.response.CourseResponse;
import com.example.qlsv.domain.model.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        SubjectMapper.class,
        SemesterMapper.class,
        LecturerMapper.class
})
public interface CourseMapper {

    // MapStruct sẽ tự động dùng các Mapper trong 'uses' để map các trường con:
    // 1. source="subject" -> target="subject" (dùng SubjectMapper)
    // 2. source="semester" -> target="semester" (dùng SemesterMapper)
    // 3. source="lecturer" -> target="lecturer" (dùng LecturerMapper)
    CourseResponse toResponse(Course course);
}