package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.connection.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * dao
 */
abstract class JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private ConnectionPool connectionPool = null;

    JdbcDao(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sqlExpression) {
        try {
            return connection.prepareStatement(sqlExpression);
        } catch (SQLException exc) {
            logger.error("Prepared statement create error\n{}", exc.getMessage());
        }
        return null;
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
    public boolean execute(Connection connection, String sqlExpression) {
        try {
            Statement statement = connection.createStatement();
            return statement.execute(sqlExpression);
        } catch (SQLException exc) {
            logger.error("Statement execution error ({})", sqlExpression, exc);
        }
        return false;
    }

    @Override
    public final int insert(Connection connection, Object entityObject) {
        StringBuilder sqlExpression = new StringBuilder();
        sqlExpression.append("INSERT INTO \"").append(entityObject.getClass().getSimpleName()).append("\" (");
        sqlExpression.append(this.getFieldsNamesAndWildcardedValuesPartSqlExpression(entityObject.getClass())).append(");");
        PreparedStatement preparedStatement = this.createPreparedStatement(connection, sqlExpression.toString());
        if (preparedStatement == null) return 0;
        if (!this.setValuesInsteadWildcards(preparedStatement, entityObject))
            logger.error("Setting values to prepared statement error");
        try {
            int result = preparedStatement.executeUpdate();
            if (result > 0) logger.info("Entity '{}' inserted successfully", entityObject);
            else logger.info("Entity '{}' not inserted", entityObject);
            return result;
        } catch (SQLException exc) {
            logger.error("Prepared statement execution error", exc);
        } finally {
            this.closePreparedStatement(preparedStatement);
        }
        return 0;
    }
}
