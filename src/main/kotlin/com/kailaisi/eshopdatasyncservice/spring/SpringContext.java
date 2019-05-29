package com.kailaisi.eshopdatasyncservice.spring;

import org.springframework.context.ApplicationContext;

/**
 * description:spring的上下文
 * author: wu
 * created on: 2018/8/16 22:24
 */
public class SpringContext {
  private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContext.applicationContext = applicationContext;
    }
}
