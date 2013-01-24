package com.aplana.timesheet.util;

import com.aplana.timesheet.enums.TSEnum;

import java.util.NoSuchElementException;

/**
 * User: vsergeev
 * Date: 22.01.13
 */
public class EnumsUtils {

    public static <T extends TSEnum> T getEnumById(int id, Class<T> aClass) {
        for (T enumValue : aClass.getEnumConstants()) {
            if (enumValue.getId() == id) {
                return enumValue;
            }
        }

        throw new NoSuchElementException();
    }

}
