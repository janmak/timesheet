package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum TypeOfActivity {

    TIME_OFF_FOR_OVERTIME( 24, "Отгул за переработки" ),
    VACATION( 16, "Отпуск" ),
    ILLNESS( 17, "Болезнь" ),
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

    /**
     * Проверяет, входит ли тип активности в перечень тех, по которым нужно считать время работы
     * @param id ид типа работы
     * @return
     */
    public static boolean isEfficientActivity( int id ) {
        return isEfficientActivity( getById( id ) );
    }

    /**
     * Проверяет, входит ли тип активности в перечень тех, по которым нужно считать время работы
     * @param typeOfActivity тип работы
     * @return
     */
    public static boolean isEfficientActivity( TypeOfActivity typeOfActivity ) {
        return  typeOfActivity == NON_PROJECT
                || typeOfActivity == PRESALE
                || typeOfActivity == PROJECT
                || typeOfActivity == PROJECT_PRESALE;
    }

    public static boolean isProjectActivity( TypeOfActivity typeOfActivity ) {
        return     PROJECT == typeOfActivity
                || PRESALE == typeOfActivity
                || PROJECT_PRESALE == typeOfActivity;
    }

    public static boolean isNotEfficientActivity( int id ) {
        return ! isEfficientActivity( id );
    }

    public static boolean isNotEfficientActivity( TypeOfActivity typeOfActivity ) {
        return ! isEfficientActivity( typeOfActivity );
    }

    public static TypeOfActivity getById( final int id ) {
        return Iterables.tryFind(Arrays.asList(TypeOfActivity.values()), new Predicate<TypeOfActivity>() {
            @Override
            public boolean apply(@Nullable TypeOfActivity input) {
                return input.getId() == id;
            }
        }).orNull();
    }
}
