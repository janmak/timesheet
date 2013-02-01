package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.RegionDAO;
import com.aplana.timesheet.dao.ReportCheckDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.TimeSheetConstans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class ReportCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ReportCheckService.class);
    Properties mailConfig = new Properties();

    private StringBuffer trace = new StringBuffer();

    private Boolean reportForming = false;

    @Autowired
    private ReportCheckDAO reportCheckDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private HolidayDAO holidayDAO;

    @Autowired
    private RegionDAO regionDAO;

    /**
     * Метод формирования оповещений используемый в таймере
     */
    public void storeReportCheck() {

        trace.setLength(0);

        trace.append("Start send mails\n");

        String currentDay = DateTimeUtil.currentDay();

        Calendar currentCalendar = calendarService.find(currentDay);

        // Выполняем проверки только по рабочим дням
        if (holidayDAO.isWorkDay(currentDay)) {
            String firstDay = DateTimeUtil.previousMonthFirstDay(),
                    endMonthDay = DateTimeUtil.endMonthDay(new Timestamp(System.currentTimeMillis())),
                    endPrevMonthDay = DateTimeUtil.endPrevMonthDay(),
                    lastSunday = DateTimeUtil.lastSunday(),
                    lastDay = lastSunday;
            // Если конец месяца
            if (DateTimeUtil.dayAfterDay(endMonthDay, lastSunday))
                lastDay = currentDay;
            // Если новый месяц - надо взять последний день предыдущего месяца
            if (DateTimeUtil.dayAfterDay(endPrevMonthDay, lastSunday))
                lastDay = endPrevMonthDay;
            // lastDay никогда не должен быть сегодняшним днем. т.к. проверка идет ночью и сегодняшний день еще только начался
            if (lastDay.equals(currentDay))
                lastDay = DateTimeUtil.decreaseDay(lastDay);
            storeReportCheck(firstDay, lastDay, lastSunday.equals(currentDay));
        }

        trace.append("Finish send mails\n");

    }

    /**
     * Заносит в базу список проверки заполнения отчетов по опред-м подразделениям за определенные дни
     *
     * @param firstDay
     * @param lastDay
     * @param sundayCheck
     */
    public void storeReportCheck(String firstDay, String lastDay, boolean sundayCheck) {
        try {
            FileInputStream propertiesFile = new FileInputStream( TimeSheetConstans.PROPERTY_PATH );

            mailConfig.load(propertiesFile);

            String[] divisionsSendMail = mailConfig.getProperty("mail.divisions").split(" ");
            logger.info("divisionlist is {}", mailConfig.getProperty("mail.divisions"));

            for (String divisionId : divisionsSendMail) {
                logger.info("division id is {}", Integer.parseInt(divisionId));
                storeReportCheck(divisionService.find(Integer.parseInt(divisionId)), firstDay, lastDay, sundayCheck);
            }
        } catch (FileNotFoundException e1) {
            logger.error("File timesheet.properties not found.");
        } catch (InvalidPropertiesFormatException e) {
            logger.error("Invalid timesheet.properties file format.");
        } catch (IOException e) {
            logger.error("Input-output error.");
        }

    }

    /**
     * Заносит в базу список проверки заполнения отчетов по подразделению за определенные дни
     *
     * @param division
     * @param firstDay
     * @param lastDay
     * @param sundayCheck
     */
    @Transactional  // Траблы с LAZY. Пусть весь метод выполняется в одной транзакции
    public void storeReportCheck(Division division, String firstDay, String lastDay, boolean sundayCheck) {
        logger.info("storeReportcheck() for division {} entered", division.getId());
        Integer reportsNotSendNumber;
        List<ReportCheck> reportCheckList = new ArrayList<ReportCheck>();
        List<Employee> employeeList = employeeService.getEmployeesReportCheck(division);
        List<String> dayList = DateTimeUtil
                .splitDateRangeOnDays(firstDay, lastDay);
        String currentDay = DateTimeUtil.currentDay();
        for (Employee emp : employeeList) {
            logger.info("Employee {}", emp.getName());
            // если сотрудник работает и не начальник подразделения
            
            if (!emp.isDisabled(null) && emp.getManager() != null) {
                reportsNotSendNumber = 0;
                List<String> passedDays = new ArrayList<String>();
                for (String day : dayList) {
                    Calendar calendar = calendarService.find(day);
                    //если рабочий день
                    if (holidayDAO.isWorkDay(day, emp.getRegion())) {
                        //если день после устройства на работу включительно
                        if ( ! calendar.getCalDate().before( emp.getStartDate() ) ) {
                            //если сотрудник не списал рабочее время за этот день
                            if (timeSheetService.findForDateAndEmployee(day, emp.getId()) == null) {
                                reportsNotSendNumber++;
                                logger.info("Passed day added");
                                passedDays.add(day);
                            }
                        }
                    }
                }
                // В reportCheckList попадают только те у кого есть долги
                if (reportsNotSendNumber > 0) {
                    ReportCheck reportCheck = new ReportCheck();
                    reportCheck.setCheckDate(currentDay);
                    reportCheck.setEmployee(emp);
                    reportCheck.setDivision(division);
                    reportCheck.setReportsNotSendNumber(reportsNotSendNumber);
                    reportCheck.setSundayCheck(sundayCheck);
                    reportCheck.setPassedDays(passedDays);
                    reportCheckList.add(reportCheck);
                }

            }
        }
        if (reportCheckList.size() > 0) {
            reportCheckDAO.setReportChecks(reportCheckList);

            Calendar currentCalendar = calendarService.find(currentDay);

            logger.info("Reportcheck object for division {} ({}) saved.", division.getId(), currentCalendar.getCalDate());

            sendMailService.performPersonalAlertMailing(reportCheckList);
            sendMailService.performManagerMailing(reportCheckList);

            // Если последний рабочий день месяца (по стране, без учета регионов)- рассылаем напоминания о заполнении отчетов для всех у кого нет долгов по отчетности
            Calendar workDay = calendarService.getLastWorkDay(currentCalendar, null);
            if (workDay.getCalDate().equals(DateTimeUtil.stringToTimestamp(currentDay, DateTimeUtil.DATE_PATTERN)))
                sendMailService.performEndMonthMailing(reportCheckList);
        } else
            logger.info("Reportchecks not found, all timesheets are filled");
    }

    public String getTrace() {
        return trace.toString();
    }

}