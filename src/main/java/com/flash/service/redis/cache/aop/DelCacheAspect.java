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

import com.flash.service.redis.RedisService;
import com.flash.service.redis.cache.annotation.CacheableField;
import com.flash.service.redis.cache.annotation.DelCache;

/**
 * 删除cache的aop
 * @author leon
 *
 */
@Aspect
@Component
@Order(11)
public class DelCacheAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(DelCacheAspect.class);

	private static final String ANNOTION = "ANNOTATION";
	private static final String OBJECT = "OBJECT";
	
	@Resource(name = "redisService")
	RedisService redisService;

	@Pointcut("@annotation(com.flash.service.redis.cache.annotation.DelCache)")
	public void pointcutExp() {
	}
	

	@Around("pointcutExp()")
	public Object aroundMethod(ProceedingJoinPoint jp) throws Throwable {
		LOGGER.debug("DelCache aop entry.");
		MethodSignature joinPointObject = (MethodSignature) jp.getSignature();
		Method method = joinPointObject.getMethod();
		DelCache cachable = method.getAnnotation(DelCache.class);
		if (cachable == null) { // Should not happen.
			return jp.proceed();
		}
		Map<String, Object> cacheableField = this.getCacheableObject(jp.getArgs(), method.getParameterAnnotations());
		String field = cacheableField.get(OBJECT).toString();
		LOGGER.debug("Try to delete cache, key is {}, field is {}",cachable.cacheKey().name(),field);
		this.redisService.hdel(cachable.cacheKey().name(), field);
		LOGGER.debug("Try to get result from cache failed, key{} field{}", cachable.cacheKey().name(), field);
		Object result = jp.proceed();
		return result;
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
