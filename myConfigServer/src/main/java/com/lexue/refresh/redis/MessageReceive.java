package com.lexue.refresh.redis;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by 25610 on 2020/7/29.
 */
@Component
public class MessageReceive {
    private Logger log = org.slf4j.LoggerFactory.getLogger(MessageReceive.class);
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RedisRefreshContent content;

    public void getMessage(String object){

        Object deserialize = redisTemplate.getValueSerializer().deserialize(object.getBytes());
        Message message = (Message)deserialize;

        log.info("redis messageReceive key {}：value {}",message.getKey(),message.getValue());

        content.refreshSpringProperty();
        content.refreshBean();
    }
}
