package com.aplana.timesheet.util;

import com.aplana.timesheet.enums.TSEnum;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * User: vsergeev
 * Date: 22.01.13
 */
public class EnumsUtils {

    public static <T extends TSEnum> T getEnumById (T[] values, final Integer value){
        return Iterables.tryFind(Arrays.asList(values), new Predicate<T>() {
            @Override
            public boolean apply(@Nullable T t) {
                return value.equals(t.getId());
            }
        }).orNull();
    }

}
