package com.epam.java.rt.lab.dao;

import java.sql.Connection;

/**
 * com.epam.java.rt.lab.dao
 */
public interface Dao {
    String createTableExpression(Class<?> entityClass);

    boolean execute(Connection connection, String sqlExpression);
    //CRUD
    int insert(Connection connection, Object entityObject);
}
