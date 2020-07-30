package com.lexue.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by 25610 on 2020/7/7.
 *
 * 缓存管理器，可以配置Redis，map，MongoDB等，通过注解优雅加入缓存
 */
@Configuration
public class CacheManagers {


    /**
     * 创建redis连接对象
     * redisTemplate依赖注入时，必须指定泛型，不指定泛型的话，依赖注入的不是同一个Bean
     * @param redisConnectionFactory
     * @return
     */

    @Bean
    public RedisTemplate<String,Object> getRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> template = new RedisTemplate<String,Object>();
        // 配置连接工厂
        template.setConnectionFactory(redisConnectionFactory);
        //序列化对象（特别注意：发布的时候需要设置序列化；订阅方也需要设置序列化）
        FastJsonRedisSerializer seria = new FastJsonRedisSerializer(Object.class);
        // 值采用json序列化
        template.setValueSerializer(seria);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(seria);
        template.afterPropertiesSet();
        return template;
    }


}
