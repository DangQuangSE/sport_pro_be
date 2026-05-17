package com.sport_pro_be.modules.order.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.order.constant.OrderMessageConstant;
import com.sport_pro_be.modules.order.dto.ReturnResponse;
import com.sport_pro_be.modules.order.dto.UpdateReturnStatusRequest;
import com.sport_pro_be.modules.order.interfaces.IReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReturnController {

    private final IReturnService returnService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReturnResponse>>> getAllReturns(Pageable pageable) {
        Page<ReturnResponse> response = returnService.getAllReturns(pageable);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ALL_ORDERS_RETRIEVED, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnResponse>> getReturnDetails(@PathVariable Long id) {
        ReturnResponse response = returnService.getReturnDetails(id);
        return ResponseEntity.ok(ApiResponse.of(null, response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReturnResponse>> updateReturnStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReturnStatusRequest request) {
        ReturnResponse response = returnService.updateReturnStatus(id, request);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ORDER_STATUS_UPDATED, response));
    }
}
