package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum  WorkPlace implements TSEnum {
    OFFICE(42, "В офисе"),
    HOME(43,"Дома"),
    AT_CUSTOMERS_OFFICE(44, "У заказчика"),
    OTHER(45, "Другое");

    private int id;
    private String name;

    private WorkPlace(int id, String name) {
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

    public static WorkPlace getById(final int id) {
        return Iterables.tryFind(Arrays.asList(WorkPlace.values()), new Predicate<WorkPlace>() {
            @Override
            public boolean apply(WorkPlace input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
