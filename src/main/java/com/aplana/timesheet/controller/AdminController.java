package com.aplana.timesheet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/admin/", "/admin"})
public class AdminController {

	@RequestMapping
	public String adminPanel() {
		return "adminPanel";
	}


}
