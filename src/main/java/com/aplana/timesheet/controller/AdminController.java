package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = {"/admin/", "/admin"})
public class AdminController {
    @Autowired
    private EmployeeService employeeService;

    @RequestMapping
    public ModelAndView adminPanel() {
        ModelAndView mav = new ModelAndView("adminPanel");
        mav.addObject("showalluser", employeeService.isShowAll());
        return mav;
    }
}
