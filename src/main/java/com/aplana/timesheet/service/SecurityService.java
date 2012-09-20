package com.aplana.timesheet.service;

import com.aplana.timesheet.util.TimeSheetUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public TimeSheetUser getSecurityPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof TimeSheetUser)
            return (TimeSheetUser) principal;
        else
            return null;
    }

}
