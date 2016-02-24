package com.flash.service.redis.cache.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.flash.commons.json.JsonHelper;
import com.flash.service.redis.RedisService;
import com.flash.service.redis.cache.annotation.CacheKey;
import com.flash.service.redis.cache.annotation.Cacheable;
import com.flash.service.redis.cache.annotation.CacheableField;
import com.flash.service.redis.cache.constant.CacheConstant;

/**
 * AOP that used to cache results, it works together with annotation
 * {@code Cacheable} and {@code CacheableField}. Initialized via component scan.
 */
@Aspect
@Component
@Order(10)
public class CacheableAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheableAspect.class);

	private static final String ANNOTION = "ANNOTATION";
	private static final String OBJECT = "OBJECT";

	@Resource(name = "redisService")
	RedisService redisService;

	@Pointcut("@annotation(com.flash.service.redis.cache.annotation.Cacheable)")
	public void pointcutExp() {
	}

	@Around("pointcutExp()")
	public Object aroundMethod(ProceedingJoinPoint jp) throws Throwable {
		LOGGER.debug("Cacheable aop entry.");
		MethodSignature joinPointObject = (MethodSignature) jp.getSignature();
		Method method = joinPointObject.getMethod();
		Cacheable cachable = method.getAnnotation(Cacheable.class);
		if (cachable == null) { // Should not happen.
			return jp.proceed();
		}
		Map<String, Object> cacheableField = this.getCacheableObject(jp.getArgs(), method.getParameterAnnotations());
		String field = cacheableField.get(OBJECT).toString();
		Object result = getResultFromCache(cachable, field);
		if (result == null) {
			LOGGER.debug("Try to get result from cache failed, key{} field{}", cachable.cacheKey().name(), field);
			result = jp.proceed();
			storeResultIntoCache(cachable, field, result);
		}
		LOGGER.debug("Cacheable aop exit.");
		return result;
	}

	private void storeResultIntoCache(Cacheable cachable, String field, Object result) {
		try {
			if (result != null) {
				redisService.hset(cachable.cacheKey().name(), field, result);
				redisService.hset(CacheKey.CACHE_KEY_GLOBAL_VERSION_CREATED_AT.name(),
						String.format("%s.%s", cachable.cacheKey().name(), field),
						String.format("%s,%s", CacheConstant.VERSION, System.currentTimeMillis()));
			}
		} catch (Exception e) {
			LOGGER.error("Exception caught when store result into cache, key {} field {}", cachable.cacheKey().name(),
					field);
		}
	}

	private Object getResultFromCache(Cacheable cachable, String field) {
		try {
			String cachedVersionCreatedAt = redisService.hget(CacheKey.CACHE_KEY_GLOBAL_VERSION_CREATED_AT.name(),
					String.format("%s.%s", cachable.cacheKey().name(), field));
			if (cachedVersionCreatedAt != null) {
				String[] vc = cachedVersionCreatedAt.split(",");
				if (!CacheableUtil.isCacheExpired(vc[0], Long.parseLong(vc[1]), cachable.expireTime())) {
					String cachedResult = redisService.hget(cachable.cacheKey().name(), field);
					if (StringUtils.isEmpty(cachedResult)) {
						return null;
					}
					Object result = JsonHelper.transJsonStringToObj(cachedResult, cachable.clazz());
					return result;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception caught when get result from cache, key {} field {}", cachable.cacheKey().name(),
					field);
		}
		return null;
	}

	private Map<String, Object> getCacheableObject(Object[] args, Annotation[][] annotations) {
		Map<String, Object> result = new HashMap<>();
		if (args == null || args.length == 0) {
			return result;
		}
		if (annotations == null || annotations.length == 0) {
			return result;
		}
		// 目前不支持锁多个参数，只支持锁第一个标记为CacheableField的参数
		int index = -1;
		for (int i = 0; i < annotations.length; i++) {
			for (int j = 0; j < annotations[i].length; j++) {
				if (annotations[i][j] instanceof CacheableField) {
					result.put(ANNOTION, annotations[i][j]);
					index = i;
					break;
				}
			}
			if (index != -1) {
				break;
			}
		}
		if (index != -1) {
			result.put(OBJECT, args[index]);
		}
		return result;
	}
}
