package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.dao.entity.ProjectRole;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author iziyangirov
 *
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class EmployeeLdapServiceTest extends TestCase {

    @Autowired
    EmployeeLdapService employeeLdapService;

    @Autowired
    EmployeeService employeeService;

    @Test
    public void testSetEmployeePermission() throws Exception {

        Employee emplMock = mock(Employee.class);

        ProjectRole job = mock(ProjectRole.class);
        doReturn(1).when(job).getId();
        doReturn(job).when(emplMock).getJob();
        when(emplMock.getJob().getId()).thenReturn(1);

        employeeLdapService.setEmployeePermission(emplMock);

        verify(emplMock, times(1)).setPermissions((Set<Permission>) anyObject());
    }

    @Test
    public void testSynchronize() throws Exception{
        // employeeLdapService.synchronize(); Это сложно назвать адекватным тестом. Закомментировал, потому что процедура выполняется очень долго.
    }
}