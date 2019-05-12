package com.phyling.test;

import com.phyling.app.Appconfig;
import com.phyling.app.DBUtilConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
//		DBUtilConfig bean = applicationContext.getBean(DBUtilConfig.class);
//
//		bean.print();






















		List<String> features = Arrays.asList("Lambdas", "Default Method", "Stream API", "Date and Time API","LamxxbdasL");

		filter(features,(str)->str.startsWith("L"),(str)->str.endsWith("L"));


		// 新方法：
		//List<Integer> costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
		//rduce(costBeforeTax);

	}
	// filter 满足条件的元素
	private static void filter(List<String> names, Predicate<String> condition,Predicate<String> conditionn2) {
		List<String> collect = names.stream().filter(condition.or(conditionn2)).collect(Collectors.toList());
		System.out.println(collect);
	}

	// filter 满足条件的元素
	private static void rduce(List<Integer> costBeforeTax) {
		Double aDouble = costBeforeTax.stream().map(cost -> cost * (1 + 0.12)).reduce((sum, cost) -> sum + cost).get();
		System.out.println(aDouble);
	}
}

