package com.aplana.timesheet.enums;

/**
 *
 * @author aimamutdinov
 */
public enum OvertimeCategory {
    HOLIDAY("В выходные и праздники"),
    SIMPLE("В рабочие дни"),
    ALL("Все");

    public String title;

    private OvertimeCategory() {
    }

    private OvertimeCategory(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getTitle() {
        return title;
    }

}
