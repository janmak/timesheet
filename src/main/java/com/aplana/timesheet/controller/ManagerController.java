package com.aplana.timesheet.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/managertools/", "/managertools"})
public class ManagerController {
    @RequestMapping
    public String adminPanel() {
        return "managerPanel";
    }


}

