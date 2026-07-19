package com.sport_pro_be.modules.printing.service;

import com.sport_pro_be.exception.AppException;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.enums.PrintingElementType;
import com.sport_pro_be.modules.printing.interfaces.IPrintingPriceConfigService;
import com.sport_pro_be.modules.printing.repository.PrintingPriceConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintingPriceConfigService implements IPrintingPriceConfigService {

    private static final String CACHE_NAME = "printing_price_configs";

    private final PrintingPriceConfigRepository priceConfigRepository;

    @Override
    public List<PrintingPriceConfig> getAllPriceConfigs() {
        return priceConfigRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
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
    @CacheEvict(value = CACHE_NAME, allEntries = true)
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
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deletePriceConfig(Long id) {
        if (!priceConfigRepository.existsById(id)) {
            throw new AppException(PrintingMessageConstant.PRICE_CONFIG_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        priceConfigRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "#type")
    public BigDecimal getUnitPrice(PrintingElementType type) {
        PrintingPriceConfig config = priceConfigRepository.findByType(type)
                .orElseThrow(() -> new AppException(PrintingMessageConstant.PRICE_CONFIG_NOT_FOUND, HttpStatus.NOT_FOUND));
        return config.getUnitPrice();
    }
}
