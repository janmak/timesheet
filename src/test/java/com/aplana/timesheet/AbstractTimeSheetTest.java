package com.aplana.timesheet;

import javax.annotation.PostConstruct;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
