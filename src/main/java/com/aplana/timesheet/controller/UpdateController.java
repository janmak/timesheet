package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.EmployeeLdapService;
import com.aplana.timesheet.service.OQProjectSyncService;
import com.aplana.timesheet.service.ReportCheckService;
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
	private DivisionService divisionService;

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
	
	@RequestMapping(value = "/update/ldapDivisions")
    public String ldapDivisionsUpdate(Model model) {
		StringBuffer sb = this.divisionService.synchronize();
		model.addAttribute("trace", sb.toString().replaceAll("\n", "<br/>"));
        return "updateDivisionsLDAP";
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

}
