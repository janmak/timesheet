package com.aplana.timesheet.enums;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum VacationStatus implements TSEnum {
    APPROVEMENT_WITH_PM(50, "На согласовании РПГ"),
    APPROVED_BY_PM(51, "Согласовано РПГ"),
    APPROVEMENT_WITH_LM(52, "На согласовании ЛР"),
    APPROVED(53, "Утверждено"),
    REJECTED(54, "Отклонено");

    private final int id;
    private final String name;

    private VacationStatus(int id, String name) {
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
