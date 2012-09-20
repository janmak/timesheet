package com.aplana.timesheet.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimeUtil {
    public static final long DAY_IN_MILLS = 86400000;
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    public static final String MIN_DATE="1900-01-01";
    public static final String MAX_DATE="2999-12-31";

    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * Преобразует строку даты в указанном формате в объект класса {@link Date}.
     *
     * @param date       - строка даты.
     * @param dateFormat - формат даты ({@link SimpleDateFormat})
     * @return объект класса {@link Date}.
     */
    public static Date stringToDate(String date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date result = new Date();

        try {
            result = sdf.parse(date);
        } catch (ParseException e) {
            logger.error("Error while parsing date in string format.", e);
        }
        return result;
    }

    /**
     * Преобразует строку даты в указанном формате в объект класса {@link Timestamp}.
     *
     * @param date       - строка даты.
     * @param dateFormat - формат даты ({@link SimpleDateFormat})
     * @return объект класса {@link Timestamp}.
     */
    public static Timestamp stringToTimestamp(String date, String dateFormat) {
        return new Timestamp(stringToDate(date, dateFormat).getTime());
    }

    /**
     * Разбивает указанный диапазон дат на отдельные даты и возвращает их в виде списка Timestamp
     *
     * @param String beginDate начальная дата.
     * @param String endDate конечная дата.
     * @return список дат из диапазона.
     */
    public static List<String> splitDateRangeOnDays(String beginDate, String endDate) {
        List<String> result = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        long begin = stringToTimestamp(beginDate, DATE_PATTERN).getTime();
        long end = stringToTimestamp(endDate, DATE_PATTERN).getTime();
        Calendar calendar = Calendar.getInstance();
        while (begin <= end) {
            calendar.setTimeInMillis(begin);
            result.add(sdf.format(calendar.getTime()));
            begin += DAY_IN_MILLS;
        }
        logger.debug(result.toString());
        return result;
    }

    /**
     * Преобразует строку даты из формата ldap в Timestamp
     *
     * @param ldapDate строка даты в формате ldap (yyyymmdd)
     * @return датa в формате Timestamp
     */
    public static Timestamp ldapDateToTimestamp(String ldapDate) {
        String year = ldapDate.substring(0, 4);
        String month = ldapDate.substring(4, 6);
        String day = ldapDate.substring(6, 8);
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("-").append(month).append("-").append(day);
        return stringToTimestamp(sb.toString(), DATE_PATTERN);
    }

    /**
     * Преобразует строку даты из формата yyyy-MM-dd в формат dd.MM.yyyy
     *
     * @param d строка даты в формате yyyy-MM-dd
     * @return строка даты в формате dd.MM.yyyy
     */
    public static String formatDateString(String d) {
        String[] date = StringUtils.split(d, "-");
        return new StringBuilder()
                .append(date[2])
                .append(".")
                .append(date[1])
                .append(".")
                .append(date[0])
                .toString();
    }

    /**
     * Преобразует дату из строки в Timestamp
     *
     * @param Date
     * @return Timestamp
     */
    public static Timestamp stringToTimestamp(String Date) {
        logger.debug("Datestring {}", Date);
        return new Timestamp(stringToTimestamp(Date, DATE_PATTERN).getTime());
    }

    /**
     * Возвращает первый день текущего месяца
     *
     * @return String
     */
    public static String currentMonthFirstDay() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
//		logger.debug("First day of month " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    /**
     * Возвращает первый день прошлого месяца
     *
     * @return String
     */
    public static String previousMonthFirstDay() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
//		logger.debug("First day of previous month " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    /**
     * Возвращает текущий день
     *
     * @return String
     */
    public static String currentDay() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        logger.debug("Present day " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    /**
     * Возвращает последнее воскресенье
     *
     * @return String
     */
    public static String lastSunday() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            } else
                calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        logger.debug("Last Sunday " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    /**
     * Возвращает день за два дня до последнего рабочего дня месяца
     *
     * @return String
     */
    public static String endMonthCheckDay() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);

        while (calendar.get(Calendar.DAY_OF_MONTH) != calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 2) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            } else
                calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        logger.debug("End month check day " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    /**
     * Возвращает конец прошлого месяца
     *
     * @return String
     */
    public static String endPrevMonthDay() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Date curDate = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        while (calendar.get(Calendar.DAY_OF_MONTH) != calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                calendar.add(Calendar.YEAR, -1);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
            } else
                calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        logger.debug("End prev month day " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    /**
     * Возвращает конец текущего месяца
     *
     * @return String
     */
    public static String endMonthDay(Timestamp day) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        logger.debug("End current month day " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }


    /**
     * Возвращает название текущего месяца в виде строки
     *
     * @param dateString
     * @return String
     */
    public static String getMonthTxt(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
        Date date = new Date();
        date.setTime(DateTimeUtil.stringToTimestamp(dateString).getTime());
        return sdf.format(date);
    }

    /**
     * Cравнивает даты, true если первая дата позже
     *
     * @param firstDate
     * @param secondDate
     * @return
     */
    public static boolean dayAfterDay(String firstDate, String secondDate) {
        return stringToTimestamp(firstDate).after(stringToTimestamp(secondDate));
    }

    public static String decreaseDay(String day) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateTimeUtil.stringToTimestamp(day));

        if (calendar.get(Calendar.DAY_OF_YEAR) == 1) {
            calendar.add(Calendar.YEAR, -1);
            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        } else
            calendar.add(Calendar.DAY_OF_YEAR, -1);

        return sdf.format(calendar.getTime());
    }

    public static String increaseDay(String day) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateTimeUtil.stringToTimestamp(day));

        if (calendar.get(Calendar.DAY_OF_YEAR) == calendar.getActualMaximum(calendar.DAY_OF_YEAR)) {
            calendar.add(Calendar.YEAR, 1);
            calendar.set(Calendar.DAY_OF_YEAR, 1);
        } else
            calendar.add(Calendar.DAY_OF_YEAR, 1);

        return sdf.format(calendar.getTime());
    }

    public static String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(timestamp);
    }
    
    public static Boolean isDateValid(String date) {
        boolean result = false;

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

        try {
            sdf.parse(date);
            result = true;
        } catch (ParseException e) {
            result = false;
        }
        return result;                
    }


    public static Boolean isPeriodValid(String strDateBegin, String strDateEnd) {
        boolean result = false;

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

        try {
            Date dateBeg = sdf.parse(strDateBegin);
            Date dateEnd = sdf.parse(strDateEnd);
            result = dateBeg.before(dateEnd) || dateBeg.equals(dateEnd);
        } catch (ParseException e) {
            result = false;
        }
        return result;
    }

}