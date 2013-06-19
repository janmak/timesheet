package com.aplana.timesheet.dao.entity;

/**
 * @author Aalikin
 * @since 05.06.13
 */
public class VacationInYear {

    private String vacationType;
    private Integer year;
    private Integer summaryCalDays;
    private Integer summaryWorkDays;

    public VacationInYear(){
        super();
    }

    public VacationInYear(String vacationType, Integer year, Integer summaryCalDays, Integer summaryWorkDays){
        this.vacationType = vacationType;
        this.year = year;
        this.summaryCalDays = summaryCalDays;
        this.summaryWorkDays  = summaryWorkDays;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getSummaryCalDays() {
        return summaryCalDays;
    }

    public void setSummaryCalDays(Integer summaryCalDays) {
        this.summaryCalDays = summaryCalDays;
    }

    public Integer getSummaryWorkDays() {
        return summaryWorkDays;
    }

    public void setSummaryWorkDays(Integer summaryWorkDays) {
        this.summaryWorkDays = summaryWorkDays;
    }

    public String getVacationType() {
        return vacationType;
    }

    public void setVacationType(String vacationType) {
        this.vacationType = vacationType;
    }
}
