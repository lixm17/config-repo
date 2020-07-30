package com.lexue;


import com.lexue.refresh.redis.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTest 
{
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void shouldAnswerWithTrue() throws UnsupportedEncodingException {
        for (int i = 0; i < 10; i++) {
            System.out.println("send=>"+i);
            String s="中国"+i;
            Message message = new Message();
            message.setKey("name");
            message.setValue("好");
            redisTemplate.convertAndSend("test1",message);
        }
    }

    @Test
    public void t(){
        //default,dev
        //master,branch
        String uri="http://localhost:8086/producer/dev/master";
        Environment environment = restTemplate.getForObject(uri, Environment.class);

        System.out.println(environment);
        List<PropertySource> propertySources = environment.getPropertySources();
        for (PropertySource propertySource:propertySources) {
            Map<?, ?> source = propertySource.getSource();
            for (Map.Entry<?, ?> entry:source.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key+"--->"+value);
            }
        }
    }
}
