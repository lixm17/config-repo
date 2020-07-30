package com.lexue.refresh.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by 25610 on 2020/7/30.
 */
@Component
public class EnviromentProperties {

    @Value("${spring.cloud.config.profile:default}")
    private String profile;

    @Value("${spring.cloud.config.label:master}")
    private String label;
    @Value("${spring.cloud.config.uri}")
    private String uri;

    @Autowired
    private RestTemplate restTemplate;

    public PropertySource getEnv(){
        String urL="";
        urL=uri+"myConfigServer"+"/"+profile+"/"+label;
        Environment environment = restTemplate.getForObject(urL, Environment.class);

        List<PropertySource> propertySources = environment.getPropertySources();
        if (propertySources!=null&&propertySources.size()==1){
            return propertySources.get(0);
        }
        for (PropertySource propertySource:propertySources) {
            //name=https://github.com/lixm17/config-repo/configserver/application-dev.properties]
            String name = propertySource.getName();

            if (name.endsWith(profile+".properties")){
                return propertySource;
            }
        }
        return null;
    }
}
