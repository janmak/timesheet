package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum UndertimeCausesEnum implements TSEnum {
    ASK_FOR_LEAVE(100, "Отпросился"),
    DOG_SICK(101, "Плохо себя чувствовал"),
    LATE_FOR_WORK(102, "Опоздал"),
    NO_TASKS(103, "Для меня нет задач"),
    STATE_OF_EMERGENCY_IN_THE_OFFICE(104, "В офисе произошло ЧП"),
    OTHER(105, "Другое"),
    PARTIAL_JOB_RATE(123, "Я работаю на часть ставки, положенное время отработал");

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private UndertimeCausesEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
