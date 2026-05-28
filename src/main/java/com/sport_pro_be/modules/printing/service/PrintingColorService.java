package com.sport_pro_be.modules.printing.service;

import com.sport_pro_be.exception.AppException;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingColor;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingColorService;
import com.sport_pro_be.modules.printing.repository.PrintingColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintingColorService implements IPrintingColorService {

    private final PrintingColorRepository colorRepository;

    @Override
    public List<PrintingColor> getAllColors() {
        return colorRepository.findAll();
    }

    @Override
    public List<PrintingColor> getActiveColors() {
        return colorRepository.findAllByIsActiveTrue();
    }

    @Override
    @Transactional
    public PrintingColor createColor(PrintingDto.ColorRequest request) {
        PrintingColor color = PrintingColor.builder()
                .name(request.getName())
                .hexCode(request.getHexCode())
                .isActive(request.getIsActive())
                .build();
        return colorRepository.save(color);
    }

    @Override
    @Transactional
    public PrintingColor updateColor(Long id, PrintingDto.ColorRequest request) {
        PrintingColor color = colorRepository.findById(id)
                .orElseThrow(() -> new AppException(PrintingMessageConstant.COLOR_NOT_FOUND, HttpStatus.NOT_FOUND));

        color.setName(request.getName());
        color.setHexCode(request.getHexCode());
        color.setIsActive(request.getIsActive());

        return colorRepository.save(color);
    }

    @Override
    @Transactional
    public void deleteColor(Long id) {
        if (!colorRepository.existsById(id)) {
            throw new AppException(PrintingMessageConstant.COLOR_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        colorRepository.deleteById(id);
    }
}
