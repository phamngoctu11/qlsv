package com.example.qlsv.application.dto.mapper;

import com.example.qlsv.application.dto.response.UserResponse;
import com.example.qlsv.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Mapping thủ công để lấy tên từ bảng liên kết
    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    @Mapping(target = "firstName", expression = "java(getFirstName(user))")
    @Mapping(target = "lastName", expression = "java(getLastName(user))")
    UserResponse toResponse(User user);

    @Named("roleToString")
    default String roleToString(com.example.qlsv.domain.model.enums.Role role) {
        return role != null ? role.name().replace("ROLE_", "") : null;
    }

    // Hàm helper để lấy tên tùy theo Role (Yêu cầu User.java phải có mapping ngược student/lecturer)
    /* LƯU Ý: Trong file domain/model/User.java bạn CẦN BỎ COMMENT 2 dòng này:
       @OneToOne(mappedBy = "user") private Student student;
       @OneToOne(mappedBy = "user") private Lecturer lecturer;
    */
    default String getFirstName(User user) {
        if (user.getStudent() != null) return user.getStudent().getFirstName();
        if (user.getLecturer() != null) return user.getLecturer().getFirstName();
        return "Admin"; // Hoặc default
    }

    default String getLastName(User user) {
        if (user.getStudent() != null) return user.getStudent().getLastName();
        if (user.getLecturer() != null) return user.getLecturer().getLastName();
        return "";
    }

    // Mapper cho SimpleStudentResponse (Dùng trong danh sách lớp)
    @Mapping(source = "studentCode", target = "studentCode") // Giờ lấy trực tiếp
    @Mapping(source = "user.email", target = "email")        // Lấy email từ user
    com.example.qlsv.application.dto.response.SimpleStudentResponse studentToSimpleStudentResponse(com.example.qlsv.domain.model.Student student);
}