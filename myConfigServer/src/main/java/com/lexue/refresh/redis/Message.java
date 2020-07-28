package com.lexue.refresh.redis;

import java.io.Serializable;

/**
 * Created by 25610 on 2020/7/28.
 */
public class Message implements Serializable {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
