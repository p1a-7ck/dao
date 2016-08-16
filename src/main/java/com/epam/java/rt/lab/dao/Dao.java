package com.epam.java.rt.lab.dao;

/**
 * com.epam.java.rt.lab.dao
 */
public interface Dao {
    boolean createTable(Class<?> entityClass);

    <E> int insert(E entityObject);
}
