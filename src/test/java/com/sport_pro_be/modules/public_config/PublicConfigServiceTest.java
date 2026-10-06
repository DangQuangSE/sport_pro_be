package com.sport_pro_be.modules.public_config;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.public_config.domain.ConfigType;
import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import com.sport_pro_be.modules.public_config.domain.SettingChangeAudit;
import com.sport_pro_be.modules.public_config.domain.SettingScope;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.repository.PublicConfigRepository;
import com.sport_pro_be.modules.public_config.repository.SettingChangeAuditRepository;
import com.sport_pro_be.modules.public_config.service.PublicConfigService;
import com.sport_pro_be.modules.public_config.service.SettingDefinitionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicConfigServiceTest {

    @Mock
    private PublicConfigRepository publicConfigRepository;

    @Mock
    private SettingChangeAuditRepository settingChangeAuditRepository;

    private PublicConfigService service;

    @BeforeEach
    void setUp() {
        service = new PublicConfigService(publicConfigRepository, settingChangeAuditRepository,
                new SettingDefinitionRegistry());
    }

    @Test
    void publicReadsReturnOnlyActivePublicSettings() {
        PublicConfig config = config("contact.zalo", "https://zalo.me/example", SettingScope.PUBLIC, true);
        when(publicConfigRepository.findAllByScopeAndActiveTrueOrderByCategoryAscConfigKeyAsc(SettingScope.PUBLIC))
                .thenReturn(List.of(config));

        assertEquals(1, service.getAllConfigs().size());
        verify(publicConfigRepository)
                .findAllByScopeAndActiveTrueOrderByCategoryAscConfigKeyAsc(SettingScope.PUBLIC);
    }

    @Test
    void staleVersionIsRejectedBeforeMutation() {
        PublicConfig config = config("checkout.tax.rate", "10", SettingScope.INTERNAL, true);
        config.setVersion(3L);
        when(publicConfigRepository.findByConfigKey("checkout.tax.rate")).thenReturn(Optional.of(config));

        PublicConfigUpdateRequest request = PublicConfigUpdateRequest.builder()
                .configValue("12")
                .expectedVersion(2L)
                .build();

        assertThrows(ConflictException.class, () -> service.updateSetting("checkout.tax.rate", request));
    }

    @Test
    void validUpdateWritesBeforeAfterAudit() {
        PublicConfig config = config("checkout.tax.rate", "10", SettingScope.INTERNAL, true);
        config.setVersion(1L);
        when(publicConfigRepository.findByConfigKey("checkout.tax.rate")).thenReturn(Optional.of(config));
        when(publicConfigRepository.saveAndFlush(config)).thenReturn(config);

        PublicConfigUpdateRequest request = PublicConfigUpdateRequest.builder()
                .configValue("12")
                .expectedVersion(1L)
                .build();

        service.updateSetting("checkout.tax.rate", request);

        ArgumentCaptor<SettingChangeAudit> auditCaptor = ArgumentCaptor.forClass(SettingChangeAudit.class);
        verify(settingChangeAuditRepository).save(auditCaptor.capture());
        assertEquals("checkout.tax.rate", auditCaptor.getValue().getSettingKey());
        assertTrue(auditCaptor.getValue().getBeforeValue().startsWith("valueHash="));
        assertTrue(auditCaptor.getValue().getAfterValue().startsWith("valueHash="));
        assertTrue(!auditCaptor.getValue().getBeforeValue().contains("10"));
        assertTrue(!auditCaptor.getValue().getAfterValue().contains("12"));
    }

    @Test
    void secretLikeKeysAreRejected() {
        PublicConfigRequest request = PublicConfigRequest.builder()
                .configKey("payos.client_secret")
                .configValue("do-not-store")
                .configType(ConfigType.TEXT)
                .build();

        assertThrows(BadRequestException.class, () -> service.createConfig(request));
    }

    @Test
    void publicLookupDoesNotExposeInternalSetting() {
        when(publicConfigRepository.findByConfigKeyAndScopeAndActiveTrue("checkout.tax.rate", SettingScope.PUBLIC))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getConfigByKey("checkout.tax.rate"));
    }

    @Test
    void checkoutDefinitionRejectsWrongType() {
        PublicConfigRequest request = PublicConfigRequest.builder()
                .configKey("checkout.tax.rate")
                .configValue("10")
                .configType(ConfigType.TEXT)
                .build();

        assertThrows(BadRequestException.class, () -> service.createConfig(request));
    }

    @Test
    void arbitraryPublicSettingIsRejected() {
        PublicConfigRequest request = PublicConfigRequest.builder()
                .configKey("homepage.internal_note")
                .configValue("should not be public")
                .configType(ConfigType.TEXT)
                .scope(SettingScope.PUBLIC)
                .active(true)
                .build();

        assertThrows(BadRequestException.class, () -> service.createConfig(request));
    }

    @Test
    void compatibilityUpdateStillRequiresVersion() {
        PublicConfigUpdateRequest request = PublicConfigUpdateRequest.builder()
                .configValue("12")
                .build();

        assertThrows(BadRequestException.class, () -> service.updateConfig("checkout.tax.rate", request));
    }

    private PublicConfig config(String key, String value, SettingScope scope, boolean active) {
        return PublicConfig.builder()
                .configKey(key)
                .configValue(value)
                .configType(key.startsWith("checkout.tax.rate") ? ConfigType.NUMBER : ConfigType.TEXT)
                .scope(scope)
                .active(active)
                .category("CHECKOUT")
                .unit(key.startsWith("checkout.tax.rate") ? "PERCENT" : null)
                .validationRules(key.startsWith("checkout.tax.rate") ? "min=0;max=100" : null)
                .version(1L)
                .build();
    }
}
