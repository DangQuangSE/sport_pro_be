package com.sport_pro_be.modules.order.interfaces;

import com.sport_pro_be.modules.order.dto.ReturnRequest;
import com.sport_pro_be.modules.order.dto.ReturnResponse;
import com.sport_pro_be.modules.order.dto.UpdateReturnStatusRequest;
import com.sport_pro_be.modules.order.enums.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReturnService {
    ReturnResponse requestReturn(Long userId, ReturnRequest request);
    ReturnResponse updateReturnStatus(Long returnId, UpdateReturnStatusRequest request);
    Page<ReturnResponse> getAllReturns(Pageable pageable);
    ReturnResponse getReturnDetails(Long returnId);
    ReturnResponse getReturnByOrderId(Long userId, Long orderId);
}
