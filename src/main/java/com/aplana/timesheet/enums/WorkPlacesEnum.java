package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum WorkPlacesEnum implements TSEnum {
    OFFICE(49, "В офисе"),
    HOME(50,"Дома"),
    AT_CUSTOMERS_OFFICE(51, "У заказчика"),
    OTHER(52, "Другое");

    private int id;
    private String name;

    private WorkPlacesEnum(int id, String name) {
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

    public static WorkPlacesEnum getById(final int id) {
        return Iterables.tryFind(Arrays.asList(WorkPlacesEnum.values()), new Predicate<WorkPlacesEnum>() {
            @Override
            public boolean apply(WorkPlacesEnum input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
