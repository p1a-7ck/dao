package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.dao.factory.DaoFactory;
import com.epam.java.rt.lab.dao.factory.H2TableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * dao
 */
abstract class JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private Class<? extends DaoFactory> factoryClass;
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;

    JdbcDao(Class<? extends DaoFactory> factoryClass) {
        this.factoryClass = factoryClass;
    }

    private boolean createConnection() {
        try {
            Method method = this.factoryClass.getMethod("createConnection");
            this.connection = (Connection) method.invoke(null);
            return true;
        } catch (NoSuchMethodException exc) {
            logger.error("Static method 'createConnection' for factoryClass '{}' not found\n{}",
                    this.factoryClass.getSimpleName(), exc.getMessage());
        } catch (IllegalAccessException exc) {
            logger.error("Static method 'createConnection' for factoryClass '{}' access error\n{}",
                    this.factoryClass.getSimpleName(), exc.getMessage());
        } catch (InvocationTargetException exc) {
            logger.error(exc.getMessage());
        }
        return false;
    }

    private void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException exc) {
            logger.error("Connection closing error\n{}", exc.getMessage());
        }
    }

    private boolean createPreparedStatement(String sqlExpression) {
        try {
            this.preparedStatement = this.connection.prepareStatement(sqlExpression);
            return true;
        } catch (SQLException exc) {
            logger.error("Prepared statement create error\n{}", exc.getMessage());
        }
        return false;
    }

    private void closePreparedStatement() {
        try {
            this.preparedStatement.close();
        } catch (SQLException exc) {
            logger.error("Prepared statement closing error ({})", exc.getMessage());
        }
    }

    abstract StringBuilder getFieldsAndValuesPartSqlExpression(Class<?> entityClass);

    abstract boolean setValueInsteadWildcards(PreparedStatement preparedStatement, Object entityObject);

    @Override
    public final boolean createTable(Class<?> entityClass) {
        if (!this.createConnection()) return false;
        StringBuilder sqlExpression = H2TableFactory.getCreateTableExpression(entityClass);
        logger.info(sqlExpression.toString());
        if (!this.createPreparedStatement(sqlExpression.toString())) {
            this.closeConnection();
            return false;
        }
        try {
            boolean result = this.preparedStatement.execute();
            if (result) logger.info("Table create for entity '{}' sql expression executed successfully",
                    entityClass.getSimpleName());
            else logger.info("Table create for entity '{}' sql expression not executed",
                    entityClass.getSimpleName());
            return result;
        } catch (SQLException exc) {
            logger.error("Prepared statement execution error\n{}", exc.getMessage());
        } finally {
            this.closePreparedStatement();
            this.closeConnection();
        }
        return false;
    }

    @Override
    public final int insert(Object entityObject) {
        StringBuilder sqlExpression = new StringBuilder();
        if (!this.createConnection()) return 0;
        sqlExpression.append("INSERT INTO \"").append(entityObject.getClass().getSimpleName()).append("\" ");
        sqlExpression.append(this.getFieldsAndValuesPartSqlExpression(entityObject.getClass())).append(";");
        logger.info(sqlExpression.toString());
        if (!this.createPreparedStatement(sqlExpression.toString())) {
            this.closeConnection();
            return 0;
        }
        if (!this.setValueInsteadWildcards(this.preparedStatement, entityObject))
            logger.error("Setting values to prepared statement error");
        try {
            int result = this.preparedStatement.executeUpdate();
            if (result > 0) logger.info("Entity '{}' inserted successfully", entityObject);
            else logger.info("Entity '{}' not inserted", entityObject);
            return result;
        } catch (SQLException exc) {
            logger.error("Prepared statement execution error\n{}", exc.getMessage());
        } finally {
            this.closePreparedStatement();
            this.closeConnection();
        }
        return 0;
    }

}
