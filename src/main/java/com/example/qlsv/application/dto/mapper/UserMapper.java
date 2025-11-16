package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.response.SimpleStudentResponse; // <-- MỚI
import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.domain.model.Lecturer;
import com.example.qlsv.domain.model.Student;
import com.example.qlsv.domain.model.User;
import com.example.qlsv.domain.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // (Các hàm toResponse, roleToString, getFirstName... giữ nguyên)
    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    @Mapping(source = "user", target = "firstName", qualifiedByName = "getFirstName")
    @Mapping(source = "user", target = "lastName", qualifiedByName = "getLastName")
    UserResponse toResponse(User user);

    // --- [MỚI] DÙNG CHO API LẤY DANH SÁCH SINH VIÊN ---
    SimpleStudentResponse studentToSimpleStudentResponse(Student student);
    // ---------------------------------------------------

    @Named("roleToString")
    default String roleToString(Role role) {
        if (role == null) return null;
        return role.name().replace("ROLE_", "");
    }

    @Named("getFirstName")
    default String getFirstName(User user) {
        if (user instanceof Student) return ((Student) user).getFirstName();
        if (user instanceof Lecturer) return ((Lecturer) user).getFirstName();
        return null;
    }

    @Named("getLastName")
    default String getLastName(User user) {
        if (user instanceof Student) return ((Student) user).getLastName();
        if (user instanceof Lecturer) return ((Lecturer) user).getLastName();
        return null;
    }
}