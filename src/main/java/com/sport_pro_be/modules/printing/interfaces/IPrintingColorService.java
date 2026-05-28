package com.sport_pro_be.modules.printing.interfaces;

import com.sport_pro_be.modules.printing.domain.PrintingColor;
import com.sport_pro_be.modules.printing.dto.PrintingDto;

import java.util.List;

public interface IPrintingColorService {
    List<PrintingColor> getAllColors();
    List<PrintingColor> getActiveColors();
    PrintingColor createColor(PrintingDto.ColorRequest request);
    PrintingColor updateColor(Long id, PrintingDto.ColorRequest request);
    void deleteColor(Long id);
}
