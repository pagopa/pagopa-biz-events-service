package it.gov.pagopa.bizeventsservice.util;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheManager caffeineCacheManager;

    public CacheService(CacheManager caffeineCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
    }


    /**
     * Evict from cache all the noticeList entries related to the given tax code
     *
     * @param taxCode the tax code
     */
    public void evictNoticeListByTaxCode(String taxCode) {
        CaffeineCache springCache = (CaffeineCache) caffeineCacheManager.getCache("noticeList");
        if (springCache == null) return;

        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = springCache.getNativeCache();

        nativeCache.asMap().keySet().removeIf(key -> {
            String keyString = key.toString();
            return keyString.contains(taxCode);
        });
    }
}
