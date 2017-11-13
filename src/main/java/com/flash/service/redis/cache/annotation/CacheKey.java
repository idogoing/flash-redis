package com.flash.service.redis.cache.annotation;

public enum CacheKey {
	//全局版本号和创建时间的缓存
	CACHE_KEY_GLOBAL_VERSION_CREATED_AT,
	
	//
	WECHAT_ACCOUNT_CACHE_KEY,
	
	FLASH_API_CONFIG_CACHE_KEY,
	
	//fans表的数据
	FANS_INFO_CACHE_KEY, 
	
	//website cache key
	WEBSITE_CACHE_KEY,
	
	WEBSITE_SLOW_CACHE_KEY,
	
	
	//所有智慧餐厅插件的缓存
	RESTAURANT_ALL_PLUGINS_CACHE_KEY,
	
	//用户信息缓存
	WE7_USERINFO_CACHE_KEY,
	
	//用户版权缓存
	USER_COPYWRITE_CACHE_KEY,
	
	//用户下载touch密钥
	DOWNLOAD_TOUCH_SIGN_CACHE_KEY,
	
	
	WE7_USER_CACHE_KEY,
}
