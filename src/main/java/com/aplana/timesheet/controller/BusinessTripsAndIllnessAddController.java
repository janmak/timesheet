package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.BusinessTrip;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.enums.BusinessTripTypesEnum;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessAddException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessAddForm;
import com.aplana.timesheet.form.validator.BusinessTripsAndIllnessAddFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static com.aplana.timesheet.enums.QuickReportTypesEnum.BUSINESS_TRIP;
import static com.aplana.timesheet.enums.QuickReportTypesEnum.ILLNESS;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
@Controller
public class BusinessTripsAndIllnessAddController extends AbstractController{

    @Autowired
    DictionaryItemService dictionaryItemService;
    @Autowired
    BusinessTripService businessTripService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    IllnessService illnessService;
    @Autowired
    ProjectService projectService;
    @Autowired
    BusinessTripsAndIllnessAddFormValidator businessTripsAndIllnessAddFormValidator;

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripsAndIllnessController.class);

    /**
     * Возвращает форму с заполненными данными для редактирования отчетов
     */
    @RequestMapping(value = "/businesstripsandillnessadd/{reportId}/{reportFormed}")
    public ModelAndView editBusinessTripOrIllness(@PathVariable("reportId") Integer reportId,
                                            @PathVariable("reportFormed") Integer reportFormed,
                                            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
                                            BindingResult result) throws BusinessTripsAndIllnessAddException {
        QuickReportTypesEnum reportType = getReportTypeAsEnum(reportFormed);
        switch (reportType){
            case ILLNESS: return getIllnessEditingForm(reportId, tsForm);
            case BUSINESS_TRIP: return getBusinessTripEditingForm(reportId, tsForm);
            default: throw new BusinessTripsAndIllnessAddException("Редактирование отчетов такого типа пока не реализовано!");
        }
    }

    /**
     * возвращает форму для создания больничного/командировки
     */
    @RequestMapping(value = "/businesstripsandillnessadd/{employeeId}")
    public ModelAndView showCreateBusinessTripOrIllnessForm (
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
            BindingResult result){
        Employee employee = employeeService.find(employeeId);

        return getModelAndViewCreation(employee);
    }

    /**
     * пытаемя добавить новый больничный/командировку
     */
    @RequestMapping(value = "/businesstripsandillnessadd/tryAdd/{employeeId}")
    public ModelAndView validateAndAddBusinessTripOrIllness(
            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
            BindingResult result, @PathVariable("employeeId") Integer employeeId) throws BusinessTripsAndIllnessAddException {
        Employee employee = employeeService.find(employeeId);
        tsForm.setEmployee(employee);
        businessTripsAndIllnessAddFormValidator.validate(tsForm, result);

        if (result.hasErrors()){
            return getModelAndViewCreation(employee);
        }

        QuickReportTypesEnum reportType = getReportTypeAsEnum(tsForm.getReportType());
        tsForm.setEmployee(employee);

        switch (reportType){
            case BUSINESS_TRIP: return addBusinessTrip(tsForm);
            case ILLNESS: return addIllness(tsForm);
            default: throw new BusinessTripsAndIllnessAddException("Сохранение данных для такого типа отчета не реализовано!");
        }
    }

    /**
     * сохраняем измененный больничный/командировку
     */
    @RequestMapping(value = "/businesstripsandillnessadd/trySave/{reportId}")
    public ModelAndView validateAndSaveBusinessTripOrIllness(
            @ModelAttribute("businesstripsandillnessadd") BusinessTripsAndIllnessAddForm tsForm,
            BindingResult result, @PathVariable("reportId") Integer reportId) throws BusinessTripsAndIllnessAddException {
        tsForm.setReportId(reportId);
        businessTripsAndIllnessAddFormValidator.validate(tsForm, result);
        if (result.hasErrors()){
            return getModelAndViewCreation(tsForm.getEmployee());
        }

        QuickReportTypesEnum reportType = getReportTypeAsEnum(tsForm.getReportType());

        switch (reportType){
            case BUSINESS_TRIP: return saveBusinessTrip(tsForm, reportId);
            case ILLNESS: return saveIllness(tsForm, reportId);
            default: throw new BusinessTripsAndIllnessAddException("Редактирование данных для такого типа отчета пока не реализовано!");
        }
    }

    @RequestMapping(value = "/businesstripsandillnessadd/resultsuccess")
    public ModelAndView businessTripOrIllnessAddedResultSuccess (){
        ModelAndView modelAndView = new ModelAndView("businesstripsandillnessaddresult");
        modelAndView.addObject("result", 1);

        return modelAndView;
    }

    @RequestMapping(value = "/businesstripsandillnessadd/resultfailed")
    public ModelAndView businessTripOrIllnessAddedResultError(){
        ModelAndView modelAndView = new ModelAndView("businesstripsandillnessaddresult");
        modelAndView.addObject("result", 0);
        modelAndView.addObject("errorMsg", "Произошла ошибка при сохранении данных на стороне сервиса.");

        return modelAndView;
    }

    @RequestMapping(value = "/businesstripsandillnessadd/getprojects/{employeeId}/{beginDate}/{endDate}", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String getProjects(@PathVariable("employeeId") Integer employeeId,
                              @PathVariable("beginDate") String beginDateStr,
                              @PathVariable("endDate") String endDateStr){
        final Timestamp beginDate = DateTimeUtil.stringToTimestamp(beginDateStr, DATE_FORMAT);
        final Timestamp endDate = DateTimeUtil.stringToTimestamp(endDateStr, DATE_FORMAT);
        //final Employee employee = employeeService.find(employeeId);  //скорее всего, проекты надо для конкретного сотрудника брать будет. пока не надо.

        List<Project> projects = projectService.getProjectsByDates(beginDate, endDate);

        return projectService.getProjectListAsJson(projects);
    }

    /**
     * возвращаем enum отчета по id. если такого id в emun-е не существует - бросаем exception
     */
    private QuickReportTypesEnum getReportTypeAsEnum(Integer reportId) throws BusinessTripsAndIllnessAddException {
        try {
            return EnumsUtils.getEnumById(reportId, QuickReportTypesEnum.class);
        } catch (NoSuchElementException ex){
            throw new BusinessTripsAndIllnessAddException("Операция не поддерживается для данного типа отчета!", ex);
        }
    }

    /**
     * Возвращает формочку с табличкой по больничным или командировкам выбранного сотрудника за выбранный месяц и
     * результат о выполнении операции
     */
    private ModelAndView getModelAndViewSuccess(Employee employee, Date reportDate, QuickReportTypesEnum reportType) {
        Integer divisionId = employee.getDivision().getId();
        Integer employeeId = employee.getId();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(reportDate);
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1;

        return new ModelAndView (String.format("redirect:/businesstripsandillness/%s/%s/%s/%s/%s", divisionId, employeeId, year, month, reportType.getId()));
    }

    /**
     * заполняем форму для редактирования командировки и возвращаем пользователю
     */
    private ModelAndView getBusinessTripEditingForm(Integer reportId, BusinessTripsAndIllnessAddForm tsForm) throws BusinessTripsAndIllnessAddException {
        BusinessTrip businessTrip;
        try {
            businessTrip = businessTripService.find(reportId);
            tsForm.setReportType(QuickReportTypesEnum.BUSINESS_TRIP.getId());
            tsForm.setBeginDate(businessTrip.getBeginDate());
            tsForm.setEndDate(businessTrip.getEndDate());
            tsForm.setEmployee(businessTrip.getEmployee());
            tsForm.setBusinessTripType(businessTrip.getType().getId());
            if (businessTrip.getType().getId().equals(BusinessTripTypesEnum.PROJECT)) {
                tsForm.setProjectId(businessTrip.getProject().getId());
            }
            tsForm.setComment(businessTrip.getComment());

            return getModelAndViewEditing(businessTrip.getEmployee(), businessTrip.getId());
        } catch (Throwable th) {
            logger.error(th.getMessage(), th);
            throw new BusinessTripsAndIllnessAddException("Ошибка при получении отчета из БД!");
        }
    }

    /**
     * заполняем форму для редактирования больничного и возвращаем пользователю
     */
    private ModelAndView getIllnessEditingForm(Integer reportId, BusinessTripsAndIllnessAddForm tsForm) {
        Illness illness = illnessService.find(reportId);
        tsForm.setReportType(QuickReportTypesEnum.ILLNESS.getId());
        tsForm.setEmployee(illness.getEmployee());
        tsForm.setBeginDate(illness.getBeginDate());
        tsForm.setEndDate(illness.getEndDate());
        tsForm.setReason(illness.getReason().getId());
        tsForm.setComment(illness.getComment());

        return getModelAndViewEditing(illness.getEmployee(), illness.getId());
    }

    /**
     * сохраняем больничный и возвращаем форму с табличкой по больничным и сообщением о результатах сохранения
     */
    private ModelAndView saveIllness(BusinessTripsAndIllnessAddForm tsForm, Integer reportId) throws BusinessTripsAndIllnessAddException {
        try {
            Illness illness = illnessService.find(reportId);
            illness.setBeginDate(tsForm.getBeginDate());
            illness.setEndDate(tsForm.getEndDate());
            illness.setReason(dictionaryItemService.find(tsForm.getReason()));
            illness.setComment(tsForm.getComment());
            illnessService.setIllness(illness);

            return getModelAndViewSuccess(illness.getEmployee(), illness.getEndDate(), ILLNESS);
        } catch (Throwable th) {
            throw new BusinessTripsAndIllnessAddException("Ошибка при редактировании больничного!", th);
        }
    }

    /**
     * сохраняем командировку и возвращаем форму с табличкой по командировкам и сообщением о результатах сохранения
     */
    private ModelAndView saveBusinessTrip(BusinessTripsAndIllnessAddForm tsForm, Integer reportId) throws BusinessTripsAndIllnessAddException {
        try {
            BusinessTrip businessTrip = businessTripService.find(reportId);
            businessTrip.setBeginDate(tsForm.getBeginDate());
            businessTrip.setEndDate(tsForm.getEndDate());
            businessTrip.setType(dictionaryItemService.find(tsForm.getBusinessTripType()));
            if (tsForm.getReportType().equals(BusinessTripTypesEnum.PROJECT.getId())){
                businessTrip.setProject(projectService.find(tsForm.getProjectId()));
            }
            businessTrip.setComment(tsForm.getComment());

            businessTripService.setBusinessTrip(businessTrip);

            return getModelAndViewSuccess(businessTrip.getEmployee(), businessTrip.getBeginDate(), BUSINESS_TRIP);
        } catch (Throwable th) {
            throw new BusinessTripsAndIllnessAddException("Ошибка при редактировании командировки!", th);
        }
    }

    /**
     * возвращает форму для редактирования
     */
    private ModelAndView getModelAndViewEditing(Employee employee, Integer reportId){
        ModelAndView modelAndView = getModelAndViewCreation(employee);
        modelAndView.addObject("reportId", reportId);

        return modelAndView;
    }

    /**
     * возвращаем форму для создания
     */
    private ModelAndView getModelAndViewCreation(Employee employee) {

        ModelAndView modelAndView = new ModelAndView("businesstripsandillnessadd");
        modelAndView.addObject("employeeId", employee.getId());
        modelAndView.addObject("employeeName", employee.getName());

        return modelAndView;
    }

    /**
     * создаем новый больничный, сохраняем в базу. возвращаем форму с табличкой по больничным и результатом сохранения
     */
    private ModelAndView addIllness(BusinessTripsAndIllnessAddForm tsForm) throws BusinessTripsAndIllnessAddException {
        try {
            Illness illness = new Illness();
            illness.setEmployee(tsForm.getEmployee());
            illness.setBeginDate(tsForm.getBeginDate());
            illness.setEndDate(tsForm.getEndDate());
            illness.setComment(tsForm.getComment());
            illness.setReason(dictionaryItemService.find(tsForm.getReason()));
            illnessService.setIllness(illness);

            return getModelAndViewSuccess(illness.getEmployee(), illness.getBeginDate(), ILLNESS);
        } catch (Throwable th) {
            throw new BusinessTripsAndIllnessAddException("Ошибка при сохранении больничного!", th);
        }
    }

    /**
     * создаем новую командировку, сохраняем в базу. возвращаем форму с табличкой по командировкам и результатом сохранения
     */
    private ModelAndView addBusinessTrip (BusinessTripsAndIllnessAddForm tsForm) throws BusinessTripsAndIllnessAddException {
        try {
            BusinessTrip businessTrip = new BusinessTrip();
            businessTrip.setEmployee(tsForm.getEmployee());
            businessTrip.setBeginDate(tsForm.getBeginDate());
            businessTrip.setEndDate(tsForm.getEndDate());
            businessTrip.setComment(tsForm.getComment());
            businessTrip.setType(dictionaryItemService.find(tsForm.getBusinessTripType()));
            businessTrip.setProject(projectService.find(tsForm.getProjectId()));
            businessTripService.setBusinessTrip(businessTrip);

            return getModelAndViewSuccess(businessTrip.getEmployee(), businessTrip.getBeginDate(), BUSINESS_TRIP);
        } catch (Throwable th){
            throw new BusinessTripsAndIllnessAddException("Ошибка при сохранении командировки!", th);
        }
    }

}
