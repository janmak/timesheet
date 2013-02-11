package com.aplana.timesheet.enums;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Arrays;

/**
 * @author eshangareev
 * @version 1.0
 */
public enum CategoriesOfActivityEnum implements TSEnum {
    //TODO переименовать
    CATEGORY_12(12, "Проект"),
    CATEGORY_13(13, "Пресейл"),
    CATEGORY_14(14, "Непроектная"),
    CATEGORY_15(15, "Отгул"),
    CATEGORY_16(16, "Отпуск"),
    CATEGORY_17(17, "Болезнь"),
    CATEGORY_18(18, "Нерабочий день"),
    CATEGORY_24(24, "Отгул за переработки"),
    CATEGORY_42(42, "Проектный пресейл");


    private int id;
    private String name;

    private CategoriesOfActivityEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public static CategoriesOfActivityEnum getById(final int id) {
        return Iterables.find(Arrays.asList(CategoriesOfActivityEnum.values()),new Predicate<CategoriesOfActivityEnum>() {
            @Override
            public boolean apply(CategoriesOfActivityEnum input) {
                return input.getId() == id;
            }
        } );
    }
}
