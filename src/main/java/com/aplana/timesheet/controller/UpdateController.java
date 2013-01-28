package com.aplana.timesheet.controller;

import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.EmployeeLdapService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.OQProjectSyncService;
import com.aplana.timesheet.service.ReportCheckService;
import com.aplana.timesheet.util.TimeSheetConstans;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/admin*")
public class UpdateController {

    @Autowired
    private EmployeeLdapService employeeLdapService;
    @Autowired
    private EmployeeService employeeService;

    public void setEmployeeLdapService(EmployeeLdapService employeeLdapService) {
        this.employeeLdapService = employeeLdapService;
    }

    @Autowired
    @Qualifier("reportCheckService")
    private ReportCheckService reportCheckService;

    public void setReportCheckService(ReportCheckService reportCheckService) {
        this.reportCheckService = reportCheckService;
    }

    @Autowired
    OQProjectSyncService oqProjectSyncService;

    @RequestMapping(value = "/update/ldap")
    public String ldapUsersUpdate(Model model) {
        this.employeeLdapService.synchronize();
        model.addAttribute("trace", this.employeeLdapService.getTrace().replaceAll("\n", "<br/>"));
        return "updateLDAP";
    }

    @RequestMapping(value = "/update/checkreport")
    public String checkReportUpdate(Model model) {
        this.reportCheckService.storeReportCheck();
        model.addAttribute("trace", this.reportCheckService.getTrace().replaceAll("\n", "<br/>"));
        return "checkEmails";
    }

    @RequestMapping(value = "/update/oqsync")
    public ModelAndView oqSyncUpdate(Model model) {
        oqProjectSyncService.sync();
        ModelAndView mav = new ModelAndView("oqSync");
        mav.addObject("trace", oqProjectSyncService.getTrace().replaceAll("\n", "<br/>"));
        return mav;
    }

    @RequestMapping(value = "/update/showalluser")
    public String showAllUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(TimeSheetConstans.COOKIE_SHOW_ALLUSER, "active");
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        return "redirect:/admin";
    }

    @RequestMapping(value = "/update/hidealluser")
    public String hideAllUser(HttpServletRequest request, HttpServletResponse response) {
        if (employeeService.isShowAll()) {
            Cookie cookie = new Cookie(TimeSheetConstans.COOKIE_SHOW_ALLUSER, "deactive");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:/admin";
    }

    @RequestMapping(value = "/update/properties")
    public String updateProperties(HttpServletRequest request, HttpServletResponse response) {
        TSPropertyProvider.updateProperties();

        return "redirect:/admin";
    }
}
