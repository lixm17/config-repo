package com.lexue.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 25610 on 2020/7/28.
 */
@RestController
public class TestController {
    private Logger log = org.slf4j.LoggerFactory.getLogger(TestController.class);
    //默认值
    //单例的情况下，@Value无法进行修改
    @Value("${name:jack}")
    private String username;

    @Autowired
    private Environment environment;

    @RequestMapping("/test")
    @ResponseBody
    private String testEnv(){
        log.info("---------TestController-----------"+this.hashCode());
        String name = environment.getProperty("name");
        String pwd = environment.getProperty("pwd");
        log.info(name);
        log.info(pwd);
        log.info("@Value:"+username);
        return "1";
    }
}
