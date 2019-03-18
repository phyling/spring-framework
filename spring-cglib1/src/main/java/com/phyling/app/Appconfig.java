package com.phyling.app;

import com.phyling.service.IndexDao;
import com.phyling.service.IndexDao1;
import com.phyling.service.IndexService;
import com.phyling.service.OrderService;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan("com.phyling")
//@ImportResource("classpath:spring.xml")
public class Appconfig {
	@Bean
	public IndexDao indexDao() {
		return new IndexDao();
	}

	@Bean
	public IndexDao1 indexDao1() {
		indexDao();
		return new IndexDao1();
	}

}
