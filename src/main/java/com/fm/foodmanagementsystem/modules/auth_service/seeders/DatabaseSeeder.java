package com.fm.foodmanagementsystem.modules.auth_service.seeders;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    RoleRepository roleRepository;
    // Bác có thể inject thêm UserRepository và PasswordEncoder vào đây
    // nếu muốn tự động tạo luôn 1 tài khoản Admin mặc định nhé!

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
    }

    private void seedRoles() {
        // Chỉ khởi tạo nếu trong bảng Role chưa có dữ liệu nào (Tránh việc mỗi lần restart app lại bị duplicate)
        if (roleRepository.count() == 0) {
            log.info("Đang khởi tạo các Role (Quyền) mặc định cho hệ thống...");

            Role adminRole = Role.builder()
                    // Tùy thuộc vào Entity Role của bác, nếu Khóa chính (Id) là String thì set ID luôn:
                    .name("ADMIN")
                    .description("Quản trị viên toàn quyền hệ thống")
                    .build();

            Role userRole = Role.builder()
                    .name("USER")
                    .description("Khách hàng đăng ký trên Mobile App")
                    .build();

            Role staffRole = Role.builder()
                    .name("STAFF")
                    .description("Nhân viên nhà hàng (Nhận đơn, chế biến)")
                    .build();

            // Lưu 1 cục vào DB
            roleRepository.saveAll(List.of(adminRole, userRole, staffRole));

            log.info("Khởi tạo Role thành công! Đã có: ADMIN, USER, STAFF.");
        } else {
            log.info("Dữ liệu Role đã tồn tại, bỏ qua bước Seeding.");
        }
    }
}