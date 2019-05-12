package com.phyling.app;


import com.phyling.annotation.DBConfig;
import com.phyling.dao.Dao;
import com.phyling.dao.IndexDao;
import com.phyling.dao.IndexDao1;

import org.springframework.context.annotation.*;
@ComponentScan({"com.phyling"})
@Configuration
@Import(DBUtilConfig.class)
@DBConfig(name = "zhangsan",password = "123")
public class Appconfig {

//	@Bean
//	public IndexDao1 indexDao1(){
//
//		return new IndexDao1();
//	}
//
//	@Bean
//	public IndexDao indexDao(){
//		indexDao1();
//		indexDao1();
//		return new IndexDao();
//	}
}
