package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.DictionaryItemService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.RegionService;
import com.aplana.timesheet.service.VacationService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.*;
import java.util.Calendar;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class VacationsController extends AbstractControllerForEmployeeWithYears {

    @Autowired
    private VacationsFormValidator vacationsFormValidator;
    @Autowired
    private VacationService vacationService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private EmployeeService employeeService;

    private static final String VACATION_FORM = "vacationsForm";

    @RequestMapping(value = "/vacations", method = RequestMethod.GET)
    public ModelAndView prepareToShowVacations(
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm
    ) {
        final Employee employee = securityService.getSecurityPrincipal().getEmployee();
        vacationsForm.setDivisionId(employee.getDivision().getId());
        vacationsForm.setEmployeeId(employee.getId());

        vacationsForm.setRegionsIdList(getRegionIdList());
        final ModelAndView modelAndView = createModelAndViewForEmployee("vacations", employee.getId(), employee.getDivision().getId());

        modelAndView.addObject("regionId", employee.getRegion().getId());
        modelAndView.addObject("managerList", getManagerList());
        modelAndView.addObject("regionList", getRegionList());
        modelAndView.addObject("regionsIdList", getRegionIdList());
        modelAndView.addObject("vacationTypes",
                dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId()));
        modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());

        return modelAndView;
    }

    @RequestMapping(value = "/vacations/{divisionId}/{employeeId}", method = RequestMethod.POST)
    public ModelAndView showVacations(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @ModelAttribute(VACATION_FORM) VacationsForm vacationsForm,
            BindingResult result
    ) {
        vacationsForm.setDivisionId(divisionId);
        vacationsForm.setEmployeeId(employeeId);

        vacationsFormValidator.validate(vacationsForm, result);

        String fromDate = vacationsForm.getCalFromDate();
        String toDate = vacationsForm.getCalToDate();
        Date beginDateTS = DateTimeUtil.stringToDate(fromDate, DATE_FORMAT);
        Date endDateTS = DateTimeUtil.stringToDate(toDate, DATE_FORMAT);
        Employee employee = employeeId != -1 ? employeeService.find(employeeId) : null;

        final List<Vacation> vacations = employeeId != -1 ? vacationService.findVacations(employeeId, beginDateTS, endDateTS,
                dictionaryItemService.find(vacationsForm.getVacationType()))
                : findAllVacations(divisionId,
                vacationsForm.getManagerId(),
                vacationsForm.getRegions(),
                beginDateTS,
                endDateTS,
                dictionaryItemService.find(vacationsForm.getVacationType()));

        final ModelAndView modelAndView = createModelAndViewForEmployee("vacations", employeeId, divisionId);

        final Integer vacationId = vacationsForm.getVacationId();

        if (vacationId != null) {
            try {
                vacationService.deleteVacation(vacationId);
                vacationsForm.setVacationId(null);
            } catch (DeleteVacationException ex) {
                result.rejectValue("vacationId", "error.vacations.deletevacation.failed", ex.getLocalizedMessage());
            }
        }


        final int vacationsSize = vacations.size();

        final List<Integer> calDays = new ArrayList<Integer>(vacationsSize);
        final List<Integer> workDays = new ArrayList<Integer>(vacationsSize);

        modelAndView.addObject("regionId", vacationsForm.getRegions());
        modelAndView.addObject("managerList", getManagerList());
        modelAndView.addObject("regionList", getRegionList());
        modelAndView.addObject("regionsIdList", getRegionIdList());
        modelAndView.addObject("calFromDate", fromDate);
        modelAndView.addObject("calToDate", toDate);
        modelAndView.addObject("vacationsList", vacations);
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        modelAndView.addObject("vacationTypes",
                dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId()));
        List<Region> regionListForCalc = new ArrayList<Region>();
        for (Integer i : vacationsForm.getRegions()){
            regionListForCalc.add(regionService.find(i));
        }
        modelAndView.addAllObjects(
                getSummaryAndCalcDays(regionListForCalc, vacations, calDays, workDays, beginDateTS.getYear() + 1900));
        modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());

        return modelAndView;
    }

    private List<Vacation> findAllVacations(Integer divisionId, Integer managerId, List<Integer> regionsId,
                                            Date beginDate, Date endDate, DictionaryItem typeId){
        List<Vacation> vacations = new ArrayList<Vacation>();
        for (Integer i : regionsId){
            List<Integer> employeesId = findEmployeeByManager(divisionId, managerId, i);
            for (Integer e : employeesId){
                List<Vacation> empVacation = vacationService.findVacations(e, beginDate, endDate, typeId);
                for (Vacation vac : empVacation){
                    vacations.add(vac);
                }
            }
        }
        return vacations;
    }

    private List<Integer> findEmployeeByManager(Integer divisionId, Integer managerId, Integer regionId){
        List<Integer> returnList = new ArrayList<Integer>();
        List<Integer> iteratorList = employeeService.getEmployeesIdByDivisionManagerRegion(divisionId, managerId, regionId);
        if (iteratorList.size() == 0){
            return returnList;
        }else{
            for (Integer i : iteratorList){
                returnList.add(i);
                List<Integer>  iterator2List = findEmployeeByManager(divisionId, i, regionId);
                if (iterator2List.size() != 0){
                    for (Integer l : iterator2List){
                        returnList.add(l);
                    }
                }
            }
            return returnList;
        }
    }

    private Map<String, Integer> getSummaryAndCalcDays(List<Region> regions, List<Vacation> vacations,
                                                       List<Integer> calDays,
                                                       List<Integer> workDays, int year
    ) {

        int summaryApproved = 0;
        int summaryRejected = 0;
        int summaryCalDays = 0;
        int summaryWorkDays = 0;

        if (!vacations.isEmpty()) {
            for (Region reg : regions){
                List<Vacation> differRegionVacations = new ArrayList<Vacation>();
                for (Vacation vac : vacations){
                    if (vac.getEmployee().getRegion().equals(reg)){
                        differRegionVacations.add(vac);
                    }
                }
                final int vacationsSize = differRegionVacations.size();
                if (!differRegionVacations.isEmpty()){
                    final Vacation firstVacation = differRegionVacations.get(0);
                    Date minDate = firstVacation.getBeginDate();
                    Date maxDate = firstVacation.getEndDate();

                    Date beginDate, endDate;
                    for (Vacation vacation : differRegionVacations) {
                        beginDate = vacation.getBeginDate();
                        endDate = vacation.getEndDate();

                        if (minDate.after(beginDate)) {
                            minDate = beginDate;
                        }

                        if (maxDate.before(endDate)) {
                            maxDate = endDate;
                        }

                        calDays.add(getDiffInDays(beginDate, endDate));
                    }

                    final List<Holiday> holidaysForRegion =
                            calendarService.getHolidaysForRegion(minDate, maxDate, reg);
                    final Calendar calendar = Calendar.getInstance();

                    calendar.set(Calendar.YEAR, year);
                    final Date currentYearBeginDate = DateUtils.truncate(calendar.getTime(), Calendar.YEAR);

                    calendar.setTime(currentYearBeginDate);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    calendar.set(Calendar.YEAR, year);
                    final Date currentYearEndDate = calendar.getTime();

                    for (int i = 0; i < vacationsSize; i++) {
                        final Vacation vacation = differRegionVacations.get(i);
                        final int holidaysCount = getHolidaysCount(holidaysForRegion, vacation.getBeginDate(), vacation.getEndDate());

                        final int calDaysCount = calDays.get(i);
                        final int workDaysCount = calDaysCount - holidaysCount;

                        workDays.add(workDaysCount);

                        final VacationStatusEnum vacationStatus =
                                EnumsUtils.getEnumById(vacation.getStatus().getId(), VacationStatusEnum.class);

                        if (vacationStatus == VacationStatusEnum.APPROVED) {
                            beginDate = vacation.getBeginDate();
                            endDate = vacation.getEndDate();

                            calendar.setTime(beginDate);
                            final int beginYear = calendar.get(Calendar.YEAR);

                            calendar.setTime(endDate);
                            final int endYear = calendar.get(Calendar.YEAR);

                            if (beginYear == year && year == endYear) {
                                summaryCalDays += calDaysCount;
                                summaryWorkDays += workDaysCount;
                            } else {
                                final long days = DateUtils.getFragmentInDays(endDate, Calendar.YEAR);

                                if (endYear == year) {
                                    summaryCalDays += days;
                                    summaryWorkDays += days - getHolidaysCount(holidaysForRegion, currentYearBeginDate, endDate);
                                } else {
                                    final long daysInCurrentYear = calDaysCount - days;

                                    summaryCalDays += daysInCurrentYear;
                                    summaryWorkDays += daysInCurrentYear -
                                            getHolidaysCount(holidaysForRegion, beginDate, currentYearEndDate);
                                }
                            }

                            summaryApproved++;
                        }

                        if (vacationStatus == VacationStatusEnum.REJECTED) {
                            summaryRejected++;
                        }
                    }
                }
            }
        }

        final Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("summaryApproved", summaryApproved);
        map.put("summaryRejected", summaryRejected);
        map.put("summaryCalDays", summaryCalDays);
        map.put("summaryWorkDays", summaryWorkDays);

        return map;
    }

    private int getDiffInDays(Date beginDate, Date endDate) {
        return (int) ((endDate.getTime() - beginDate.getTime()) / (24 * 3600 * 1000) + 1);
    }

    private List<Region> getRegionList() {
        return regionService.getRegions();
    }

    private List<Employee> getManagerList() {
        return employeeService.getManagerListForAllEmployee();
    }

    private List<Integer> getRegionIdList(){
        List<Integer> regionsIdList = new ArrayList<Integer>();
        for (Region region : getRegionList()){
            regionsIdList.add(region.getId());
        }
        return regionsIdList;
    }

    private int getHolidaysCount(List<Holiday> holidaysForRegion, final Date beginDate, final Date endDate) {
        return Iterables.size(Iterables.filter(holidaysForRegion, new Predicate<Holiday>() {
            @Override
            public boolean apply(@Nullable Holiday holiday) {
                final Timestamp calDate = holiday.getCalDate().getCalDate();

                return (
                        calDate.compareTo(beginDate) == 0 || calDate.compareTo(endDate) == 0 ||
                                calDate.after(beginDate) && calDate.before(endDate)
                );
            }
        }));
    }

}
