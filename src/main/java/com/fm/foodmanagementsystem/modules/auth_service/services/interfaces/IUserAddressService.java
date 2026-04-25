package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserAddressRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserAddressResponse;

import java.util.List;

public interface IUserAddressService {
    List<UserAddressResponse> getMyAddresses(String userId);
    UserAddressResponse addAddress(String userId, UserAddressRequest request);
    UserAddressResponse updateAddress(String userId, Long addressId, UserAddressRequest request);
    void deleteAddress(String userId, Long addressId);
    void setDefaultAddress(String userId, Long addressId);
}
