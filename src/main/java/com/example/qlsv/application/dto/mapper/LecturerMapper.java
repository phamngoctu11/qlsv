package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.response.SimpleLecturerResponse;
import com.example.qlsv.domain.model.Lecturer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LecturerMapper {
    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "lecturerCode", target = "lecturerCode")
    SimpleLecturerResponse toSimpleResponse(Lecturer lecturer);
}