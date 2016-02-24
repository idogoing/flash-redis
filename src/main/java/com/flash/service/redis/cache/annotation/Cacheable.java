package com.flash.service.redis.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

	/**
	 * Returns the cached key in redis.
	 */
	public CacheKey cacheKey();

	/**
	 * returns the class type of cached object.
	 */
	public Class<?>clazz();

	/**
	 * Returns the expire time in milliseconds of the cache. Returns {@code 0}
	 * if the cache no need to be expired.
	 */
	public long expireTime();
}
