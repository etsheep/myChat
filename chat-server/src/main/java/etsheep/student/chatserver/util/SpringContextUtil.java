package etsheep.student.chatserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Created by etsheep on 2019-7-20.
 */

@Slf4j
public final class SpringContextUtil {

    private SpringContextUtil(){

    }

    // Spring应用上下文环境
    private static ApplicationContext applicationContext;

    static {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    public static<T> T getBean(String beanId){
        T bean = null;
        try{
            if (!StringUtils.isEmpty(StringUtils.trimAllWhitespace(beanId))){
                bean = (T) applicationContext.getBean(beanId);
            }
        }catch (NoSuchBeanDefinitionException e){
//            log.error("获取bean失败");
            return null;
        }
        return bean;
    }


    public static<T> T getBean(String... partName){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partName.length; i++){
            sb.append(partName[i]);
            if (i != partName.length - 1){
                sb.append(".");
            }
        }
        return getBean(sb.toString());
    }
}
