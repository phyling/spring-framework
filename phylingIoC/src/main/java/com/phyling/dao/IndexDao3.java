package com.phyling.dao;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class IndexDao3 implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object object, String beanName) throws BeansException {
		if (beanName.equals("indexDao")){
			Object bean = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Dao.class},
					(proxy, method, args) -> {
                System.out.println(23444444);
                return method.invoke(object,args);
            });

		}
		return object;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return null;
	}



}
