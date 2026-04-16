package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.auth_service.mappers.UserMapper;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.RoleRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserUpdateRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new SystemException(SystemErrorCode.USER_EXISTED);
        }

        User user = userMapper.mapToUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        if (request.roles() != null && !request.roles().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.roles());
            user.setRoles(new HashSet<>(roles));
        }

        return userMapper.mapToUserResponse(userRepository.save(user));
    }

    @Override
    @CacheEvict(value = "userDetail", key = "#id")
    public UserResponse updateUserById(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.dob() != null) user.setDob(request.dob());
        user.setGender(request.gender());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.roles() != null && !request.roles().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.roles());
            user.setRoles(new HashSet<>(roles));
        }

        return userMapper.mapToUserResponse(userRepository.save(user));
    }

    @Override
    @Cacheable(value = "userDetail", key = "#id")
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));
        return userMapper.mapToUserResponse(user);
    }

    @Override
    public UserResponse getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));
        return userMapper.mapToUserResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(userMapper::mapToUserResponse);
    }

    @Override
    @CacheEvict(value = "userDetail", key = "#id")
    public void deleteUserById(String id) {
        if (!userRepository.existsById(id)) {
            throw new SystemException(SystemErrorCode.USER_NOT_EXISTED);
        }
        userRepository.deleteById(id);
    }
}