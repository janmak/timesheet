package com.aplana.timesheet.util;

public enum Region {
    ETC(1), UFA(2), MOSCOW(3), PERM(4), NOVGOROD(5);

    private Integer code;

    private Region (Integer value) {
        this.code = value;
    }

    public Integer getCode() {
        return this.code;
    }
}
