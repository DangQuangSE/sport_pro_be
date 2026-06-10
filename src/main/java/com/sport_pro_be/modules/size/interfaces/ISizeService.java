package com.sport_pro_be.modules.size.interfaces;

import com.sport_pro_be.modules.size.dto.request.SizeGroupRequest;
import com.sport_pro_be.modules.size.dto.response.SizeGroupResponse;

import java.util.List;

public interface ISizeService {
    SizeGroupResponse createSizeGroup(SizeGroupRequest request);
    SizeGroupResponse updateSizeGroup(Long id, SizeGroupRequest request);
    void deleteSizeGroup(Long id);
    SizeGroupResponse getSizeGroupById(Long id);
    List<SizeGroupResponse> getAllSizeGroups();
}
