package com.fm.foodmanagementsystem.modules.auth_service.models.dtos;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PendingUserDto {
    String email;
    String password;
    String firstName;
    String lastName;
    String phone;
    LocalDate dob;
    List<String> roles;
}
