package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.CalendarDAO;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CalendarService
{	
	@Autowired
	private CalendarDAO calendarDAO;

	/**
	 * Ищет в базе данных запись соответсвующую указанной дате в виде строки.
	 * @param String date Дата в виде строки.
	 * @return объект типа Calendar, либо null, если объект не найден.
	 */
    @Transactional
	public Calendar find(String date)
	{		
		return calendarDAO.find(DateTimeUtil.stringToTimestamp(date, DateTimeUtil.DATE_PATTERN));
	}
	
	/**
	 * Ищет в базе данных запись соответсвующую указанной дате в миллисекундах.
	 * @param long date Дата в миллисекундах.
	 * @return объект типа Calendar, либо null, если объект не найден.
	 */
    @Transactional
	public Calendar find(long date)
	{		
		return calendarDAO.find(new Timestamp(date));
	}
	
	/**
	 * Ищет в базе данных запись соответсвующую указанной дате в Timestamp.
	 * @param Timestamp date Дата.
	 * @return объект типа Calendar, либо null, если объект не найден.
	 */
    @Transactional
	public Calendar find(Timestamp date)
	{		
		return calendarDAO.find(date);
	}
	/**
	 * Формирует список годов, которые есть в таблице списания занятости.	 
	 * @return список List Calendar.
	 */
    public List<Calendar> getYearsList() {
        Calendar minYear = calendarDAO.getMinDateList();
        int maxYear = calendarDAO.getMaxDateList().getYear();

        List<Calendar> yearList = new ArrayList<Calendar>();
        yearList.add( minYear );
        if ( minYear.getYear() != maxYear ) {
            for ( int i = minYear.getYear() + 1; i <= maxYear; i++ ) {
                yearList.add( new Calendar( i, 1, calendarDAO.getMonthTxt( 1 ) ) );
            }
        }
        return yearList;
    }

    /**
	 * Формирует список месяцев, соответствующие годам, которые есть в системе.	 
	 * @return список List Calendar.
     */
    public List<Calendar> getMonthList( Integer year ) {
        List<Calendar> monthList = new ArrayList<Calendar>();
        List<Integer> tempList = calendarDAO.getMonth( year );
        for ( Integer aTempList : tempList ) {
            monthList.add( new Calendar( year, aTempList, calendarDAO.getMonthTxt( aTempList ) ) );
        }
        return monthList;
    }

    /**
	 * Формирует список дней для одного месяца.
	 * @param year
	 * @param month
	 * @return
	 */
	public List<Calendar> getDateList(Integer year, Integer month){
        return calendarDAO.getDateList(year, month);
	}
	
	/**
	 * Проверяет год на наличие в системе
	 * param year
	 * return true если год существует в системе
	 * return false если год не существует в системе
	 */
	public boolean yearValid(Integer year){
		return calendarDAO.yearValid(year);
	}
	/**
	 * Проверяет месяц на наличие в системе
	 * @param year
	 * @param month
	 * @return
	 */
    public boolean monthValid( Integer year, Integer month ) {
        return calendarDAO.monthValid( year, month );
    }

    public Calendar getLastWorkDay(Calendar day, Region region) {
        return calendarDAO.getLastWorkDay(day, region);
    }

    public Calendar getNextWorkDay(Calendar day, Region region) {
        return calendarDAO.getNextWorkDay(day, region);
    }

    /**
     * Возвращает количество выходных дней за выбранный период для конкретного региона
     */
    public Integer getHolydaysCounForRegion(Date beginDate, Date endDate, Region region) {
        return calendarDAO.getHolydaysCountForRegion(beginDate, endDate, region);
    }

    /**
     * Возвращает количетсов дней за период (даты должны быть из одного года. иначе может не работать, но
     * для выполнения поставленной задачи такая проверка не требуется)
     */
    public Long getAllDaysCount(Date beginDate, Date endDate){
        //при подсчете к каждой дате добавляем по одному дню, потому что ищется промужуток "до даты" - сама дата в него не входит
        Long daysToLastDate = DateUtils.getFragmentInDays(DateUtils.addDays(endDate, 1), java.util.Calendar.YEAR);
        Long daysToFirstDate = DateUtils.getFragmentInDays(DateUtils.addDays(beginDate, 1), java.util.Calendar.YEAR);

        return daysToLastDate - daysToFirstDate + 1;
    }
}