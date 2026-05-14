package com.sport_pro_be.modules.order.dto;

import com.sport_pro_be.modules.order.enums.ReturnReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReturnRequest {

    @NotNull(message = com.sport_pro_be.modules.order.constant.OrderMessageConstant.ORDER_ID_REQUIRED)
    private Long orderId;

    @NotNull(message = com.sport_pro_be.modules.order.constant.OrderMessageConstant.REASON_REQUIRED)
    private ReturnReason reason;

    private String reasonDetail;

    @NotBlank(message = com.sport_pro_be.modules.order.constant.OrderMessageConstant.BANK_INFO_REQUIRED)
    private String bankAccountInfo;

    private List<String> images;
}
