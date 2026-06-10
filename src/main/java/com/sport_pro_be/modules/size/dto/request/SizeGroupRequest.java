package com.sport_pro_be.modules.size.dto.request;

import com.sport_pro_be.modules.size.constant.SizeMessageConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeGroupRequest {
    @NotBlank(message = SizeMessageConstant.SIZE_GROUP_NAME_REQUIRED)
    private String name;

    private String description;

    @Builder.Default
    private List<SizeOptionRequest> sizes = new ArrayList<>();
}
