package com.aplana.timesheet.form;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class CreateVacationForm {

    public static final String DATE_FORMAT = "dd.MM.yyyy";
    private String calFromDate;
    private String calToDate;
    private String comment;
    private Integer vacationType;

    public CreateVacationForm() {
    }

    public String getCalFromDate() {
        return calFromDate;
    }

    public void setCalFromDate(String calFromDate) {
        this.calFromDate = calFromDate;
    }

    public String getCalToDate() {
        return calToDate;
    }

    public void setCalToDate(String calToDate) {
        this.calToDate = calToDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getVacationType() {
        return vacationType;
    }

    public void setVacationType(Integer vacationType) {
        this.vacationType = vacationType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CreateVacationForm");
        sb.append("{calFromDate='").append(calFromDate).append('\'');
        sb.append(", calToDate='").append(calToDate).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", vacationType=").append(vacationType);
        sb.append('}');
        return sb.toString();
    }
}
