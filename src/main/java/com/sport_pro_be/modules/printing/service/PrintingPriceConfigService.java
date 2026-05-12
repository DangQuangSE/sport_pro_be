package com.sport_pro_be.modules.printing.service;

import com.sport_pro_be.exception.AppException;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingPriceConfigService;
import com.sport_pro_be.modules.printing.repository.PrintingPriceConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintingPriceConfigService implements IPrintingPriceConfigService {

    private final PrintingPriceConfigRepository priceConfigRepository;

    @Override
    public List<PrintingPriceConfig> getAllPriceConfigs() {
        return priceConfigRepository.findAll();
    }

    @Override
    @Transactional
    public PrintingPriceConfig createPriceConfig(PrintingDto.PriceConfigRequest request) {
        PrintingPriceConfig config = PrintingPriceConfig.builder()
                .type(request.getType())
                .unitPrice(request.getUnitPrice())
                .description(request.getDescription())
                .build();
        return priceConfigRepository.save(config);
    }

    @Override
    @Transactional
    public PrintingPriceConfig updatePriceConfig(Long id, PrintingDto.PriceConfigRequest request) {
        PrintingPriceConfig config = priceConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(PrintingMessageConstant.PRICE_CONFIG_NOT_FOUND, HttpStatus.NOT_FOUND));
        
        config.setType(request.getType());
        config.setUnitPrice(request.getUnitPrice());
        config.setDescription(request.getDescription());
        
        return priceConfigRepository.save(config);
    }

    @Override
    @Transactional
    public void deletePriceConfig(Long id) {
        if (!priceConfigRepository.existsById(id)) {
            throw new AppException(PrintingMessageConstant.PRICE_CONFIG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        priceConfigRepository.deleteById(id);
    }
}
