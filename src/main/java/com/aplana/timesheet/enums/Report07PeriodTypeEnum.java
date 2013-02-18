package com.aplana.timesheet.enums;

import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public enum Report07PeriodTypeEnum {

    PERIOD_TYPE_MONTH(1),
    PERIOD_TYPE_KVARTAL(3),
    PERIOD_TYPE_HALF_YEAR(6),
    PERIOD_TYPE_YEAR(12);


    public static Report07PeriodTypeEnum getByMonthsCount(int monthsCount) {
        for (Report07PeriodTypeEnum anEnum : Report07PeriodTypeEnum.values()) {
            if (anEnum.monthsCount == monthsCount) {
                return anEnum;
            }
        }

        throw new NoSuchElementException();
    }

    private final int monthsCount;

    Report07PeriodTypeEnum(int monthsCount) {

        this.monthsCount = monthsCount;
    }

    public int getMonthsCount() {
        return monthsCount;
    }

    public Date getMaxDateOfPartOfYear(Calendar calendar) {
        if (monthsCount != PERIOD_TYPE_MONTH.monthsCount) {
            calendar.set(
                    Calendar.MONTH,
                    Math.min(
                            calendar.getActualMaximum(Calendar.MONTH),
                            monthsCount * (calendar.get(Calendar.MONTH) / monthsCount) + (monthsCount - 1)
                    )
            );
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        }

        return calendar.getTime();
    }
}
