package com.aplana.timesheet.enums;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
public enum QuickReportTypesEnum implements TSEnum{
    ILLNESS(6, "Больничный"),
    BUSINESS_TRIP(7, "Командировка");

    private Integer id;
    private String name;

    QuickReportTypesEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
