package com.flash.service.redis.cache.aop;

import com.flash.service.redis.cache.constant.CacheConstant;

public class CacheableObject {

	private String version4Cache;

	private Long createdAt4Cache;

	public String getVersion4Cache() {
		return version4Cache;
	}

	public void setVersion4Cache(String version4Cache) {
		this.version4Cache = version4Cache;
	}

	public Long getCreatedAt4Cache() {
		return createdAt4Cache;
	}

	public void setCreatedAt4Cache(Long createdAt4Cache) {
		this.createdAt4Cache = createdAt4Cache;
	}
	
	public void initVersionCreatedAt(){
		this.version4Cache = CacheConstant.VERSION;
		this.createdAt4Cache = System.currentTimeMillis();
	}

}
