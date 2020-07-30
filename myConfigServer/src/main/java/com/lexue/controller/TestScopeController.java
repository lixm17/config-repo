package com.lexue.controller;

import com.lexue.service.TestService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 25610 on 2020/7/28.
 */
@RestController
//必须使用Scope
@Scope("refresh")
public class TestScopeController {
    private Logger log = org.slf4j.LoggerFactory.getLogger(TestScopeController.class);

    //单例的情况下，@Value无法进行修改
    @Value("${name:jack}")
    private String username;

    @Value("${spring.cloud.config.server.git.search-paths:config}")
    private String path;

    @Autowired
    private Environment environment;

    @Autowired
    private TestService testService;

    @RequestMapping("/testScope")
    @ResponseBody
    private String testEnv(){

        log.info("---------TestScopeController-----------"+this.hashCode());
        log.info(environment.getProperty("name"));
        log.info(environment.getProperty("spring.cloud.config.server.git.uri"));
        log.info(environment.getProperty("spring.redis.password1"));
        log.info(environment.getProperty("spring.cloud.config.server.git.search-paths"));

        log.info("@Value:"+username);
        log.info("@Value:"+path);

        log.info("---------------------------------------------");
        testService.testServiceValue();
        return "1";
    }
}
