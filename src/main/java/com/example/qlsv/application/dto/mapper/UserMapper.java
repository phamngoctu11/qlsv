package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.response.SimpleStudentResponse;
import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.domain.model.User;
import com.example.qlsv.domain.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Map các trường đặc biệt bằng hàm Java (expression)
    @Mapping(target = "role", expression = "java(roleToString(user.getRole()))")
    @Mapping(target = "firstName", expression = "java(getFirstName(user))")
    @Mapping(target = "lastName", expression = "java(getLastName(user))")
    @Mapping(target = "studentCode", expression = "java(getStudentCode(user))")
    @Mapping(target = "lecturerCode", expression = "java(getLecturerCode(user))")
    UserResponse toResponse(User user);

    // 1. Xử lý Role: Bỏ tiền tố "ROLE_" để trả về "ADMIN", "STUDENT"...
    default String roleToString(Role role) {
        return role != null ? role.name().replace("ROLE_", "") : null;
    }

    // 2. Logic lấy Student Code
    default String getStudentCode(User user) {
        // Chỉ trả về nếu Role là STUDENT và có dữ liệu
        if (user.getRole() == Role.ROLE_STUDENT && user.getStudent() != null) {
            return user.getStudent().getStudentCode();
        }
        return null; // Admin, Secretary, Lecturer -> null
    }

    // 3. Logic lấy Lecturer Code
    default String getLecturerCode(User user) {
        // Chỉ trả về nếu Role là LECTURER và có dữ liệu
        if (user.getRole() == Role.ROLE_LECTURER && user.getLecturer() != null) {
            return user.getLecturer().getLecturerCode();
        }
        return null; // Admin, Secretary, Student -> null
    }

    // 4. Helper lấy tên
    default String getFirstName(User user) {
        if (user.getStudent() != null) return user.getStudent().getFirstName();
        if (user.getLecturer() != null) return user.getLecturer().getFirstName();
        return "Admin";
    }

    default String getLastName(User user) {
        if (user.getStudent() != null) return user.getStudent().getLastName();
        if (user.getLecturer() != null) return user.getLecturer().getLastName();
        return "";
    }

    @Mapping(source = "studentCode", target = "studentCode")
    @Mapping(source = "user.email", target = "email")
    SimpleStudentResponse studentToSimpleStudentResponse(com.example.qlsv.domain.model.Student student);
}