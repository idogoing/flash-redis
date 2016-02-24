package com.flash.service.redis.cache.aop;

import com.flash.service.redis.cache.constant.CacheConstant;

public final class CacheableUtil {

	private CacheableUtil() {
	}

	public static boolean isCacheExpired(String cachedObjVersion, long cachedObjCreatedAt, long expireTime) {
		long now = System.currentTimeMillis();
		if (CacheConstant.VERSION.equals(cachedObjVersion)) {
			if (expireTime <= 0 || (expireTime > 0 && (now - cachedObjCreatedAt) < expireTime)) {
				return false;
			}
		}
		return true;
	}

}
