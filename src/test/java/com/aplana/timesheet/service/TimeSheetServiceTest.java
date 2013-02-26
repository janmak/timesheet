package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static com.aplana.timesheet.enums.TypesOfActivityEnum.ILLNESS;
import static com.aplana.timesheet.enums.TypesOfActivityEnum.getById;

public class TimeSheetServiceTest extends AbstractJsonTest {

    private static final int EMPLOYEE_ID = 1;
    private static final String DATE = DateFormatUtils.format(new Date(), DateTimeUtil.DATE_PATTERN);

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    AvailableActivityCategoryDAO availableActivityCategoryDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private TimeSheetDAO timeSheetDAO;

    @Autowired
    private EmployeeService employeeService;

    public String getListOfActDescriptoinForTest(){
        List<AvailableActivityCategory> availableActivityCategories = availableActivityCategoryDAO.getAllAvailableActivityCategories();
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (AvailableActivityCategory activityCategory : availableActivityCategories){
            result.append("{");
            result.append("\"actCat\":" + activityCategory.getActCat().getId() + ",");
            result.append("\"actType\":" + activityCategory.getActType().getId() + ",");
            result.append("\"projectRole\":" + activityCategory.getProjectRole().getId() + ",");
            result.append("\"description\":\"");
            if (activityCategory.getDescription() != null){
                result.append(activityCategory.getDescription());
            }
            result.append("\"");
            result.append("},");
        }
        result.append("{\"actCat\":0,\"actType\":0,\"projectRole\":0,\"description\":\"\"}");
        result.append("]");
        return result.toString();
    }

    private String getPlansJson(String date, Integer employeeId) {
        StringBuilder json = new StringBuilder();

        TimeSheet lastTimeSheet = timeSheetDAO.findLastTimeSheetBefore(calendarService.find(date), employeeId);
        Calendar nextWorkDay = calendarService.getNextWorkDay(calendarService.find(date), employeeService.find(employeeId).getRegion());
        TimeSheet nextTimeSheet = timeSheetDAO.findNextTimeSheetAfter(nextWorkDay, employeeId);

        json.append("{");
        if (lastTimeSheet != null) {
            json.append("\"prev\":{");
            json.append("\"dateStr\":");
            json.append( "\"" ).append( DateTimeUtil.formatDate(lastTimeSheet.getCalDate().getCalDate()) )
                    .append( "\"," );   //преобразование к  yyyy-MM-dd
            json.append("\"plan\":\"");
            String lastPlan = lastTimeSheet.getPlanEscaped();
            if (lastPlan != null)
                json.append( "" ).append( lastPlan.replace( "\r\n", "\\n" ) );
            json.append("\"}");
        }

        if (lastTimeSheet != null && nextTimeSheet != null)
            json.append(",");

        if (nextTimeSheet != null &&
                !( ILLNESS == getById(
                        Lists.newArrayList(
                                nextTimeSheet.getTimeSheetDetails()).get(0).getActType().getId()))) { // <APLANATS-458>
            json.append("\"next\":{")
                    .append( "\"dateStr\":" ).append( "\"" )
                    .append( DateTimeUtil.formatDate( nextTimeSheet.getCalDate().getCalDate() ) )
                    .append( "\"," )   //преобразование к  yyyy-MM-dd
                    .append( "\"plan\":\"" );
            String nextPlan = timeSheetService.getStringTimeSheetDetails(nextTimeSheet);
            if (nextPlan != null)
                json.append(nextPlan.replace("\r\n","\\n"));
            json.append("\"}");
        }
        json.append("}");
        return json.toString();

    }

    @Test
    public void testGetListOfActDescriptoin(){
        String currentResult = timeSheetService.getListOfActDescriptoin();
        String testResult = getListOfActDescriptoinForTest();
        assertJsonEquals(testResult, currentResult);
    }

    @Test
    public void testGetPlansJson() {
        assertJsonEquals(getPlansJson(DATE, EMPLOYEE_ID), timeSheetService.getPlansJson(DATE, EMPLOYEE_ID));
    }

}
