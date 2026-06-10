package com.sport_pro_be.modules.size.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeOptionRequest {
    private Long id;

    @NotBlank(message = "Size name is required")
    private String name;

    private Integer displayOrder = 0;
}
