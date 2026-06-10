package com.sport_pro_be.modules.size.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeOptionResponse {
    private Long id;
    private String name;
    private Integer displayOrder;
}
