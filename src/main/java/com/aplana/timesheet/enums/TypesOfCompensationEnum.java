package com.aplana.timesheet.enums;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum TypesOfCompensationEnum implements TSEnum {

    FINANCIAL(115, "финансовая компенсация"),
    COMPENSATORY_HOLIDAY(116, "отгул"),
    NOTHING(117, "ничего не нужно");

    private final int id;
    private final String name;

    private TypesOfCompensationEnum(int id, String name) {
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
