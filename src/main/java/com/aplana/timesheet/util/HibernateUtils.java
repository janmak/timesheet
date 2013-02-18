package com.aplana.timesheet.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class HibernateUtils {

    private static final HashSet<Class<? extends Annotation>> ANNOTATIONS = newHashSet(
            ManyToOne.class,
            OneToOne.class,
            ManyToMany.class,
            OneToMany.class
    );

    public static void fetchAllFields(Object entity) {
        final Class<?> aClass = entity.getClass();
        final Set<String> fieldsNamesWithLazyFetchType = getFieldsNamesWithLazyFetchType(aClass);

        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(aClass);

            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (fieldsNamesWithLazyFetchType.contains(propertyDescriptor.getName())) {
                    propertyDescriptor.getReadMethod().invoke(entity);
                }
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private static Set<String> getFieldsNamesWithLazyFetchType(Class aClass) {
        return FluentIterable.from(Arrays.asList(aClass.getDeclaredFields())).
            filter(new Predicate<Field>() {
                @Override
                public boolean apply(@Nullable Field field) {
                    for (Class<? extends Annotation> annotation : ANNOTATIONS) {
                        if (field.isAnnotationPresent(annotation)) {
                            final Annotation fieldAnnotation = field.getAnnotation(annotation);

                            try {
                                final Object fetch = annotation.getMethod("fetch").invoke(fieldAnnotation);

                                return (fetch == FetchType.LAZY);
                            } catch (Exception e) {
                                // do nothing
                            }

                            return true;
                        }
                    }

                    return false;
                }
            }).
            transform(new Function<Field, String>() {
                @Nullable
                @Override
                public String apply(@Nullable Field field) {
                    return field.getName();
                }
            }).
            toSet();
    }

}
