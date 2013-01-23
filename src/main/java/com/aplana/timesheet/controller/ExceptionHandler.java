package com.aplana.timesheet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ExceptionHandler implements HandlerExceptionResolver {

    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception exception)
    {
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
