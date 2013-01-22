package com.aplana.timesheet.util;

public enum Job {
    PROJECT_MANAGER(1), ANALYST(2), CENTER_MANAGER(9), DEV_TEAM_LEADER(3), ENGINEER(4), DEVELOPER(5), SYSTEM_ENGINEER(6),
    TESTER(7), TECHNIK_WRITER(8), UNKNOW(10), MANAGER(11), ADMINISTARTION(12), SENIOR_ANALYST(13), DEVELOPER_DATABASE(14);

    private Integer code;

    private Job (Integer value) {
        this.code = value;
    }

    public Integer getCode() {
        return this.code;
    }
}
