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
    CATEGORY_11(11, "Решение вопросов с персоналом"),
    CATEGORY_32(32, "Управление субподрядом"),
    CATEGORY_29(29, "Управление командой"),
    CATEGORY_28(28, "Подготовка и развертывание сборок"),
    CATEGORY_30(30, "Проектирование тестовых сценариев"),
    CATEGORY_33(33, "Разработка эксплуатационной документации"),
    CATEGORY_34(34, "Приемо-сдаточные испытания"),
    CATEGORY_35(35, "Анализ проблем"),
    OTHER(36, "Другое"),
    CATEGORY_37(37, "Подготовка ТКП"),
    CATEGORY_41(41, "Простой"),
    CATEGORY_1(1, "Управление проектом"),
    CATEGORY_8(8, "Управление инфраструктурой проекта"),
    CATEGORY_7(7, "Проведение тестирования"),
    CATEGORY_2(2, "Управление требованиями"),
    CATEGORY_3(3, "Проектирование"),
    CATEGORY_4(4, "Разработка"),
    CATEGORY_5(5, "Исследовательские работы"),
    CATEGORY_6(6, "Исправление ошибок"),
    CATEGORY_9(9, "Обучение"),
    CATEGORY_10(10, "Подбор персонала"),
    CATEGORY_26(26, "Совещания"),
    CATEGORY_27(27, "Ревью проектных документов");

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
