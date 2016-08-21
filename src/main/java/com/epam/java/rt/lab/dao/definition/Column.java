package com.epam.java.rt.lab.dao.definition;

import java.lang.annotation.*;

/**
 * dao
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String columnName() default "";
    String columnValueType();
}
