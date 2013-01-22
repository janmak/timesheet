package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.aplana.timesheet.enums.SystemRole.*;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum ProjectRole implements TSEnum {
    ADMINISTRATOR(12, "GM", "Генеральный директор,Заместитель генерального директора", "Администрация", SystemRole.MANAGER),
    DESIGN_ENGINEER(4, "KO", "Ведущий инженер-конструктор,Инженер-конструктор,Старший инженер-конструктор",
            "Конструктор", OTHER),
    TECHNICAL_WRITER(8, "TW", "Технический писатель", "Технический писатель", OTHER),
    NOT_DEFINED(10, "ND", "", "Не определена", OTHER),
    MANAGER(11, "MR", "Старший менеджер по качеству,Менеджер по качеству", "Менеджер", SystemRole.MANAGER),
    PROJECT_MANAGER(1, "MN", "Ведущий руководитель проекта,Руководитель проекта,Старший руководитель проекта," +
            "Администратор проекта", "Руководитель проекта", SystemRole.MANAGER),
    LEADING_ANALYST(13, "AL", "Ведущий инженер-аналитик", "Ведущий аналитик", SystemRole.MANAGER),
    HEAD_OF_DEVELOPMENT(3, "TL", "Руководитель группы разработки,Старший разработчик БД",
            "Руководитель группы разработки", SystemRole.MANAGER),
    DB_DEVELOPER(14, "DB", "Разработчик БД", "Разработчик БД", OTHER),
    HEAD_OF_CENTER(9, "DR", "Руководитель центра компетенции,Руководитель СК,Руководитель направления тестирования," +
            "Руководитель центра тестирования,Заместитель руководителя центра тестирования", "Руководитель центра", SystemRole.MANAGER),
    SYSTEM_ENGINEER_OLD(6, "SI", "Системный инженер,Старший системный инженер", "Системный инженер_old",
            SystemRole.SYSTEM_ENGINEER),
    ANALYST_OLD(2, "AN", "Инженер-аналитик,Старший инженер-аналитик,Ассистент инженера-аналитика", "Аналитик_old", OTHER),
    DEVELOPER_OLD(5, "DV", "Инженер-программист,Инженер-Программист,Специалист по разработке ПО,Инженер-разработчик," +
            "Ведущий инженер-программист,Ведущий инженер-разработчик,Старший инженер-программист," +
            "Ассистент инженера-программиста", "Разработчик_old", OTHER),
    TESTER_OLD(7, "TST", "Инженер-тестировщик,Старший инженер-тестировщик,Ведущий инженер-тестировщик," +
            "Специалист по тестированию ПО", "Тестировщик_old", OTHER),
    ANALYST(15, "AN", "Технический писатель,Инженер-аналитик,Старший инженер-аналитик,Ассистент инженера-аналитика," +
            "Администратор проекта", "Аналитик", OTHER),
    DEVELOPER(16, "DV", "Инженер-программист,Специалист по разработке ПО,Инженер-разработчик," +
            "Ведущий инженер-программист,Ведущий инженер-разработчик,Старший инженер-программист," +
            "Ассистент инженера-программиста,Руководитель группы разработки,Старший разработчик БД", "Разработчик", OTHER),
    TESTER(17, "TST", "Инженер-тестировщик,Старший инженер-тестировщик,Ведущий инженер-тестировщик," +
            "Специалист по тестированию ПО,Тест-аналитик", "Тестировщик", OTHER),
    SYSTEM_ENGINEER(18, "SI", "Системный инженер,Старший системный инженер", "Системный инженер",
            SystemRole.SYSTEM_ENGINEER),
    HEAD(19, "DR", "Руководитель центра компетенции,Руководитель СК,Руководитель направления тестирования," +
            "Руководитель центра тестирования,Заместитель руководителя центра тестирования," +
            "Ведущий руководитель проекта,Руководитель проекта,Старший руководитель проекта", "Руководитель", SystemRole.MANAGER)
    ;

    private int id;
    private String code;
    private String ldapTitle;
    private String name;
    private SystemRole sysRole;

    private ProjectRole( int id, String code, String ldapTitle, String name, SystemRole sysRole ) {
        this.id = id;
        this.code = code;
        this.ldapTitle = ldapTitle;
        this.name = name;
        this.sysRole = sysRole;
    }

    public String getCode() {
        return code;
    }

    public int getId() {
        return id;
    }

    public String getLdapTitle() {
        return ldapTitle;
    }

    public String getName() {
        return name;
    }

    public SystemRole getSysRole() {
        return sysRole;
    }

    public static ProjectRole getByCode( final String code ) {
        return Iterables.tryFind(Arrays.asList(ProjectRole.values()), new Predicate<ProjectRole>() {
            @Override
            public boolean apply(ProjectRole input) {
                return input.getCode().equals(code);
            }
        }).orNull();
    }

    public static ProjectRole getById(final int id) {
        return Iterables.tryFind(Arrays.asList(ProjectRole.values()), new Predicate<ProjectRole>() {
            @Override
            public boolean apply(ProjectRole input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
