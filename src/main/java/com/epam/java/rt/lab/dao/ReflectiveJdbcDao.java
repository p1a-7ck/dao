package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.connection.ConnectionPool;
import com.epam.java.rt.lab.entity.RelationTable;
import com.epam.java.rt.lab.entity.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static javafx.scene.input.KeyCode.T;

/**
 * com.epam.java.rt.lab.dao
 */
public class ReflectiveJdbcDao extends JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(ReflectiveJdbcDao.class);
    private static final Map<Type, Method> preparedStatementMethodMap = new HashMap<>();

    static {
        try {
            //
            preparedStatementMethodMap.put(int.class, PreparedStatement.class.getMethod("setInt", int.class, int.class));
            preparedStatementMethodMap.put(Integer.class, PreparedStatement.class.getMethod("setInt", int.class, int.class));
            preparedStatementMethodMap.put(boolean.class, PreparedStatement.class.getMethod("setBoolean", int.class, boolean.class));
            preparedStatementMethodMap.put(Boolean.class, PreparedStatement.class.getMethod("setBoolean", int.class, boolean.class));
            preparedStatementMethodMap.put(byte.class, PreparedStatement.class.getMethod("setByte", int.class, byte.class));
            preparedStatementMethodMap.put(Byte.class, PreparedStatement.class.getMethod("setByte", int.class, byte.class));
            preparedStatementMethodMap.put(short.class, PreparedStatement.class.getMethod("setShort", int.class, short.class));
            preparedStatementMethodMap.put(Short.class, PreparedStatement.class.getMethod("setShort", int.class, short.class));
            preparedStatementMethodMap.put(long.class, PreparedStatement.class.getMethod("setLong", int.class, long.class));
            preparedStatementMethodMap.put(Long.class, PreparedStatement.class.getMethod("setLong", int.class, long.class));
            preparedStatementMethodMap.put(BigDecimal.class, PreparedStatement.class.getMethod("setBigDecimal", int.class, BigDecimal.class));
            preparedStatementMethodMap.put(double.class, PreparedStatement.class.getMethod("setDouble", int.class, double.class));
            preparedStatementMethodMap.put(Double.class, PreparedStatement.class.getMethod("setDouble", int.class, double.class));
            preparedStatementMethodMap.put(float.class, PreparedStatement.class.getMethod("setFloat", int.class, float.class));
            preparedStatementMethodMap.put(Float.class, PreparedStatement.class.getMethod("setFloat", int.class, float.class));
//            preparedStatementMethodMap.put(Time.class, PreparedStatement.class.getMethod("setTime", int.class, Time.class));
//            preparedStatementMethodMap.put(Date.class, PreparedStatement.class.getMethod("setDate", int.class, Date.class));
//            preparedStatementMethodMap.put(Timestamp.class, PreparedStatement.class.getMethod("setTimestamp", int.class, Timestamp.class));
            preparedStatementMethodMap.put(String.class, PreparedStatement.class.getMethod("setString", int.class, String.class));
            preparedStatementMethodMap.put(Blob.class, PreparedStatement.class.getMethod("setBlob", int.class, Blob.class));
            preparedStatementMethodMap.put(Clob.class, PreparedStatement.class.getMethod("setClob", int.class, Clob.class));
        } catch (NoSuchMethodException exc) {
            logger.error("Prepared statement methods initiating error", exc);
        }
    }

    public ReflectiveJdbcDao() {
    }

    @Override
    List<StringBuilder> getFieldNamesPartSqlExpressionFromEntity(Class<?> entityClass) {
        int fieldIndex = 0;
        List<StringBuilder> stringBuilderList = new ArrayList<>();
        stringBuilderList.add(new StringBuilder());
        List<Class<?>> classList = getClassList(entityClass);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = classList.size() - 1; i >= 0; i--) {
            for (Field field : classList.get(i).getDeclaredFields()) {
                TableColumn tableColumn = field.getAnnotation(TableColumn.class);
                if (tableColumn != null) {
                    if (fieldIndex > 0) stringBuilder.append(", ");
                    fieldIndex = fieldIndex + 1;
                    stringBuilder.append(tableColumn.value().replaceFirst("\\?", field.getName()));
                } else {
                    RelationTable relationTable = field.getAnnotation(RelationTable.class);
                    if (relationTable != null)
                        stringBuilderList.add(new StringBuilder(relationTable.value()));
                    // TODO: make relationTable as object
                    // TODO: check for existence of relation and exclude other relation
                    // TODO: update relation table
                }
            }
        }
        stringBuilderList.set(0, stringBuilder);
        return stringBuilderList;
    }

    private void getParentClass(List<Class<?>> classList) {
        Class<?> entityClass = classList.get(classList.size() - 1);
        Class<?> parentClass = entityClass.getSuperclass();
        if (parentClass != null) {
            classList.add(parentClass);
            getParentClass(classList);
        }
    }

    @Override
    StringBuilder getFieldsNamesAndWildcardedValuesPartSqlExpression(Class<?> entityClass) {
        int fieldIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        String[] fieldArray = getFieldNamesPartSqlExpressionFromEntity(entityClass).get(0).toString().split(", ");
        for (String field : fieldArray) {
            if (!field.contains("PRIMARY KEY")) {
                if (fieldIndex > 0) stringBuilder.append(", ");
                fieldIndex = fieldIndex + 1;
                stringBuilder.append(field.split(" ", 2)[0]);
            }
        }
        stringBuilder.append(") VALUES (");
        for (int i = 0; i < fieldIndex; i++) {
            if (i > 0) stringBuilder.append(", ");
            stringBuilder.append("?");
        }
        return stringBuilder;
    }

    @Override
    boolean setValuesInsteadWildcards(PreparedStatement preparedStatement, Object entityObject) {
        int fieldIndex = 0;
        List<Class<?>> classList = getClassList(entityObject.getClass());
        try {
            for (int i = classList.size() - 1; i >= 0; i--) {
                for (Field field : classList.get(i).getDeclaredFields()) {
                    TableColumn tableColumn = field.getAnnotation(TableColumn.class);
                    if (tableColumn != null) {
                        if (!tableColumn.value().contains("PRIMARY KEY")) {
                            fieldIndex = fieldIndex + 1;
                            setFieldValue(preparedStatement, fieldIndex, classList.get(i).cast(entityObject), field, tableColumn.value());
                        }
                    }
                }
            }
            return true;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("Wildcards replacing with values error", e);
        }
        return false;
    }

    private <T> void setFieldValue(PreparedStatement preparedStatement, int fieldIndex,
                               Object entityObject, Field field, String tableColumnValue)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (getValue(entityObject, field) == null) {
            //
        } else {
            if (!tableColumnValue.contains("REFERENCES")) {
                Method method = ReflectiveJdbcDao.preparedStatementMethodMap.get(field.getType());
                if (method == null) throw new IllegalAccessException();
                method.invoke(preparedStatement, fieldIndex, getValue(entityObject, field));
            } else {
                T primaryKeyValue = getPrimaryKeyValue(getValue(entityObject, field));
                Method method = ReflectiveJdbcDao.preparedStatementMethodMap.get(primaryKeyValue.getClass());
                if (method == null) throw new IllegalAccessException();
                method.invoke(preparedStatement, fieldIndex, primaryKeyValue);
            }
        }
    }

    private <T> T getPrimaryKeyValue(Object entityObject)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class<?>> classList = getClassList(entityObject.getClass());
        for (int i = classList.size() - 1; i >= 0; i--) {
            for (Field field : classList.get(i).getDeclaredFields()) {
                TableColumn tableColumn = field.getAnnotation(TableColumn.class);
                if (tableColumn != null) {
                    if (tableColumn.value().contains("PRIMARY KEY")) {
                        return getValue(classList.get(i).cast(entityObject), field);
                    }
                }
            }
        }
        throw new NoSuchElementException();
    }

    private void setValue(Object entityObject, Field field, Object entityValue) throws IllegalAccessException {
        if (field.isAccessible()) {
            field.set(entityObject, entityValue);
        } else {
            field.setAccessible(true);
            field.set(entityObject, entityValue);
            field.setAccessible(false);
        }
    }

    private <T> T getValue(Object entityObject, Field field) throws IllegalAccessException {
        T result;
        if (field.isAccessible()) {
            result = (T) field.get(entityObject);
        } else {
            field.setAccessible(true);
            result = (T) field.get(entityObject);
            field.setAccessible(false);
        }
        return result;
    }

    @Override
    void setEntityObjectGeneratedKeys(PreparedStatement preparedStatement, Object entityObject)
            throws NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException {
        List<Class<?>> classList = getClassList(entityObject.getClass());
        Method method;
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        int fieldIndex = 0;
        if (generatedKeys.next()) {
            for (int i = classList.size() - 1; i >= 0; i--) {
                for (Field field : classList.get(i).getDeclaredFields()) {
                    TableColumn tableColumn = field.getAnnotation(TableColumn.class);
                    if (tableColumn != null) {
                        if (tableColumn.value().contains("PRIMARY KEY")) {
                            fieldIndex = fieldIndex + 1;
                            setValue(classList.get(i).cast(entityObject), field, generatedKeys.getLong(fieldIndex));
                        }
                    } else {
                        RelationTable relationTable = field.getAnnotation(RelationTable.class);
                        if (relationTable != null) {
                            //
                        }
                    }
                }
            }
        }
    }

    private List<Class<?>> getClassList(Class<?> entityClass) {
        List<Class<?>> classList = new ArrayList<>();
        classList.add(entityClass);
        getParentClass(classList);
        return classList;
    }

    @Override
    <T> void insertRelations(Connection connection, Object entityObject)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Class<?>> classList = getClassList(entityObject.getClass());
        T value;
        T key;
        StringBuilder sqlExpression = new StringBuilder();
        for (int i = classList.size() - 1; i >= 0; i--) {
            for (Field field : classList.get(i).getDeclaredFields()) {
                RelationTable relationTable = field.getAnnotation(RelationTable.class);
                if (relationTable != null) {
                    value = getValue(entityObject, field);
                    System.out.println(value.getClass().getSimpleName());
                    if (value.getClass().getSimpleName().contains("List")) {
                        for (int j = 0; j < ((List) value).size(); j++) {
                            key = getPrimaryKeyValue(((List) value).get(j));
                            sqlExpression.append("SELECT * FROM ");

                        }
                    }
                }
            }
        }
    }


}

