package com.sport_pro_be.modules.order.dto;

import com.sport_pro_be.modules.order.enums.ReturnReason;
import com.sport_pro_be.modules.order.enums.ReturnStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ReturnResponse {
    private Long id;
    private Long orderId;
    private ReturnStatus status;
    private ReturnReason reason;
    private String reasonDetail;
    private BigDecimal refundAmount;
    private String adminNote;
    private String bankAccountInfo;
    private List<String> images;
    private LocalDateTime createdAt;
}
