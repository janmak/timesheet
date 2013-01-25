package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

import static com.aplana.timesheet.enums.Permissions.*;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum ProjectRole implements TSEnum {
    ADMINISTRATOR(12, "GM", "Генеральный директор,Заместитель генерального директора", "Администрация"),
    DESIGN_ENGINEER(4, "KO", "Ведущий инженер-конструктор,Инженер-конструктор,Старший инженер-конструктор",
            "Конструктор"),
    TECHNICAL_WRITER(8, "TW", "Технический писатель", "Технический писатель"),
    NOT_DEFINED(10, "ND", "", "Не определена"),
    MANAGER(11, "MR", "Старший менеджер по качеству,Менеджер по качеству", "Менеджер"),
    PROJECT_MANAGER(1, "MN", "Ведущий руководитель проекта,Руководитель проекта,Старший руководитель проекта," +
            "Администратор проекта", "Руководитель проекта"),
    LEADING_ANALYST(13, "AL", "Ведущий инженер-аналитик", "Ведущий аналитик"),
    HEAD_OF_DEVELOPMENT(3, "TL", "Руководитель группы разработки,Старший разработчик БД",
            "Руководитель группы разработки"),
    DB_DEVELOPER(14, "DB", "Разработчик БД", "Разработчик БД"),
    HEAD_OF_CENTER(9, "DR", "Руководитель центра компетенции,Руководитель СК,Руководитель направления тестирования," +
            "Руководитель центра тестирования,Заместитель руководителя центра тестирования", "Руководитель центра"),
    SYSTEM_ENGINEER_OLD(6, "SI", "Системный инженер,Старший системный инженер", "Системный инженер_old"),
    ANALYST_OLD(2, "AN", "Инженер-аналитик,Старший инженер-аналитик,Ассистент инженера-аналитика", "Аналитик_old"),
    DEVELOPER_OLD(5, "DV", "Инженер-программист,Инженер-Программист,Специалист по разработке ПО,Инженер-разработчик," +
            "Ведущий инженер-программист,Ведущий инженер-разработчик,Старший инженер-программист," +
            "Ассистент инженера-программиста", "Разработчик_old"),
    TESTER_OLD(7, "TST", "Инженер-тестировщик,Старший инженер-тестировщик,Ведущий инженер-тестировщик," +
            "Специалист по тестированию ПО", "Тестировщик_old"),
    ANALYST(15, "AN", "Технический писатель,Инженер-аналитик,Старший инженер-аналитик,Ассистент инженера-аналитика," +
            "Администратор проекта", "Аналитик"),
    DEVELOPER(16, "DV", "Инженер-программист,Специалист по разработке ПО,Инженер-разработчик," +
            "Ведущий инженер-программист,Ведущий инженер-разработчик,Старший инженер-программист," +
            "Ассистент инженера-программиста,Руководитель группы разработки,Старший разработчик БД", "Разработчик"),
    TESTER(17, "TST", "Инженер-тестировщик,Старший инженер-тестировщик,Ведущий инженер-тестировщик," +
            "Специалист по тестированию ПО,Тест-аналитик", "Тестировщик"),
    SYSTEM_ENGINEER(18, "SI", "Системный инженер,Старший системный инженер", "Системный инженер"),
    HEAD(19, "DR", "Руководитель центра компетенции,Руководитель СК,Руководитель направления тестирования," +
            "Руководитель центра тестирования,Заместитель руководителя центра тестирования," +
            "Ведущий руководитель проекта,Руководитель проекта,Старший руководитель проекта", "Руководитель")
    ;

    private int id;
    private String code;
    private String ldapTitle;
    private String name;

    private ProjectRole( int id, String code, String ldapTitle, String name) {
        this.id = id;
        this.code = code;
        this.ldapTitle = ldapTitle;
        this.name = name;
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
