package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum ProjectRolesEnum implements TSEnum {

    NOT_DEFINED(10, "ND", "", "Не определена"),
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

    private ProjectRolesEnum(int id, String code, String ldapTitle, String name) {
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

    public static ProjectRolesEnum getByCode( final String code ) {
        return Iterables.tryFind(Arrays.asList(ProjectRolesEnum.values()), new Predicate<ProjectRolesEnum>() {
            @Override
            public boolean apply(ProjectRolesEnum input) {
                return input.getCode().equals(code);
            }
        }).orNull();
    }

    public static ProjectRolesEnum getById(final Integer id) {
        return Iterables.tryFind(Arrays.asList(ProjectRolesEnum.values()), new Predicate<ProjectRolesEnum>() {
            @Override
            public boolean apply(ProjectRolesEnum input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
