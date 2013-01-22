package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum SystemRole {
    MANAGER(1), SYSTEM_ENGINEER(2), OTHER(0);

    private int id;

    private SystemRole( int id ) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SystemRole getById( final int id ) {
        return Iterables.tryFind(Arrays.asList(SystemRole.values()), new Predicate<SystemRole>() {
            @Override
            public boolean apply(SystemRole input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
