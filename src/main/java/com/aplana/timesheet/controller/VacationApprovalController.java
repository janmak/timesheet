package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.form.VacationApprovalForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SecurityService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import com.aplana.timesheet.util.TimeSheetUser;
import javassist.bytecode.annotation.BooleanMemberValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author iziyangriov
 */

@Controller
public class VacationApprovalController {

    @Autowired
    private VacationApprovalService vacationApprovalService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private SendMailService sendMailService;

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalController.class);

    final String NOT_ACCEPTED_YET = "%s, просьба принять решение по \"%s\" сотрудника %s из" +
            " г. %s на период с %s - %s. %s";
    final String ACCEPTED = "Запрос \"%s\" сотрудника %s из г. %s на период с %s - %s %s.";
    final String ACCEPTANCE = "согласован";
    final String REFUSE = "не согласован";
    final String DATE_FORMAT = "dd.MM.yyyy";
    final String BAD_REQUEST = "Неверный запрос!";
    final String LOGGER_MESSAGE = "\n    UserIP: %s\n    UserID: %s\n    UserFIO: %s";
    final String REFUSE_ANSWER = "%s, Вы не согласовали \"%s\" сотрудника %s из г. %s на период с %s - %s.";
    final String ACCEPT_ANSWER = "%s, Вы согласовали \"%s\" сотрудника %s из г. %s на период с %s - %s.";

    final private AtomicInteger GLOBAL_WRONG_REQUEST_COUNTER = new AtomicInteger(0);

    @RequestMapping(value = "/vacation_approval", method = RequestMethod.GET)
    public ModelAndView vacationApprovalShow(
            @RequestParam(value = "uid", required = true) String uid,
            @ModelAttribute("vacationApprovalForm") VacationApprovalForm vaForm,
            HttpServletRequest request
    ) {
        ModelAndView mav = new ModelAndView("vacation_approval");

        VacationApproval vacationApproval = vacationApprovalService.getVacationApproval(uid);

        if (vacationApproval == null){
            proceedBadRequest(vaForm, request);
            return mav;
        }

        String matchingFIO = vacationApproval.getManager().getName();
        String vacationType = vacationApproval.getVacation().getType().getValue();
        String employeeFIO = vacationApproval.getVacation().getEmployee().getName();
        String region = vacationApproval.getVacation().getEmployee().getRegion().getName();
        String dateBegin = getOnlyDate(vacationApproval.getVacation().getBeginDate());
        String dateEnd = getOnlyDate(vacationApproval.getVacation().getEndDate());
        String comment = vacationApproval.getVacation().getComment();
        Boolean result = vacationApproval.getResult();

        if (result == null){
            vaForm.setMessage(String.format(NOT_ACCEPTED_YET, matchingFIO, vacationType, employeeFIO, region, dateBegin,
                    dateEnd, comment));
            vaForm.setIsAllButtonsVisible(true);
        } else {
           vaForm.setMessage(String.format(ACCEPTED, vacationType, employeeFIO, region, dateBegin, dateEnd,
                   getResultString(result)));
           vaForm.setIsAllButtonsVisible(false);
        }


        return mav;
    }

    @RequestMapping(value = "/vacation_approval/save/{uid}/{acceptance}", method = RequestMethod.POST)
    public ModelAndView vacationApprovalSaveResult(
            @PathVariable("uid") String uid,
            @PathVariable ("acceptance") Boolean acceptance,
            @ModelAttribute("vacationApprovalForm") VacationApprovalForm vaForm,
            HttpServletRequest request
    ) {
        ModelAndView mav = new ModelAndView("vacation_approval");

        VacationApproval vacationApproval = vacationApprovalService.getVacationApproval(uid);

        if (vacationApproval == null){
            proceedBadRequest(vaForm, request);
        }

        // отпуск уже согласовывали
        if (vacationApproval.getResult() != null){
            return vacationApprovalShow(uid, vaForm, request);
        }

        String matchingFIO = vacationApproval.getManager().getName();
        String vacationType = vacationApproval.getVacation().getType().getValue();
        String employeeFIO = vacationApproval.getVacation().getEmployee().getName();
        String region = vacationApproval.getVacation().getEmployee().getRegion().getName();
        String dateBegin = getOnlyDate(vacationApproval.getVacation().getBeginDate());
        String dateEnd = getOnlyDate(vacationApproval.getVacation().getEndDate());

        vacationApproval.setResult(acceptance);
        if (acceptance){
            vaForm.setMessage(String.format(ACCEPT_ANSWER, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd));
        }else{
            vaForm.setMessage(String.format(REFUSE_ANSWER, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd));
        }

        sendMailService.performVacationAcceptanceMailing(vacationApproval);

        vacationApprovalService.store(vacationApproval);

        return mav;
    }

    private void proceedBadRequest(VacationApprovalForm vaForm, HttpServletRequest request){
        vaForm.setMessage(BAD_REQUEST);
        vaForm.setIsAllButtonsVisible(false);

        Integer userID = null;
        String userFIO = "<not defined>";
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser != null){
            userFIO = securityUser.getEmployee().getName();
            userID = securityUser.getEmployee().getId();
        }
        logger.warn("Somebody try to get vacation approval service by wrong UID number: {}",
                String.format(LOGGER_MESSAGE, request.getRemoteAddr(), userID, userFIO));

        // если счетчик неудачных попыток кратен лимиту из настроек
        if (GLOBAL_WRONG_REQUEST_COUNTER.getAndIncrement() % propertyProvider.getVacationApprovalErrorThreshold() == 0){
            // Отправим сообщение админам
            sendMailService.performVacationApprovalErrorThresholdMailing();
        }
    }

    public String getOnlyDate(Date date){
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public String getResultString(Boolean result){
        if (result) {
            return ACCEPTANCE;
        }
        return REFUSE;
    }


}
