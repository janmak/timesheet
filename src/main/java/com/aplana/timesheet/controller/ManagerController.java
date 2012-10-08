package com.aplana.timesheet.controller;


import com.aplana.timesheet.util.TimeSheetConstans;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/managertools/", "/managertools"})
public class ManagerController {
	 private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);
	
    @RequestMapping
    public String managerPanel(Model model) {
		Properties properties = new Properties();
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(TimeSheetConstans.PROPERTY_PATH);
		} catch (FileNotFoundException ex) {
			logger.error("Can not find propety file {}", TimeSheetConstans.PROPERTY_PATH);
			logger.error("", ex);
		}
		
		String pentahoPath = null;
		if (fis != null) {
			try {
				properties.load(fis);
				pentahoPath = properties.getProperty("pentaho.url");
				if((pentahoPath != null) && pentahoPath.isEmpty()) {
					pentahoPath = null;
				}
			} catch (IOException ex) {
				logger.error("", ex);
			}
			
		}
		model.addAttribute("pentahoUrl", pentahoPath);
        return "managerPanel";
    }


}

