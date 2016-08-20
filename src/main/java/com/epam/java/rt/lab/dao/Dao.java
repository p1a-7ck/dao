package com.epam.java.rt.lab.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * com.epam.java.rt.lab.dao
 */
public interface Dao {
    String createTableExpression(Class<?> entityClass);

    boolean execute(Connection connection, String sqlExpression) throws SQLException;
    //CRUD
    int insert(Connection connection, Object entityObject) throws SQLException;

    ResultSet query(Connection connection, String sqlExpression) throws SQLException;
}
