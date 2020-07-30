package com.lexue.refresh.redis;

import com.lexue.refresh.AbstractRefreshContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 25610 on 2020/7/28.
 */
@Component
public class RedisRefreshContent extends AbstractRefreshContent{

    @Value("${config.style}")
    private String style;

    @Autowired
    private EnviromentProperties environ;


    @Override
    protected void init() {
        if (!"redis".equals(style)) {
            log.info("redis  config-server enable.....");
            return;
        }
        log.info("redis config-server start.....");
        refreshSpringProperty();
    }


    public void refreshSpringProperty() {
        //重新加载git数据
        PropertySource env = environ.getEnv();
        if (env==null){
            log.info("git server properties is null");
            return;
        }
        Map<?, ?> source = env.getSource();

        //把/config下的子节点添加到zk的PropertySource对象中
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        org.springframework.core.env.PropertySource<?> propertySource = propertySources.get(zkPropertyName);
        ConcurrentHashMap zkmap = (ConcurrentHashMap)propertySource.getSource();

        zkmap.putAll(source);
    }
}
