package com.sport_pro_be.modules.user_address.interfaces;

import com.sport_pro_be.modules.user_address.dto.request.AddressRequest;
import com.sport_pro_be.modules.user_address.dto.response.AddressResponse;

import java.util.List;

public interface IAddressService {
    AddressResponse createAddress(AddressRequest request);
    AddressResponse updateAddress(Long id, AddressRequest request);
    void deleteAddress(Long id);
    List<AddressResponse> getMyAddresses();
    AddressResponse setDefaultAddress(Long id);
}
