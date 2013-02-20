package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.VacationApprovalForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    private TSPropertyProvider propertyProvider;
    @Autowired
    private SendMailService sendMailService;

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalController.class);

    final String NOT_ACCEPTED_YET = "%s, просьба принять решение по \"%s\" сотрудника %s из г. %s на период с %s - %s. %s";
    final String ACCEPTANCE = "Запрос \"%s\" сотрудника %s из г. %s на период с %s - %s %s.";
    final String ACCEPTED = "согласован";
    final String REFUSE = "не согласован";
    final String DATE_FORMAT = "dd.MM.yyyy";
    final String BAD_REQUEST = "Неверный запрос!";
    final String LOGGER_MESSAGE = "\n    UserIP: %s\n";
    final String REFUSE_ANSWER = "%s, Вы не согласовали \"%s\" сотрудника %s из г. %s на период с %s - %s.";
    final String ACCEPT_ANSWER = "%s, Вы согласовали \"%s\" сотрудника %s из г. %s на период с %s - %s.";

    final String BUTTONS_INVISIBLE = "style=\"display: none\"";

    final private AtomicInteger GLOBAL_WRONG_REQUEST_COUNTER = new AtomicInteger(0);

    @RequestMapping(value = "/vacation_approval", method = RequestMethod.GET)
    public ModelAndView vacationApprovalShow(
            @RequestParam(value = "uid", required = true) String uid,
            @ModelAttribute("vacationApprovalForm") VacationApprovalForm vaForm,
            HttpServletRequest request
    ) {
        ModelAndView mav = new ModelAndView("vacation_approval");
        mav.addObject("NoPageFormat", "true");

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
            vaForm.setButtonsVisible("");
        } else {
           vaForm.setMessage(String.format(ACCEPTANCE, vacationType, employeeFIO, region, dateBegin, dateEnd,
                   getResultString(result)));
           vaForm.setButtonsVisible(BUTTONS_INVISIBLE);
        }

        return mav;
    }

    @RequestMapping(value = "/vacation_approval/save/{uid}/{acceptance}")
    public ModelAndView vacationApprovalSaveResult(
            @PathVariable("uid") String uid,
            @PathVariable ("acceptance") Boolean acceptance,
            @ModelAttribute("vacationApprovalForm") VacationApprovalForm vaForm,
            HttpServletRequest request
    ) throws VacationApprovalServiceException {
        ModelAndView mav = new ModelAndView("vacation_approval");
        mav.addObject("NoPageFormat", "true");
        vaForm.setButtonsVisible(BUTTONS_INVISIBLE);

        VacationApproval vacationApproval = vacationApprovalService.getVacationApproval(uid);

        if (vacationApproval == null){
            proceedBadRequest(vaForm, request);
            return mav;
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

        vacationApprovalService.store(vacationApproval);

        sendMailService.performVacationAcceptanceMailing(vacationApproval);

        vacationApprovalService.checkVacationIsApproved(vacationApproval.getVacation());

        return mav;
    }

    private void proceedBadRequest(VacationApprovalForm vaForm, HttpServletRequest request){
        vaForm.setMessage(BAD_REQUEST);
        vaForm.setButtonsVisible(BUTTONS_INVISIBLE);

        logger.warn("Somebody try to get vacation approval service by wrong UID number: {}",
                String.format(LOGGER_MESSAGE, request.getRemoteAddr()));

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
            return ACCEPTED;
        }
        return REFUSE;
    }


}
