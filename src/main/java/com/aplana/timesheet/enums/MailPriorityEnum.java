package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

public enum MailPriorityEnum {
    HIGH    (1),
    NORMAL  (3),
    LOW     (5);


    private int id;


    public int getId() {
        return id;
    }

    private MailPriorityEnum(int id) {
        this.id = id;
    }


    public static MailPriorityEnum getById(final int id) {
        return Iterables.find(Arrays.asList(MailPriorityEnum.values()), new Predicate<MailPriorityEnum>() {
            public boolean apply(MailPriorityEnum input) {
                return input.getId() == id;
            }
        });
    }
}
