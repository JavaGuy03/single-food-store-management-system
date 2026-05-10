package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.util.EmailUtils;
import com.fm.foodmanagementsystem.modules.auth_service.mappers.UserMapper;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.RoleRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserUpdateRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserService;
import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
    OrderRepository orderRepository;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        String email = EmailUtils.normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new SystemException(SystemErrorCode.USER_EXISTED);
        }

        User user = userMapper.mapToUser(request);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));

        if (request.roles() != null && !request.roles().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.roles());
            if (roles.size() != request.roles().size()) {
                throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
            }
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
        if (request.gender() != null) user.setGender(request.gender());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        // Đã THÁO BỎ logic update Role ở đây để tránh lỗ hổng bảo mật

        return userMapper.mapToUserResponse(userRepository.save(user));
    }

    // Hàm mới: Chỉ dùng để cập nhật Role
    @Override
    @CacheEvict(value = "userDetail", key = "#id")
    public UserResponse updateUserRoles(String id, List<String> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        if (roleNames != null && !roleNames.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleNames);
            if (roles.size() != roleNames.size()) {
                throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
            }
            user.setRoles(new HashSet<>(roles));
        } else {
            user.getRoles().clear();
        }

        return userMapper.mapToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userDetail", key = "#id")
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));
        return userMapper.mapToUserResponse(user);
    }

    // getMe() removed: UserController.getMyInfo() now calls getUserById(userId) via JWT user-id claim
    // This eliminates the only remaining email-based (getName()) lookup in the codebase

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchAndFilterUsers(search, role, pageable)
                .map(userMapper::mapToUserResponse);
    }

    @Override
    @CacheEvict(value = "userDetail", key = "#id")
    public void deleteUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        // Kiểm tra xem User có đơn hàng đang xử lý không
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.PAID,
                OrderStatus.PREPARING,
                OrderStatus.DELIVERING
        );

        // Giả định bác đã inject OrderRepository vào UserService
        boolean hasActiveOrders = orderRepository.existsByUserIdAndStatusIn(id, activeStatuses);

        if (hasActiveOrders) {
            // Ném lỗi báo cho Admin biết không thể khoá
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        user.setIsActive(false);
        userRepository.save(user);
    }
}