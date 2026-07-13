package com.ldy.ldyaicodemother.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 在 @Configuration 的 @Bean 方法执行前，提前初始化 SpringContextUtil
 */
public interface SpringContextInjector extends ApplicationContextAware {

    @Override
    default void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.initApplicationContext(applicationContext);
    }
}
