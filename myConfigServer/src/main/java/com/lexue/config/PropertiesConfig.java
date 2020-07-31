package com.lexue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by 25610 on 2020/7/31.
 * 使用通用的类进行集中获取配置数据，无问题
 */
@Component
@Scope("refresh")
public class PropertiesConfig {
    @Value("${name:jack}")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
