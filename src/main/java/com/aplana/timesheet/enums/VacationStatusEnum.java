package com.aplana.timesheet.enums;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum VacationStatusEnum implements TSEnum {
    APPROVEMENT_WITH_PM(57, "На согласовании РПГ"),
    APPROVED_BY_PM(58, "Согласовано РПГ"),
    APPROVEMENT_WITH_LM(59, "На согласовании ЛР"),
    APPROVED(60, "Утверждено"),
    REJECTED(61, "Отклонено");

    private final int id;
    private final String name;

    private VacationStatusEnum(int id, String name) {
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
