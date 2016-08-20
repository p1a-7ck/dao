package com.epam.java.rt.lab.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;

/**
 * dao
 */
abstract class JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);

    JdbcDao() {
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sqlExpression, int magicConstant)
            throws SQLException {
        try {
            return connection.prepareStatement(sqlExpression, magicConstant);
        } catch (SQLException e) {
            logger.error("Prepared statement create error", e);
            throw e;
        }
    }

    private void closePreparedStatement(PreparedStatement preparedStatement) {
        try {
            preparedStatement.close();
        } catch (SQLException exc) {
            logger.error("Prepared statement closing error ({})", exc.getMessage());
        }
    }

    abstract List<StringBuilder> getFieldNamesPartSqlExpressionFromEntity(Class<?> entityClass);

    abstract StringBuilder getFieldsNamesAndWildcardedValuesPartSqlExpression(Class<?> entityClass);

    abstract boolean setValuesInsteadWildcards(PreparedStatement preparedStatement, Object entityObject);

    abstract void setEntityObjectGeneratedKeys(PreparedStatement preparedStatement, Object entityObject) throws NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException;

    abstract <T> void insertRelations(Connection connection, Object entityObject) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    @Override
    public final String createTableExpression(Class<?> entityClass) {
        StringBuilder stringBuilder = new StringBuilder();
        List<StringBuilder> stringBuilderList = getFieldNamesPartSqlExpressionFromEntity(entityClass);
        stringBuilder.append("CREATE TABLE IF NOT EXISTS \"").append(entityClass.getSimpleName()).append("\" (");
        stringBuilder.append(stringBuilderList.get(0)).append(");");
        String[] relationTableList;
        for (int i = 1; i < stringBuilderList.size(); i++) {
            relationTableList = stringBuilderList.get(i).toString().split(" ", 2);
            stringBuilder.append(" CREATE TABLE IF NOT EXISTS ").append(relationTableList[0]).append(" (");
            stringBuilder.append(relationTableList[1]).append(");");
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean execute(Connection connection, String sqlExpression) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            return statement.execute(sqlExpression);
        } catch (SQLException e) {
            logger.error("Statement execution error ({})", sqlExpression, e);
            throw e;
        }
    }

    @Override
    public final int insert(Connection connection, Object entityObject) throws SQLException {
        StringBuilder sqlExpression = new StringBuilder();
        sqlExpression.append("INSERT INTO \"").append(entityObject.getClass().getSimpleName()).append("\" (");
        sqlExpression.append(this.getFieldsNamesAndWildcardedValuesPartSqlExpression(entityObject.getClass())).append(");");
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.createPreparedStatement
                    (connection, sqlExpression.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
            if (!this.setValuesInsteadWildcards(preparedStatement, entityObject))
                logger.error("Setting values to prepared statement error");
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                setEntityObjectGeneratedKeys(preparedStatement, entityObject);
                insertRelations(connection, entityObject);
                logger.info("Entity '{}' inserted successfully", entityObject);
            } else {
                logger.info("Entity '{}' not inserted", entityObject);
            }
            return result;
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Prepared statement creating or execution error", e);
            throw new SQLException(e.getMessage());
        } finally {
            if (preparedStatement != null) this.closePreparedStatement(preparedStatement);
        }
    }

    @Override
    public final ResultSet query(Connection connection, String sqlExpression) throws SQLException {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sqlExpression);
        } catch (SQLException e) {
            logger.error("Statement querying error ({})", sqlExpression, e);
            throw e;
        }
    }
}
