package com.aplana.timesheet.service;

import com.aplana.timesheet.controller.TimeSheetController;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.enums.Permissions;
import com.aplana.timesheet.util.TimeSheetUser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Service;

import static com.aplana.timesheet.enums.Permissions.*;

@Service("myLdapUserDetailsService")
public class LdapUserDetailsService implements UserDetailsContextMapper {

    private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

    @Autowired
    private EmployeeDAO employeeDAO;


    @Autowired
    private EmployeeLdapService employeeLdapService;

    public void fillAuthority(Employee employee, List<GrantedAuthority> list) {
        List<Permission> permissionList = employeePermissonsDAO.getEmployeePermissions(employee.getId());
        for (Permission permission : permissionList){
            switch ( Permissions.getById( permission.getId() )) {
                case RERPORTS_PERMISSION: {
                    list.add( new SimpleGrantedAuthority( "ROLE_MANAGER" ) );
                    list.add( new SimpleGrantedAuthority( "ROLE_USER" ) );
                    break;
                }
                case ADMIN_PERMISSION: {
                    list.add( new SimpleGrantedAuthority( "ROLE_ADMIN" ) );
                    list.add( new SimpleGrantedAuthority( "ROLE_USER" ) );
                    break;
                }
                default:
                    list.add( new SimpleGrantedAuthority( "ROLE_USER" ) );
            }
        }
    }

    public UserDetails mapUserFromContext(DirContextOperations dirContextOperations, String s, Collection<? extends GrantedAuthority> grantedAuthorities) {
        try {
            String email = dirContextOperations.getStringAttribute("mail");

            Employee employee = employeeDAO.findByEmail(email);

            List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            if (employee != null) {
                //если на текущий момент employee считается уволенным
                if (employee.isDisabled(null)) {
                    logger.warn("Employee is archived for email {}", email);
                    throw new BadCredentialsException("Ваша учетная запись отключена");
                }
                fillAuthority(employee, list);
            } else {
                logger.warn("Employee add in DB {}", email);
                String errors = employeeLdapService.synchronizeOneEmployee(email);
                if (errors != null) {
                    if ( ! errors.equals( "" ) ) {
                        String errorsSub = errors.substring(0, errors.lastIndexOf(','));

                        throw new BadCredentialsException("Авторизация выполнена успешно, но не удалось определить следующие параметры: <br>" + errorsSub);
                    } else {
                        throw new BadCredentialsException("Авторизация выполнена успешно, пользователь добавлен в БД<br> Авторизуйтесь еще раз");
                    }
                } else
                    throw new BadCredentialsException("В LDAP что-то изменилось. попробуйте еще раз");
            }

            return new TimeSheetUser(employee, list);
        } catch (RuntimeException e) {
            if (!(e instanceof AuthenticationException)) {
                logger.error("Неопознанная ошибка при авторизации", e);
                throw new AccountExpiredException("Неопознанная ошибка при авторизации", e);
            } else
                throw e;
        }
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

}
