package com.lexue.refresh.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 25610 on 2020/7/28.
 */
public class RefreshScope implements Scope{
    private ConcurrentHashMap map=new ConcurrentHashMap();
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        if (map.containsKey(name)){
            return map.get(name);
        }
        Object object = objectFactory.getObject();
        map.put(name,object);
        return object;
    }

    @Override
    public Object remove(String name) {
        return map.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
