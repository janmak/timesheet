package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum TypesOfActivityEnum implements TSEnum {

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

    private TypesOfActivityEnum(int id, String name) {
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
    public static boolean isEfficientActivity( TypesOfActivityEnum typeOfActivity ) {
        return  typeOfActivity == NON_PROJECT
                || typeOfActivity == PRESALE
                || typeOfActivity == PROJECT
                || typeOfActivity == PROJECT_PRESALE;
    }

    public static boolean isProjectActivity( TypesOfActivityEnum typeOfActivity ) {
        return     PROJECT == typeOfActivity
                || PRESALE == typeOfActivity
                || PROJECT_PRESALE == typeOfActivity;
    }

    public static boolean isProjectOrPresale(TypesOfActivityEnum typesOfActivityEnum) {
        return (typesOfActivityEnum == PROJECT || typesOfActivityEnum == PRESALE);
    }

    public static boolean isNotEfficientActivity( int id ) {
        return ! isEfficientActivity( id );
    }

    public static boolean isNotEfficientActivity( TypesOfActivityEnum typeOfActivity ) {
        return ! isEfficientActivity( typeOfActivity );
    }

    public static TypesOfActivityEnum getById( final Integer id ) {
        if (id == null) {return null;}
        return Iterables.tryFind(Arrays.asList(TypesOfActivityEnum.values()), new Predicate<TypesOfActivityEnum>() {
            @Override
            public boolean apply(@Nullable TypesOfActivityEnum input) {
                return input.getId() == id;
            }
        }).orNull();
    }

    public static List<Integer> getProjectPresaleNonProjectActivityId(){
        List<Integer> result = new ArrayList<Integer>();
        result.add(PROJECT.getId());
        result.add(PRESALE.getId());
        result.add(NON_PROJECT.getId());
        return result;
    }
}
