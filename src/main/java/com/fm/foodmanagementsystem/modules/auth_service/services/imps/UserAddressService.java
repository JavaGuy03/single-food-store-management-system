package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.UserAddress;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserAddressRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserAddressRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserAddressResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserAddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAddressService implements IUserAddressService {

    UserAddressRepository addressRepository;
    UserRepository userRepository;

    @Override
    public List<UserAddressResponse> getMyAddresses(String userId) {
        return addressRepository.findAllByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserAddressResponse addAddress(String userId, UserAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        List<UserAddress> existingAddresses = addressRepository.findAllByUserId(userId);
        boolean isFirstAddress = existingAddresses.isEmpty();

        UserAddress address = UserAddress.builder()
                .user(user)
                .title(request.title())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .isDefault(isFirstAddress || Boolean.TRUE.equals(request.isDefault()))
                .build();

        if (address.getIsDefault() && !isFirstAddress) {
            resetOtherDefaults(existingAddresses);
        }

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public UserAddressResponse updateAddress(String userId, Long addressId, UserAddressRequest request) {
        UserAddress address = addressRepository.findById(addressId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        address.setTitle(request.title());
        address.setAddress(request.address());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        if (Boolean.TRUE.equals(request.isDefault()) && !address.getIsDefault()) {
            List<UserAddress> others = addressRepository.findAllByUserId(userId)
                    .stream().filter(a -> !a.getId().equals(addressId)).toList();
            resetOtherDefaults(others);
            address.setIsDefault(true);
        }

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String userId, Long addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String userId, Long addressId) {
        UserAddress targetAddress = addressRepository.findById(addressId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (!targetAddress.getIsDefault()) {
            // Reset other addresses FIRST (exclude target to avoid overwriting it)
            List<UserAddress> others = addressRepository.findAllByUserId(userId)
                    .stream().filter(a -> !a.getId().equals(addressId)).toList();
            resetOtherDefaults(others);
            targetAddress.setIsDefault(true);
            addressRepository.save(targetAddress);
        }
    }

    private void resetOtherDefaults(List<UserAddress> addresses) {
        for (UserAddress addr : addresses) {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

    private UserAddressResponse mapToResponse(UserAddress address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .title(address.getTitle())
                .address(address.getAddress())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .build();
    }
}
