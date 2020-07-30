package com.lexue.refresh.redis;

import java.io.Serializable;

/**
 * Created by 25610 on 2020/7/28.
 */
public class Message implements Serializable {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Message{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
