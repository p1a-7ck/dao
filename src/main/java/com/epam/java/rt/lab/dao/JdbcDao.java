package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.dao.factory.JdbcConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * dao
 */
abstract class JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private JdbcConnectionFactory connectionFactory = null;
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;

    JdbcDao(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private boolean createConnection() {
        try {
            this.connection = this.connectionFactory.getConnection();
            return true;
        } catch (SQLException exc) {
            logger.error("Connection create error", exc);
        }
        return false;
    }

    private void closeConnection() {
        try {
            this.connectionFactory.releaseConnection(this.connection);
        } catch (SQLException exc) {
            logger.error("Connection closing error", exc);
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
