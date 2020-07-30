package com.lexue.refresh;

import com.lexue.refresh.scope.RefreshScopeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 25610 on 2020/7/30.
 */
public abstract class AbstractRefreshContent implements ApplicationContextAware,InitializingBean {

    protected Logger log= LoggerFactory.getLogger(AbstractRefreshContent.class);

    private ConcurrentHashMap map=new ConcurrentHashMap();
    private static String scopeName="refresh";
    protected static String zkPropertyName="mySource";
    private BeanDefinitionRegistry beanDefinitionRegistry;
    protected static ConfigurableApplicationContext configurableApplicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {

        RefreshScopeRegistry refreshScopeRegistry=(RefreshScopeRegistry)configurableApplicationContext.getBean("refreshScopeRegistry");
        beanDefinitionRegistry=refreshScopeRegistry.getBeanDefinitionRegistry();
        //检查spring容器是否存在，不存在时创建spring容器
        if (!checkExistsSpringProperty()){
            createZkSpringProperty();
        }
        //将获取到的元素加到容器中
        init();
    }


    protected abstract void init();


    /**
     * 当有变更时刷新当前Bean
     */
    public void refreshBean() {
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

    private boolean checkExistsSpringProperty() {
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        for (PropertySource<?> ps:propertySources) {
            if (zkPropertyName.equals(ps.getName())){
                return true;
            }
        }
        return false;
    }

    /**
     * @Value和environment.getProperty("name")会从
     * List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<>()
     * 遍历，取到key立即返回；因此将自定义OriginTrackedMapPropertySource配置文件放到开头；
     * 以从自定义配置文件取得为准
     */
    private void createZkSpringProperty() {
        //封装了application.properties里面所有的属性
        MutablePropertySources propertySources = configurableApplicationContext.getEnvironment().getPropertySources();
        //自定义容器存放zk里面的配置属性
        OriginTrackedMapPropertySource zkSource=new OriginTrackedMapPropertySource(zkPropertyName,map);
        propertySources.addFirst(zkSource);
//        propertySources.addLast(zkSource);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AbstractRefreshContent.configurableApplicationContext=(ConfigurableApplicationContext)applicationContext;
    }


}
