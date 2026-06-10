package com.sport_pro_be.modules.size.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeGroupResponse {
    private Long id;
    private String name;
    private String description;
    private List<SizeOptionResponse> sizes;
}
