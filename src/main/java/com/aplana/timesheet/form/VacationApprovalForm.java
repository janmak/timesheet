package com.aplana.timesheet.form;

/**
 * @author iziyangirov
 */
public class VacationApprovalForm {

    String message;
    Boolean isAllButtonsVisible;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsAllButtonsVisible() {
        return isAllButtonsVisible;
    }

    public void setIsAllButtonsVisible(Boolean allButtonsVisible) {
        isAllButtonsVisible = allButtonsVisible;
    }
}
