package com.aplana.timesheet.form;

public class ViewReportsForm extends AbstractFormForEmployee {

    private Integer year;
	private Integer month;
	private String monthTxt;

    public Integer getYear()
	{
		return year;
	}

	public void setYear(Integer year)
	{
		this.year = year;
	}
	
	public Integer getMonth()
	{
		return month;
	}

	public void setMonth(Integer month)
	{
		this.month = month;
	}
	
	public String getMonthTxt()
	{
		return monthTxt;
	}

	public void setMonthTxt(String monthTxt)
	{
		this.monthTxt = monthTxt;
	}

}
	