package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.form.validator.FeedbackFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.TimeSheetConstans;
import com.aplana.timesheet.util.TimeSheetUser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Controller
public class FeedbackController {
	private static final Logger logger = LoggerFactory.getLogger(TimeSheetController.class);

	@Autowired
	private DivisionService divisionService;
	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private FeedbackFormValidator fbFormValidator;
    @Autowired
    private SendMailService sendMailService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private MessageSource messageSource;
	
	//IDs подразделения и сотрудника по умолчанию
	private Integer divisionId = 0, employeeId = 0;

	// Эти коды должны соответствовать страничке feedback.jsp
	private static final String[] FEEDBACK_TYPE_NAME_KEYS = {
		null,
        "feedback.type.newproposal",
		"feedback.type.incorrectdata",
		"feedback.type.cantsendreport",
		"feedback.type.notfoundproject",
		"feedback.type.other"
	};

    //Пользователь нажал на кнопку "Очистить" на странице feedback.jsp
	@RequestMapping(value = "/newFeedbackMessage", method = RequestMethod.POST)
	public String newFeedbackMessage() {
		return "redirect:feedback";
	}
	
	//Пользователь нажал "Отправить" на странице feedback.jsp
	@RequestMapping(value = "/feedback", method = RequestMethod.POST)
	public ModelAndView sendFeedback(@ModelAttribute("feedbackForm")FeedbackForm fbForm, BindingResult result, Locale locale) {
		//Валидируем форму
		logger.info("Processing FeedbackForm validation for employee {}", fbForm.getEmployeeId());
		fbFormValidator.validate(fbForm, result);
		//Если ошибки есть
		if (result.hasErrors()) {
			logger.info("FeedbackForm for employee {} has errors.", fbForm.getEmployeeId());
			ModelAndView mavWithErrors = new ModelAndView("feedback");
			//передаем ID подразделения и сотрудника в форму
			mavWithErrors.addObject("divId", fbForm.getDivisionId());
			mavWithErrors.addObject("empId", fbForm.getEmployeeId());
			mavWithErrors.addObject("errors", result.getAllErrors());
			mavWithErrors.addAllObjects(getListsToMAV());

			return mavWithErrors;
		}
		//Если ошибок нет
		fbForm.setFeedbackTypeName(messageSource.getMessage(FEEDBACK_TYPE_NAME_KEYS[fbForm.getFeedbackType().intValue()], null, locale));
		sendMailService.performFeedbackMailing(fbForm);
		ModelAndView mav = new ModelAndView("feedbackSent");
		//сохраняем ID подразделения и сотрудника для будущей формы
		divisionId = fbForm.getDivisionId();
		employeeId = fbForm.getEmployeeId();
		mav.addObject("feedbackForm", fbForm);
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		return mav;
	}

	//Пользователь нажал на кнопку "Отправить новое сообщение о проблеме" на странице feedbackSent.jsp
	@RequestMapping(value = "/sendNewFeedbackMessage", method = RequestMethod.POST)
	public String sendNewFeedbackMessage() {
		return "redirect:feedback";
	}
	
	//Берем ID подразделения и сотрудника, выбранных на главной странице
	//и перенаправляем  по URL без ID-ов
	/*@RequestMapping(value = "/problem/{divId}/{empId}", method = RequestMethod.GET)
	 public String defaultSettings(@PathVariable("divId") Integer divId, @PathVariable("empId") Integer empId, @ModelAttribute("problemForm")FeedbackForm pForm, BindingResult result) {
		divisionId = divId;
		employeeId = empId;
		return "redirect:/problem";
	 }   */
	
	//Основной метод GET
	@RequestMapping(value = "/feedback", method = RequestMethod.GET)
	 public ModelAndView sendReportFeedback(@ModelAttribute("feedbackForm")FeedbackForm fbForm, BindingResult result) {
			
		Properties properties = new Properties();
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(TimeSheetConstans.PROPERTY_PATH);
		} catch (FileNotFoundException ex) {
			logger.error("Can not find propety file {}", TimeSheetConstans.PROPERTY_PATH);
			logger.error("", ex);
		}
		
		String jiraIssueCreateUrl = null;
		if (fis != null) {
			try {
				properties.load(fis);
				jiraIssueCreateUrl = properties.getProperty("jira.issue.create.url");
				if(jiraIssueCreateUrl.isEmpty()) {
					jiraIssueCreateUrl = null;
				}					
			} catch (IOException ex) {
				logger.error("", ex);
			}
		}
		
		ModelAndView mav = new ModelAndView("feedback");
		mav.addObject("feedbackForm", fbForm);
		mav.addObject("jiraIssueCreateUrl", jiraIssueCreateUrl);

		TimeSheetUser securityUser = securityService.getSecurityPrincipal();
		if (securityUser != null) {
			divisionId = securityUser.getEmployee().getDivision().getId();
			employeeId = securityUser.getEmployee().getId();
		}

		mav.addObject("divId", divisionId);
		mav.addObject("empId", employeeId);
		mav.addAllObjects(getListsToMAV());
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return mav;
	 }

	/*
	 * Возвращает HashMap со значениями для заполнения списков сотрудников,
	 * проектов, пресейлов, проектных задач, типов и категорий активности на
	 * форме приложения.
	 */
	private Map<String, Object> getListsToMAV() {
		Map<String, Object> result = new HashMap<String, Object>();
		
		List<Division> divisions = divisionService.getDivisions();
		result.put("divisionList", divisions);
		result.put("employeeListJson", getEmployeeListJson(divisions));

		return result;
	}

	private String getEmployeeListJson(List<Division> divisions) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < divisions.size(); i++) {
			List<Employee> employees = employeeService.getEmployees(divisions.get(i));
			sb.append("{divId:'");
			sb.append(divisions.get(i).getId());
			sb.append("', divEmps:[");
			if (employees.size() > 0) {
				for (int j = 0; j < employees.size(); j++) {
					sb.append("{id:'");
					sb.append(employees.get(j).getId());
					sb.append("', value:'");
					sb.append(employees.get(j).getName());
					sb.append("', jobId:'");
					sb.append(employees.get(j).getJob().getId());
					sb.append("'}");
					if (j < (employees.size() - 1)) {
						sb.append(", ");
					}
				}
				sb.append("]}");
			} else { sb.append("{id:'0', value:''}]}"); }
			
			if (i < (divisions.size() - 1)) { sb.append(", "); }
		}
		sb.append("]");
		return sb.toString();
	}
}