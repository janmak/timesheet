package com.aplana.timesheet.enums;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

public enum EffortInNextDayEnum implements TSEnum {
    NORMAL      (125, "Всё хорошо"),
    UNDERLOADED (126, "У меня будет мало работы"),
    OVERLOADED  (127, "Я буду перегружен(а)");


    private int id;
    private String name;


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }


    private EffortInNextDayEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static EffortInNextDayEnum getById(final int id) {
        return Iterables.find(Arrays.asList(EffortInNextDayEnum.values()), new Predicate<EffortInNextDayEnum>() {
            @Override
            public boolean apply(EffortInNextDayEnum input) {
                return input.getId() == id;
            }
        });
    }

}
