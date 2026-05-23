package com.sport_pro_be.modules.color.service;

import com.sport_pro_be.modules.color.domain.Color;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.color.repository.ColorRepository;
import com.sport_pro_be.modules.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class ColorMigrationRunner implements CommandLineRunner {

    private final ProductVariantRepository productVariantRepository;
    private final ColorRepository colorRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking for any un-migrated product variant colors...");

        List<ProductVariant> variants = productVariantRepository.findAll();
        boolean migratedAny = false;

        for (ProductVariant variant : variants) {
            // If color (the normalized relationship) is not set, but colorOld (the old raw text string) is present
            if (variant.getColor() == null && variant.getColorOld() != null && !variant.getColorOld().trim().isEmpty()) {
                String colorName = variant.getColorOld().trim();
                
                // Capitalize the first letter for consistency (e.g. "red" -> "Red")
                if (colorName.length() > 0) {
                    colorName = colorName.substring(0, 1).toUpperCase() + colorName.substring(1);
                }

                // Check if color already exists in colors table
                String finalColorName = colorName;
                Color colorEntity = colorRepository.findByNameIgnoreCase(finalColorName)
                        .orElseGet(() -> {
                            String hexCode = getRandomHexColor();
                            log.info("Creating new Color entity for raw color '{}' with hex code '{}'", finalColorName, hexCode);
                            Color newColor = Color.builder()
                                    .name(finalColorName)
                                    .hexCode(hexCode)
                                    .build();
                            return colorRepository.save(newColor);
                        });

                // Link variant to Color entity
                variant.setColor(colorEntity);
                productVariantRepository.save(variant);
                log.info("Successfully migrated variant SKU '{}' to Color '{}'", variant.getSku(), colorEntity.getName());
                migratedAny = true;
            }
        }

        if (migratedAny) {
            log.info("Color data migration completed successfully!");
        } else {
            log.info("No un-migrated product variant colors found. All set!");
        }
    }

    private String getRandomHexColor() {
        // Pre-defined slate/premium colors to assign to placeholders
        String[] premiumColors = {"#FF1E27", "#1E90FF", "#32CD32", "#FFD700", "#FF69B4", "#8A2BE2", "#00FFFF", "#FF8C00", "#00FF7F"};
        Random random = new Random();
        return premiumColors[random.nextInt(premiumColors.length)];
    }
}
