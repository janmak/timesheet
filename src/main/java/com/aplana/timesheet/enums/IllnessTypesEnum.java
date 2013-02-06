package com.aplana.timesheet.enums;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
public enum IllnessTypesEnum implements TSEnum{
    ILLNESS(53, "Больничный"),
    EMPLOYEE_INFO(54, "Данные сотрудника");

    private Integer id;
    private String name;

    IllnessTypesEnum(Integer id, String name) {
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
