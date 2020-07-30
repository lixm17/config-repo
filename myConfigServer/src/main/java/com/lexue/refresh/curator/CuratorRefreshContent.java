package com.lexue.refresh.curator;

import com.lexue.refresh.AbstractRefreshContent;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 25610 on 2020/7/30.
 */
@Component
public class CuratorRefreshContent extends AbstractRefreshContent {

    @Value("${zookeeper.connect}")
    private String connectStr;

    private static CuratorFramework client;

    @Value("${zookeeper.config.path}")
    private String path;

    @Value("${config.style}")
    private String style;


    @Override
    protected void init() {
        if (!"zookeeper".equals(style)) {
            log.info("zk {} config-server enable.....", connectStr);
            return;
        }
        log.info("zk {} config-server start.....", connectStr);
        client = CuratorFrameworkFactory.builder()
                .connectString(connectStr)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(path, "zookeeper config".getBytes());
            } else {
                //如果config节点存在，将下面的子节点加入到spring容器中
                addZKChildToSpringProperty(client, path);
            }
            childNodeCache(client, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addZKChildToSpringProperty(CuratorFramework client, String path) {
        //把/config下的子节点添加到zk的PropertySource对象中
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        PropertySource<?> propertySource = propertySources.get(zkPropertyName);
        ConcurrentHashMap zkmap = (ConcurrentHashMap)propertySource.getSource();

        try {
            List<String> strings = client.getChildren().forPath(path);
            for (String s:strings) {
                zkmap.put(s,client.getData().forPath(path+"/"+s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void childNodeCache(final CuratorFramework client, String path) {

        final PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, false);
        try {
            pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            log.info("增加了节点 {}", data.getPath());
                            addEnv(data, client);
                            break;
                        case CHILD_REMOVED:
                            log.info("删除了节点 {}", data.getPath());
                            delEnv(data);
                            break;
                        case CHILD_UPDATED:
                            log.info("更新了节点 {}", data.getPath());
                            addEnv(data, client);
                        default:
                            break;
                    }
                    refreshBean();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void delEnv(ChildData data) {
        String childPath = data.getPath();
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        for (PropertySource<?> ps:propertySources){
            if (zkPropertyName.equals(ps.getName())){
                Object source = ps.getSource();
                ConcurrentHashMap chm=null;
                if (source instanceof ConcurrentHashMap){
                    chm=(ConcurrentHashMap)source;
                }else{
                    OriginTrackedMapPropertySource ops=(OriginTrackedMapPropertySource)ps.getSource();
                    chm=(ConcurrentHashMap)ops.getSource();
                }
                chm.remove(childPath.substring(path.length()+1));
            }
        }
    }

    private void addEnv(ChildData data, CuratorFramework client) {
        String childPath = data.getPath();
        String childData=null;
        try {
            childData=new String(client.getData().forPath(childPath));
            log.info("=>"+childData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        for (PropertySource<?> ps:propertySources){
            if (zkPropertyName.equals(ps.getName())){
                Object source = ps.getSource();
                ConcurrentHashMap chm=null;
                if (source instanceof ConcurrentHashMap){
                    chm=(ConcurrentHashMap)source;
                }else{
                    OriginTrackedMapPropertySource ops=(OriginTrackedMapPropertySource)ps.getSource();
                    chm=(ConcurrentHashMap)ops.getSource();
                }

                chm.put(childPath.substring(path.length()+1),childData);
            }
        }
    }
}
