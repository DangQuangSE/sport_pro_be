package com.sport_pro_be.modules.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import com.sport_pro_be.modules.audit.domain.AuditLog;
import com.sport_pro_be.modules.audit.service.AuditLogService;
import com.sport_pro_be.modules.auth.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(loggable)")
    public Object audit(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        Object result = null;
        String status = "SUCCESS";
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            status = "FAIL";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            try {
                captureLog(joinPoint, loggable, status, errorMessage);
            } catch (Exception e) {
                log.error("Error capturing audit log: {}", e.getMessage());
            }
        }
    }

    private void captureLog(ProceedingJoinPoint joinPoint, Loggable loggable, String status, String errorMessage) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        User currentUser = null;
        try {
            currentUser = SecurityUtils.getCurrentUser();
        } catch (Exception e) {
            // Might be anonymous or background task
        }

        String ipAddress = getClientIp(request);
        String payload = getPayload(joinPoint);

        AuditLog auditLog = AuditLog.builder()
                .userId(currentUser != null ? currentUser.getId() : null)
                .email(currentUser != null ? currentUser.getEmail() : "anonymous")
                .action(loggable.action())
                .module(loggable.module())
                .payload(payload)
                .status(status)
                .ipAddress(ipAddress)
                .errorMessage(errorMessage)
                .build();

        auditLogService.saveLog(auditLog);
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    private String getPayload(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0)
                return null;

            Map<String, Object> params = new HashMap<>();
            String[] parameterNames = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature())
                    .getParameterNames();

            for (int i = 0; i < args.length; i++) {
                String key = parameterNames[i];
                Object value = args[i];

                // Simple masking for sensitive fields
                if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret")
                        || key.toLowerCase().contains("token")) {
                    value = "******";
                }
                params.put(key, value);
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "Error capturing payload: " + e.getMessage();
        }
    }
}
