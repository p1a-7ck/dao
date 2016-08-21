package com.epam.java.rt.lab.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * com.epam.java.rt.lab.dao
 */
public interface Dao {
    String generateCreateTableExpression(Class<?> entityClass);

    boolean execute(Connection connection, String sqlExpression) throws SQLException;
    //CRUD
    int insert(Connection connection, Object entityObject) throws SQLException, IllegalAccessException, InvocationTargetException;

    ResultSet query(Connection connection, String sqlExpression) throws SQLException;
}
