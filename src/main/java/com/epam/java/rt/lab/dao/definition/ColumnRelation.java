package com.epam.java.rt.lab.dao.definition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * dao
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnRelation {
    String columnName() default "";
    String columnValueType();
    String columnTableName();
    String columnTableColumnName() default "";
}
