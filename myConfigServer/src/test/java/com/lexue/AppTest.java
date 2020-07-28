package com.lexue;


import com.lexue.refresh.redis.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTest 
{
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void shouldAnswerWithTrue()
    {
        for (int i = 0; i < 10; i++) {
            System.out.println("send=>"+i);
            String s="中国";
            Message message = new Message();
            try {
                message.setContent(new String( s.getBytes("GBK") , "GBK"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            redisTemplate.convertAndSend("test1",message);
        }
    }
}
