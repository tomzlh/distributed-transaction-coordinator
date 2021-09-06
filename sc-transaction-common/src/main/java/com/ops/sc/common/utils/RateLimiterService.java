package com.ops.sc.common.utils;

import com.google.common.util.concurrent.RateLimiter;
import com.ops.sc.common.constant.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiterService {

    private static volatile Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    private static volatile Integer globalRate; // 全局速度


    public static synchronized void modifyRateConfig(String rateConfig) {
        try {
            // rateConfig = 10, 全局限流器模式
            int rate = Integer.parseInt(rateConfig);
            if (rate > 0) {
                globalRate = rate;
            } else {
                globalRate = null;
            }
            rateLimiterMap.clear();

        } catch (NumberFormatException e) {
            // rateConfig = {order:10, user:20} 限流器模式
            Map<String, Integer> rateConfigMap;
            if (StringUtils.isBlank(rateConfig)) {
                rateConfigMap = new ConcurrentHashMap<>();
            } else {
                rateConfigMap = JsonUtil.toMap(rateConfig);
            }
            Map<String, RateLimiter> rlMap = new ConcurrentHashMap<>();

            for (String name : rateConfigMap.keySet()) {
                int value = rateConfigMap.get(name);
                if (value > 0) {
                    final RateLimiter rl = RateLimiter.create(value);
                    rlMap.put(name, rl);
                }
            }
            rateLimiterMap = rlMap;
            globalRate = null;
        }
    }

    private static  RateLimiter getRateLimiter(String name) {
        RateLimiter result = rateLimiterMap.get(name);

        if (result != null || globalRate == null) {
            return result;
        }

        // 设定全局限流器，此时需要新建name对应的限流器并插入map
        synchronized (RateLimiterService.class) {
            if (globalRate != null) {
                RateLimiter rateLimiter = RateLimiter.create(globalRate);
                rateLimiterMap.putIfAbsent(name, rateLimiter);
            }

            result = rateLimiterMap.get(name);

        }

        return result;
    }

    public static boolean acquire(String transactionName) {
        RateLimiter rl = getRateLimiter(transactionName);
        return rl == null || rl.tryAcquire(Constants.RATELIMIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
}
