package com.aplana.timesheet.form;

public class ViewReportsForm
{
	private Integer employeeId;
    private Integer divisionId;

	private Integer year;
	private Integer month;
	private String monthTxt;

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

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
		
	public Integer getEmployeeId()
	{
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId)
	{
		this.employeeId = employeeId;
	}
}
	