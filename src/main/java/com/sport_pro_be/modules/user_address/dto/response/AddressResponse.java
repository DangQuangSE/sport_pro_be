package com.sport_pro_be.modules.user_address.dto.response;

import com.sport_pro_be.modules.user_address.enums.AddressType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
    private Boolean isDefault;
    private AddressType type;
}
