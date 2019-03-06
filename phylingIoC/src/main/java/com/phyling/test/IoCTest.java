package com.phyling.test;

import com.phyling.app.Appconfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * <p>description:</p>
 *
 * @author phyling 
 * @date 2019年03月06日 14:36:11 星期三
 */
public class IoCTest {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(Appconfig.class);
		applicationContext.refresh();
		Appconfig appconfig=(Appconfig)applicationContext.getBean("appconfig");
		System.out.println(appconfig);
	}
}
