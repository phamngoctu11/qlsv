package com.example.qlsv.domain.model;
import com.example.qlsv.domain.model.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "admins")
public class Admin extends User {

    // (Hiện tại Admin không cần trường nào đặc biệt,
    // nhưng chúng ta vẫn cần class này để kế thừa)

    /**
     * Constructor để tạo nhanh tài khoản Admin.
     */
    public Admin(String username, String password, String email) {
        // Gọi constructor của class cha (User)
        super(username, password, email, Role.ROLE_ADMIN);
    }
}