package com.aplana.timesheet.enums;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
public enum BusinessTripTypes implements TSEnum {
    PROJECT(55, "Проектная"),
    NOT_PROJECT(56, "Внепроектная");

    private Integer id;
    private String name;

    BusinessTripTypes(Integer id, String name) {
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
