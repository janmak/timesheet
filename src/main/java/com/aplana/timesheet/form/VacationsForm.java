package com.aplana.timesheet.form;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class VacationsForm extends AbstractFormForEmployee {

    private Integer year;
    private Integer vacationId;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getVacationId() {
        return vacationId;
    }

    public void setVacationId(Integer vacationId) {
        this.vacationId = vacationId;
    }
}
