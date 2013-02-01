package com.aplana.timesheet.enums;

/**
 * User: vsergeev
 * Date: 22.01.13
 */
public enum Regions implements TSEnum {
    OTHERS(1, "Остальные регионы"),
    UFA(2, "Уфа"),
    MOSCOW(3, "Москва"),
    PERM(4, "Пермь"),
    NIJNIY_NOVGOROD(5, "Нижний Новгород");

    private int id;
    private final String name;

    Regions(int id, String name) {
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
