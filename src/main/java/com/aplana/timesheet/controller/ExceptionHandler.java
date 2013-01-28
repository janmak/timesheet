package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.MailSenders.Mail;
import com.aplana.timesheet.service.SendMailService;
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

    protected static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception exception)
    {
        // Отправим сообщение админам
        sendMailService.performExceptionSender(exception.getMessage() + "\n" + exception.getStackTrace());
        // Выведем в лог
        logger.error("Error message and stack trace:" + exception.getMessage() + "\n" + exception.getStackTrace());

        // Вернем сообщение для отображения
        Map<String, Object> model = new HashMap<String, Object>();
        if (exception instanceof MaxUploadSizeExceededException) {
            model.put("exceptionText", "error.feedback.maxsize");
            return new ModelAndView("redirect:feedback", model);
        } else {
            model.put("errors", "Unexpected error: " + exception.getMessage());
        }
        return new ModelAndView("exception", model);
    }


}
