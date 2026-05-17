package com.sport_pro_be.modules.user_address.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.user_address.constant.AddressMessageConstant;
import com.sport_pro_be.modules.user_address.dto.request.AddressRequest;
import com.sport_pro_be.modules.user_address.dto.response.AddressResponse;
import com.sport_pro_be.modules.user_address.interfaces.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class AddressController {

    private final IAddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.ok(ApiResponse.of(AddressMessageConstant.ADDRESS_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses() {
        List<AddressResponse> response = addressService.getMyAddresses();
        return ResponseEntity.ok(ApiResponse.of(null, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.of(AddressMessageConstant.ADDRESS_UPDATED, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.of(AddressMessageConstant.ADDRESS_DELETED, null));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(@PathVariable Long id) {
        AddressResponse response = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.of(AddressMessageConstant.ADDRESS_SET_DEFAULT, response));
    }
}
