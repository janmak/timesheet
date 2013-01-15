package com.aplana.timesheet.enums;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum TypeOfActivity {

    TIME_OFF_FOR_OVERTIME( 24, "Отгул за переработки" ),
    VACATION( 16, "Отпуск" ),
    DISEASE( 17, "Болезнь" ),
    HOLIDAY( 18, "Нерабочий день" ),
    PROJECT( 12, "Проект" ),
    PRESALE( 13, "Пресейл" ),
    COMPENSATORY_HOLIDAY( 15, "Отгул" ),
    NON_PROJECT( 14, "Непроектная" ),
    PROJECT_PRESALE( 42, "Проектный пресейл" )
    ;

    private int id;
    private String name;

    private TypeOfActivity( int id, String name ) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TypeOfActivity getById( int id ) {
        for ( TypeOfActivity typeOfActivity : TypeOfActivity.values() ) {
            if ( typeOfActivity.getId() == id ) {
                return typeOfActivity;
            }
        }
        return null;
    }
}
