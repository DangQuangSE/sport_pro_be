package com.sport_pro_be.modules.order.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.order.constant.OrderMessageConstant;
import com.sport_pro_be.modules.order.dto.ReturnRequest;
import com.sport_pro_be.modules.order.dto.ReturnResponse;
import com.sport_pro_be.modules.order.interfaces.IReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserReturnController {

    private final IReturnService returnService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnResponse>> requestReturn(@Valid @RequestBody ReturnRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReturnResponse response = returnService.requestReturn(userId, request);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.RETURN_REQUESTED_SUCCESS, response));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<ReturnResponse>> getReturnByOrder(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReturnResponse response = returnService.getReturnByOrderId(userId, orderId);
        return ResponseEntity.ok(ApiResponse.of(null, response));
    }
}
