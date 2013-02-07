package com.aplana.timesheet.enums;

/**
 * @author iziyangirov
 * @version 1.0
 */
public enum PermissionsEnum {

    PLAIN_PERMISSION(1, "Обычные права сотрудника"),
    RERPORTS_PERMISSION(2, "Формирование отчетов"),
    ADMIN_PERMISSION(3, "Администрирование системы"),
    VIEW_ILLNESS_BUSINESS_TRIP(4, "Просмотр болезней и командировок всех сотрудников"),
    CHANGE_ILLNESS_BUSINESS_TRIP(5, "Изменение болезней и командировок всех сотрудников");

    private int id;
    private String name;

    private PermissionsEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static PermissionsEnum getById( int id ) {
        for ( PermissionsEnum permission : PermissionsEnum.values() ) {
            if ( permission.getId() == id ) {
                return permission;
            }
        }
        return null;
    }
}
