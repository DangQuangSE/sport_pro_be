package com.sport_pro_be.modules.order.dto;

import com.sport_pro_be.modules.order.enums.ReturnStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReturnStatusRequest {

    @NotNull(message = com.sport_pro_be.modules.order.constant.OrderMessageConstant.STATUS_REQUIRED)
    private ReturnStatus status;

    private String adminNote;
}
