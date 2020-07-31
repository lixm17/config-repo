package com.lexue.service.impl;

import com.lexue.service.TestService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by 25610 on 2020/7/30.
 */
@Service
@Scope("refresh")
public class TestServiceImpl implements TestService {
    private Logger log = org.slf4j.LoggerFactory.getLogger(TestServiceImpl.class);
    @Value("${name:jack}")
    private String name;

    @Autowired
    private Environment environment;
    @Override
    public void testServiceValue() {
        log.info("servicev@environment:"+environment.getProperty("spring.cloud.config.server.git.search-paths"));
        log.info("servicev@Value :name==>:"+name);
    }
}
