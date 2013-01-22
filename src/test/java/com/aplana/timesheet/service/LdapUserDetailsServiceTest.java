package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Employee;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author iziyangirov
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class LdapUserDetailsServiceTest extends TestCase {

    @Autowired
    LdapUserDetailsService ldapUserDetailsService;

    @Test
    public void testFillAuthority() throws Exception {
        List<GrantedAuthority> mockedList = spy(new ArrayList<GrantedAuthority>());
        Employee mockedEmpl = mock(Employee.class);
        when(mockedEmpl.getId()).thenReturn(1);

        ldapUserDetailsService.fillAuthority(mockedEmpl, mockedList);

        verify(mockedList, atLeastOnce()).add((GrantedAuthority) anyObject());
    }

}
