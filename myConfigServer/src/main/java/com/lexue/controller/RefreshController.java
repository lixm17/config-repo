package com.lexue.controller;

import com.lexue.refresh.redis.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 25610 on 2020/7/29.
 * git 服务器有变动时主动调用此接口通知redis更新配置
 * 或者手动调用此接口做更新更新 http://ip:port/refresh
 */
@RestController
public class RefreshController {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @ResponseBody
    @RequestMapping("/refresh")
    public String refresh(){
        Message message = new Message();
        message.setKey("git server");
        message.setValue("update");
        redisTemplate.convertAndSend("configUpdate",message);
        return "ok";
    }
}
