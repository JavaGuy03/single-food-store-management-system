package com.fm.foodmanagementsystem.modules.auth_service.mappers;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class UserMapper {
    public UserResponse mapToUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dob(user.getDob())
                .gender(user.getGender())
                .isActive(user.getIsActive())
                .roles(user.getRoles() != null ? user.getRoles().stream().map(Role::getName).toList() : Collections.emptyList())
                .build();
    }

    public User mapToUser(UserCreationRequest request){
        return User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .dob(request.dob())
                .gender(request.gender())
                .password(request.password())
                .build();
    }
}
