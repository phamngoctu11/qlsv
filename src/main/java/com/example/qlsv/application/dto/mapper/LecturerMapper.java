package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.response.SimpleLecturerResponse;
import com.example.qlsv.domain.model.Lecturer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LecturerMapper {

    SimpleLecturerResponse toSimpleResponse(Lecturer lecturer);
}