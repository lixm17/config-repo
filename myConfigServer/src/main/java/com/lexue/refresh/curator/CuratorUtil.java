package com.lexue.refresh.curator;

import com.lexue.refresh.scope.RefreshScopeRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 25610 on 2020/7/28.
 * ApplicationContextAware 作用：获取spring上下文，并取得当前上下文中的所有Bean对象
 *
 */
@Component
public class CuratorUtil implements ApplicationContextAware{
    private Logger log = org.slf4j.LoggerFactory.getLogger(CuratorUtil.class);

    @Value("${zookeeper.connect}")
    private String connectStr;

    private static CuratorFramework client;

    @Value("${zookeeper.config.path}")
    private String path;

    @Value("${config.style}")
    private String style;
    @Autowired
    private Environment environment;

    private static String zkPropertyName="zookeeperSource";

    private static String scopeName="refresh";

    private static ConfigurableApplicationContext configurableApplicationContext;

    private ConcurrentHashMap map=new ConcurrentHashMap();

    private BeanDefinitionRegistry beanDefinitionRegistry;

    @PostConstruct
    public void init(){
        if (!"zookeeper".equals(style)){
            log.info("zk {} config-server enable.....",connectStr);
            return;
        }
        log.info("zk {} config-server start.....",connectStr);
        RefreshScopeRegistry refreshScopeRegistry=(RefreshScopeRegistry)configurableApplicationContext.getBean("refreshScopeRegistry");
        beanDefinitionRegistry=refreshScopeRegistry.getBeanDefinitionRegistry();
        client= CuratorFrameworkFactory.builder()
                .connectString(connectStr)
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .build();
        client.start();
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat==null){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(path,"zookeeper config".getBytes());
            }else {
                //如果config节点存在，将下面的子节点加入到spring容器中
                addChildToSpringProperty(client,path);
            }
            childNodeCache(client,path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void childNodeCache(final CuratorFramework client, String path) {

        final PathChildrenCache pathChildrenCache=new PathChildrenCache(client,path,false);
        try {
            pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    switch (event.getType()){
                        case CHILD_ADDED:
                            log.info("增加了节点 {}",data.getPath());
                            addEnv(data,client);
                            break;
                        case CHILD_REMOVED:
                            log.info("删除了节点 {}",data.getPath());
                            delEnv(data);
                            break;
                        case CHILD_UPDATED:
                            log.info("更新了节点 {}",data.getPath());
                            addEnv(data,client);
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

    private void refreshBean() {
        String[] beanDefinitionNames = configurableApplicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName:beanDefinitionNames) {
            BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanDefinitionName);
            if (scopeName.equals(beanDefinition.getScope())){
                //先删除
                configurableApplicationContext.getBeanFactory().destroyScopedBean(beanDefinitionName);
                //再实例化
//                configurableApplicationContext.getBean(beanDefinitionName);
            }
        }
    }

    private void addChildToSpringProperty(CuratorFramework client, String path) {
        //检查spring容器是否存在，不存在时创建spring容器
        if (!checkExistsSpringProperty()){
            createZkSpringProperty();
        }
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

    private boolean checkExistsSpringProperty() {
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        for (PropertySource<?> ps:propertySources) {
            if (zkPropertyName.equals(ps.getName())){
                return true;
            }
        }
        return false;
    }

    private void createZkSpringProperty() {
        //封装了application.properties里面所有的属性
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        //自定义容器存放zk里面的配置属性
        OriginTrackedMapPropertySource zkSource=new OriginTrackedMapPropertySource(zkPropertyName,map);

        propertySources.addLast(zkSource);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CuratorUtil.configurableApplicationContext=(ConfigurableApplicationContext) applicationContext;
    }


}
