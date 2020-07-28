package com.lexue.refresh.redis;

import com.lexue.refresh.scope.RefreshScopeRegistry;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 25610 on 2020/7/28.
 */
@Component
public class RedisUtil implements ApplicationContextAware {
    private Logger log = org.slf4j.LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${config.style}")
    private String style;

    private static String zkPropertyName="redisSource";

    private static String scopeName="refresh";

    private static ConfigurableApplicationContext configurableApplicationContext;

    private ConcurrentHashMap map=new ConcurrentHashMap();

    private BeanDefinitionRegistry beanDefinitionRegistry;

    private static ConfigurableApplicationContext applicationContext;
    @PostConstruct
    public void init(){
        if (!"redis".equals(style)){
            log.info("redis config-server enable.....");
            return;
        }
        log.info("redis config-server start.....");


    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RedisUtil.applicationContext=(ConfigurableApplicationContext) applicationContext;
    }
}
