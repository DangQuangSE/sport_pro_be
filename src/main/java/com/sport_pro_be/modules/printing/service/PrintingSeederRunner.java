package com.sport_pro_be.modules.printing.service;

import com.sport_pro_be.modules.printing.domain.PrintingColor;
import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.enums.PrintingElementType;
import com.sport_pro_be.modules.printing.repository.PrintingColorRepository;
import com.sport_pro_be.modules.printing.repository.PrintingMaterialRepository;
import com.sport_pro_be.modules.printing.repository.PrintingPriceConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrintingSeederRunner implements CommandLineRunner {

    private final PrintingMaterialRepository materialRepository;
    private final PrintingPriceConfigRepository priceConfigRepository;
    private final PrintingColorRepository colorRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Custom Printing Configurations Database Seeding...");

        // 1. Seed Printing Materials
        if (materialRepository.count() == 0) {
            log.info("Seeding default printing materials...");
            materialRepository.saveAll(List.of(
                    PrintingMaterial.builder()
                            .name("In chuyển nhiệt")
                            .description("Bền màu, phẳng mịn, phù hợp thiết kế nhiều màu sắc phức tạp.")
                            .basePrice(BigDecimal.valueOf(30000))
                            .isActive(true)
                            .build(),
                    PrintingMaterial.builder()
                            .name("Decal phản quang")
                            .description("Màu sắc nổi bật, độ bền cao, phù hợp in tên và số áo phát sáng.")
                            .basePrice(BigDecimal.valueOf(50000))
                            .isActive(true)
                            .build()
            ));
            log.info("Successfully seeded printing materials.");
        }

        // 2. Seed Printing Price Configurations
        if (priceConfigRepository.count() == 0) {
            log.info("Seeding default printing element price configurations...");
            priceConfigRepository.saveAll(List.of(
                    PrintingPriceConfig.builder()
                            .type(PrintingElementType.TEXT)
                            .unitPrice(BigDecimal.valueOf(10000))
                            .description("Đơn giá cho mỗi dòng chữ in (ví dụ: Tên áo, số áo)")
                            .build(),
                    PrintingPriceConfig.builder()
                            .type(PrintingElementType.IMAGE)
                            .unitPrice(BigDecimal.valueOf(25000))
                            .description("Đơn giá cho mỗi hình ảnh/logo tải lên để in")
                            .build()
            ));
            log.info("Successfully seeded printing element price configurations.");
        }

        // 3. Seed Printing Colors (Isolated from product colors)
        if (colorRepository.count() == 0) {
            log.info("Seeding default isolated printing colors...");
            colorRepository.saveAll(List.of(
                    PrintingColor.builder().name("Trắng cơ bản").hexCode("#ffffff").isActive(true).build(),
                    PrintingColor.builder().name("Đen carbon").hexCode("#1a1c1f").isActive(true).build(),
                    PrintingColor.builder().name("Xanh dương thể thao").hexCode("#0058bc").isActive(true).build(),
                    PrintingColor.builder().name("Cam dạ quang").hexCode("#FF9500").isActive(true).build(),
                    PrintingColor.builder().name("Đỏ chiến thắng").hexCode("#ba1a1a").isActive(true).build(),
                    PrintingColor.builder().name("Xanh lá dạ quang").hexCode("#00b32c").isActive(true).build(),
                    PrintingColor.builder().name("Hồng dạ quang").hexCode("#e0007b").isActive(true).build(),
                    PrintingColor.builder().name("Vàng phản quang").hexCode("#ffd700").isActive(true).build()
            ));
            log.info("Successfully seeded isolated printing colors.");
        }

        log.info("Printing configurations database check/seeding completed.");
    }
}
