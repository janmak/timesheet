package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.MailSenders.Mail;
import com.aplana.timesheet.service.SecurityService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.TimeSheetUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ExceptionHandler implements HandlerExceptionResolver {

    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeService employeeService;

    protected static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception exception)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // При MaxUploadSizeExceededException не нужно отправлять письмо админу
        if (exception instanceof MaxUploadSizeExceededException) {
            model.put("exceptionText", "error.feedback.maxsize");
            return new ModelAndView("redirect:feedback", model);
        }

        model.put("errors", "Unexpected error: " + exception.getMessage());
        // получим ФИО пользователя
        String FIO = "<не определен>";
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser != null) {
            int employeeId = securityUser.getEmployee().getId();
            FIO = employeeService.find(employeeId).getName();
        }
        // Отправим сообщение админам
        sendMailService.performExceptionSender("У пользователя " + FIO + " произошла следующая ошибка:\n" + exception
                .getMessage() + "\n" + exception.getStackTrace());
        // Выведем в лог
        logger.error("Произошла неожиданная ошибка:", exception);
        return new ModelAndView("exception", model);
    }


}
