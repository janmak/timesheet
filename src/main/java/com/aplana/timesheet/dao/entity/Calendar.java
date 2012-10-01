package com.aplana.timesheet.dao.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;

@Entity
@Table(name = "calendar")
public class Calendar
{
	@Id
	@Column(name = "caldate", columnDefinition = "date not null")
	private Timestamp calDate;
	
	@Column(name = "month_txt", nullable = false, length = 10)
	private String monthTxt;
	
	@Column(nullable = false)
	private int month;
	
	@Column(nullable = false)
	private int year;

    @OneToMany(mappedBy = "calDate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TimeSheet> timeSheets;

    @OneToMany(mappedBy = "calDate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Holiday> holidays;

    public Set<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(Set<Holiday> holidays) {
        this.holidays = holidays;
    }

    public Set<TimeSheet> getTimeSheets() {
        return timeSheets;
    }

    public void setTimeSheets(Set<TimeSheet> timeSheets) {
        this.timeSheets = timeSheets;
    }

	public Calendar(){};
	
	public Calendar(Integer year, Integer month, String monthTxt){
		this.year = year;
		this.month = month;
		this.monthTxt = monthTxt;
	}

	public Timestamp getCalDate()
	{
		return calDate;
	}

	public int getYear()
	{
		return year;
	}

	public String getMonthTxt()
	{
		return monthTxt;
	}

	public int getMonth()
	{
		return month;
	}

	public void setCalDate(Timestamp calDate)
	{
		this.calDate = calDate;
	}

	public void setYear(int year)
	{
		this.year = year;
	}

	public void setMonthTxt(String monthTxt)
	{
		this.monthTxt = monthTxt;
	}

	public void setMonth(int month)
	{
		this.month = month;
	}

   	@Override
	public String toString()
	{
		return new StringBuilder()		
			.append(" calDate=")
			.append(calDate)
			.append(" monthTxt=")
			.append(monthTxt)
			.append(" month=")
			.append(month)
			.append(" year=")
			.append(year)
			.toString();
	}
}