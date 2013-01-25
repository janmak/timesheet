package com.aplana.timesheet.enums;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum VacationType implements TSEnum {

    WITH_PAY(62, "Отпуск с сохранением содержания"),
    WITHOUT_PAY(63, "Отпуск без сохранения содержания"),
    WITH_NEXT_WORKING(64, "Отпуск с последующей отработкой");

    public static final int DICT_ID = 9;

    private final int id;
    private final String name;

    private VacationType(int id, String name) {
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
