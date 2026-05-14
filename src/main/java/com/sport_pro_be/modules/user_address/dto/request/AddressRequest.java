package com.sport_pro_be.modules.user_address.dto.request;

import com.sport_pro_be.modules.user_address.constant.AddressMessageConstant;
import com.sport_pro_be.modules.user_address.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = AddressMessageConstant.RECEIVER_NAME_REQUIRED)
    private String receiverName;

    @NotBlank(message = AddressMessageConstant.PHONE_NUMBER_REQUIRED)
    private String phoneNumber;

    @NotBlank(message = AddressMessageConstant.PROVINCE_REQUIRED)
    private String province;

    @NotBlank(message = AddressMessageConstant.DISTRICT_REQUIRED)
    private String district;

    @NotBlank(message = AddressMessageConstant.WARD_REQUIRED)
    private String ward;

    @NotBlank(message = AddressMessageConstant.DETAIL_ADDRESS_REQUIRED)
    private String detailAddress;

    @Builder.Default
    private Boolean isDefault = false;

    @NotNull(message = AddressMessageConstant.ADDRESS_TYPE_REQUIRED)
    private AddressType type;
}
