package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum OvertimeCausesEnum implements TSEnum {
    A_LOT_OF_WORK(110, "У меня большой объем задач, не успеваю справляться в рабочее время"),
    OWN_INITIATIVE(111, "Моя собственная инициатива"),
    MANAGER_REQUEST(112, "Просьба руководителя"),
    WORKING_OFF(113, "Отрабатываю время за предыдущие (последующие) дни"),
    OTHER(114, "Другое");

    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private OvertimeCausesEnum(int id, String name) {

        this.id = id;
        this.name = name;
    }
}
