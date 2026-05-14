package com.sport_pro_be.common.aspect;

import com.sport_pro_be.common.annotation.RateLimit;
import com.sport_pro_be.exception.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Before("@annotation(rateLimit)")
    public void before(JoinPoint joinPoint, RateLimit rateLimit) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = getClientIp(request);
        String methodName = joinPoint.getSignature().toShortString();
        String key = ip + "-" + methodName;

        Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket(rateLimit));

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {} on method: {}", ip, methodName);
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }
    }

    private Bucket createNewBucket(RateLimit rateLimit) {
        Bandwidth limit = Bandwidth.classic(rateLimit.requests(), 
                Refill.greedy(rateLimit.requests(), Duration.ofSeconds(rateLimit.periodInSeconds())));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }
}
