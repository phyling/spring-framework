package com.phyling.app;

import com.phyling.annotation.DBConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;

@Configuration
public class DBUtilConfig  implements ImportAware {
	private String username;
	private String password;

	@Override
	public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {
		Map<String, Object> map = importMetadata.getAnnotationAttributes(DBConfig.class.getName());
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(map);
		this.username=attributes.getString("name");
		this.password=attributes.getString("password");
	}

	public void print(){
		System.out.println(username+"----------"+ password);
	}
}
