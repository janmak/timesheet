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

/**
 * Базовый класс для юнит-тестов
 * Для создания нового юнит-теста надо унаследоваться от данного класса
 * @author aimamutdinov
 */
@Transactional(propagation = Propagation.REQUIRED)
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@ContextConfiguration(locations = {"classpath:testApplicationContext.xml"})
public abstract class AbstractTimeSheetTest {

	@Value("${test.username}")
	private String testUserName;
	@Value("${test.password}")
	private String testPassword;

	@PostConstruct
	private void postConstruct() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUserName, testPassword));
	}
}
