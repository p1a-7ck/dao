package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.dao.definition.RelationEntity;
import com.epam.java.rt.lab.dao.factory.DaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * dao
 */
abstract class JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcDao.class);
    private static Properties PROPERTIES;

    JdbcDao() throws IOException {
        if (JdbcDao.PROPERTIES == null) {
            JdbcDao.PROPERTIES = new Properties();
            JdbcDao.PROPERTIES
                    .load(DaoFactory.class.getClassLoader().getResourceAsStream("crud.properties"));
        }
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sqlExpression, int magicConstant)
            throws SQLException {
        try {
            return connection.prepareStatement(sqlExpression, magicConstant);
        } catch (SQLException e) {
            logger.error("Prepared statement create error", e);
            throw e;
        }
    }

    private void closePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                logger.error("Prepared statement closing error ({})", e);
                throw e;
            }
        }
    }

    abstract List<ReflectiveJdbcDao.TableDefinition> getTableDefinitionList(Class<?> entityClass);

    abstract List<StringBuilder> getSqlPartNames(List<ReflectiveJdbcDao.TableDefinition> tableDefinitionList, boolean withDefinition);

    abstract StringBuilder getSqlPartNames(ReflectiveJdbcDao.TableDefinition tableDefinition, boolean withDefinition);

    abstract StringBuilder getSqlPartWildcardedValues(ReflectiveJdbcDao.TableDefinition tableDefinition);

    abstract ReflectiveJdbcDao.TableDefinition getTableDefinitionForNotNullObjectFields
            (ReflectiveJdbcDao.TableDefinition tableDefinition, Object entityObject) throws SQLException, IllegalAccessException;

    abstract <T> void setValuesToPreparedStatement
            (PreparedStatement preparedStatement, ReflectiveJdbcDao.TableDefinition tableDefinition, Object entityObject)
            throws SQLException, IllegalAccessException, InvocationTargetException;

    abstract void setGeneratedKeysToEntityObject(PreparedStatement preparedStatement,
                                                 ReflectiveJdbcDao.TableDefinition tableDefinition, Object entityObject) throws SQLException, IllegalAccessException;

    abstract List<RelationEntity> getRelationEntityList(List<ReflectiveJdbcDao.TableDefinition> tableDefinitionList, Object entityObject);

    @Override
    public final String generateCreateTableExpression(Class<?> entityClass) {
        List<ReflectiveJdbcDao.TableDefinition> tableDefinitionList = getTableDefinitionList(entityClass);
        if (tableDefinitionList == null) return null;
        List<StringBuilder> sqlPartNamesList = getSqlPartNames(tableDefinitionList, true);
        StringBuilder resultExpression = new StringBuilder();
        int replacePosition = 0;
        for (int i = 0; i < tableDefinitionList.size(); i++) {
            resultExpression.append(JdbcDao.PROPERTIES.getProperty("dao.operation.table.create"));
            replacePosition = resultExpression.indexOf("<?>", replacePosition);
            resultExpression.replace(replacePosition, replacePosition + 3,
                    "\"".concat(tableDefinitionList.get(i).name).concat("\""));
            replacePosition = resultExpression.indexOf("<?>", replacePosition);
            resultExpression.replace(replacePosition, replacePosition + 3,
                    sqlPartNamesList.get(i).toString());
        }
        return resultExpression.toString();
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

    private StringBuilder generateInsertIntoTableWildcardedExpression
            (ReflectiveJdbcDao.TableDefinition tableDefinition, Object entityObject)
            throws SQLException, IllegalAccessException {
        StringBuilder sqlPartNames = getSqlPartNames(tableDefinition, false);
        StringBuilder sqlPartWildcardedValues = getSqlPartWildcardedValues(tableDefinition);
        StringBuilder resultExpression = new StringBuilder
                (JdbcDao.PROPERTIES.getProperty("dao.operation.record.insert"));
        int replacePosition = resultExpression.indexOf("<?>", 0);
        resultExpression.replace(replacePosition, replacePosition + 3,
                "\"".concat(tableDefinition.name).concat("\""));
        replacePosition = resultExpression.indexOf("<?>", replacePosition);
        resultExpression.replace(replacePosition, replacePosition + 3,
                sqlPartNames.toString());
        replacePosition = resultExpression.indexOf("<?>", replacePosition);
        resultExpression.replace(replacePosition, replacePosition + 3,
                sqlPartWildcardedValues.toString());
        return resultExpression;
    }

    @Override
    public final int insert(Connection connection, Object entityObject)
            throws SQLException, IllegalAccessException, InvocationTargetException {
        List<ReflectiveJdbcDao.TableDefinition> tableDefinitionList = getTableDefinitionList(entityObject.getClass());
        if (tableDefinitionList == null) throw new SQLException("Table definition not found");
        ReflectiveJdbcDao.TableDefinition tableDefinition =
                getTableDefinitionForNotNullObjectFields(tableDefinitionList.get(0), entityObject);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getPreparedStatement(connection,
                    generateInsertIntoTableWildcardedExpression(tableDefinition, entityObject).toString(),
                    PreparedStatement.RETURN_GENERATED_KEYS);
            setValuesToPreparedStatement(preparedStatement, tableDefinition, entityObject);
            int result = preparedStatement.executeUpdate();
            if (result == 0) throw new SQLException("Insert error");
            setGeneratedKeysToEntityObject(preparedStatement, tableDefinitionList.get(0), entityObject);
            if (tableDefinitionList.size() > 1) result = result + insertRelations(tableDefinitionList, entityObject);
            return result;
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Prepared statement creating or execution error", e);
            throw e;
        } finally {
            closePreparedStatement(preparedStatement);
        }
    }

    private final int insertRelations (List<ReflectiveJdbcDao.TableDefinition> tableDefinitionList, Object entityObject) {
        List<RelationEntity> relationEntityList = getRelationEntityList(tableDefinitionList, entityObject);
        StringBuilder sqlExpression;
        for (RelationEntity relationEntity : relationEntityList) {
            // TODO: check for existence of relationEntity

            // TODO: generate insert into table expression

            // TODO: executeUpdate

            // TODO: add result value to global result

        }

        return 0;
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
