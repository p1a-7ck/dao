package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.dao.factory.DaoFactory;
import com.epam.java.rt.lab.dao.factory.JdbcConnectionFactory;
import com.epam.java.rt.lab.entity.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * com.epam.java.rt.lab.dao
 */
public class ReflectiveJdbcDao extends JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(ReflectiveJdbcDao.class);
    private static final Map<Type, Method> preparedStatementMethodMap = new HashMap<>();

    static {
        try {
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
        super(JdbcConnectionFactory.getInstance());
    }

    @Override
    StringBuilder getFieldsAndValuesPartSqlExpression(Class<?> entityClass) {
        boolean firstField = true;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder wildCardStringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (Field field : entityClass.getDeclaredFields()) {
            if (firstField) {
                firstField = false;
            } else {
                stringBuilder.append(", ");
                wildCardStringBuilder.append(", ");
            }
            stringBuilder.append(field.getName());
            wildCardStringBuilder.append("?");
        }
        return stringBuilder.append(") values (").append(wildCardStringBuilder).append(")");
    }

    @Override
    boolean setValueInsteadWildcards(PreparedStatement preparedStatement, Object entityObject) {
        int fieldIndex = 0;
        try {
            for (Field field : entityObject.getClass().getDeclaredFields()) {
                fieldIndex++;
                Method method = ReflectiveJdbcDao.preparedStatementMethodMap.get(field.getType());
                if (method == null) return false;
                if (field.isAccessible()) {
                    method.invoke(preparedStatement, fieldIndex, field.get(entityObject));
                } else {
                    field.setAccessible(true);
                    method.invoke(preparedStatement, fieldIndex, field.get(entityObject));
                    field.setAccessible(false);
                }
            }
            return true;
        } catch (InvocationTargetException | IllegalAccessException exc) {
            logger.error("Wildcards replacing with values error", exc);
        }
        return false;
    }

}
