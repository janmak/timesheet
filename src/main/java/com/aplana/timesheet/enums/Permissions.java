package com.aplana.timesheet.enums;

/**
 * @author iziyangirov
 * @version 1.0
 */
public enum Permissions {

    PLAIN_PERMISSION(1, "Обычные права сотрудника"),
    RERPORTS_PERMISSION(2, "Формирование отчетов"),
    ADMIN_PERMISSION(3, "Администрирование системы");

    private int id;
    private String name;

    private Permissions( int id, String name) {
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

    public static Permissions getById( int id ) {
        for ( Permissions permission : Permissions.values() ) {
            if ( permission.getId() == id ) {
                return permission;
            }
        }
        return null;
    }
}
