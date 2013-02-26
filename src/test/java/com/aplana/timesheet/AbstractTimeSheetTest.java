package com.aplana.timesheet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

/**
 * Базовый класс для юнит-тестов
 * Для создания нового юнит-теста надо унаследоваться от данного класса
 * @author aimamutdinov
 */
public abstract class AbstractTimeSheetTest extends AbstractTest {

	@Value("${test.username}")
	private String testUserName;
	@Value("${test.password}")
	private String testPassword;

	@PostConstruct
	private void postConstruct() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUserName, testPassword));
	}
}
