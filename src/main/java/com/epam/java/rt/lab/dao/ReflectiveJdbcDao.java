package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.dao.definition.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.epam.java.rt.lab.dao
 */
public class ReflectiveJdbcDao extends JdbcDao implements Dao {
    private static final Logger logger = LoggerFactory.getLogger(ReflectiveJdbcDao.class);
    private static final Map<Type, Method> statementMethodMap = new HashMap<>();

    public ReflectiveJdbcDao() throws IOException, ClassNotFoundException, NoSuchMethodException {
        super();
        if (statementMethodMap.size() == 0) {
            statementMethodMap.put(int.class, PreparedStatement.class.getMethod("setInt", int.class, int.class));
            statementMethodMap.put(Integer.class, PreparedStatement.class.getMethod("setInt", int.class, int.class));
            statementMethodMap.put(boolean.class, PreparedStatement.class.getMethod("setBoolean", int.class, boolean.class));
            statementMethodMap.put(Boolean.class, PreparedStatement.class.getMethod("setBoolean", int.class, boolean.class));
            statementMethodMap.put(byte.class, PreparedStatement.class.getMethod("setByte", int.class, byte.class));
            statementMethodMap.put(Byte.class, PreparedStatement.class.getMethod("setByte", int.class, byte.class));
            statementMethodMap.put(short.class, PreparedStatement.class.getMethod("setShort", int.class, short.class));
            statementMethodMap.put(Short.class, PreparedStatement.class.getMethod("setShort", int.class, short.class));
            statementMethodMap.put(long.class, PreparedStatement.class.getMethod("setLong", int.class, long.class));
            statementMethodMap.put(Long.class, PreparedStatement.class.getMethod("setLong", int.class, long.class));
            statementMethodMap.put(BigDecimal.class, PreparedStatement.class.getMethod("setBigDecimal", int.class, BigDecimal.class));
            statementMethodMap.put(double.class, PreparedStatement.class.getMethod("setDouble", int.class, double.class));
            statementMethodMap.put(Double.class, PreparedStatement.class.getMethod("setDouble", int.class, double.class));
            statementMethodMap.put(float.class, PreparedStatement.class.getMethod("setFloat", int.class, float.class));
            statementMethodMap.put(Float.class, PreparedStatement.class.getMethod("setFloat", int.class, float.class));
//            statementMethodMap.put(Time.class, PreparedStatement.class.getMethod("setTime", int.class, Time.class));
//            statementMethodMap.put(Date.class, PreparedStatement.class.getMethod("setDate", int.class, Date.class));
//            statementMethodMap.put(Timestamp.class, PreparedStatement.class.getMethod("setTimestamp", int.class, Timestamp.class));
            statementMethodMap.put(String.class, PreparedStatement.class.getMethod("setString", int.class, String.class));
            statementMethodMap.put(Blob.class, PreparedStatement.class.getMethod("setBlob", int.class, Blob.class));
            statementMethodMap.put(Clob.class, PreparedStatement.class.getMethod("setClob", int.class, Clob.class));
        }
    }

    private List<Class<?>> reflectClass(Class<?> entityClass) {
        List<Class<?>> classList = new ArrayList<>();
        classList.add(0, entityClass);
        reflectClass(classList);
        return classList;
    }

    private void reflectClass(List<Class<?>> classList) {
        Class<?> superClass = classList.get(0).getSuperclass();
        if (superClass != null) {
            classList.add(0, superClass);
            reflectClass(classList);
        }
    }

    @Override
    List<TableDefinition> getTableDefinitionList(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null) {
            List<TableDefinition> tableDefinitionList = new ArrayList<>();
            TableDefinition tableDefinition = TableDefinition.setName
                    ((table.tableName().length() == 0) ?
                            entityClass.getSimpleName() : table.tableName());
            List<Class<?>> classList = reflectClass(entityClass);
            Column column;
            ColumnRelation columnRelation;
            ColumnRelationMany columnRelationMany;
            ColumnDefinition columnDefinition;
            for (Class<?> classItem : classList) {
                for (Field field : classItem.getDeclaredFields()) {
                    column = field.getAnnotation(Column.class);
                    if (column != null) {
                        columnDefinition = ColumnDefinition.setName
                                ((column.columnName().length() == 0) ?
                                        field.getName() : column.columnName());
                        columnDefinition.definition.append(column.columnValueType());
                        columnDefinition.fieldDefinition = new FieldDefinition(classItem, field);
                        tableDefinition.columnDefinitionList.add(columnDefinition);
                    } else {
                        columnRelation = field.getAnnotation(ColumnRelation.class);
                        if (columnRelation != null) {
                            columnDefinition = ColumnDefinition.setName
                                    ((columnRelation.columnName().length() == 0) ?
                                            field.getName() : columnRelation.columnName());
                            columnDefinition.definition.append(columnRelation.columnValueType());
                            if (columnRelation.columnTableColumnName().length() != 0) columnDefinition.definition
                                    .append(" REFERENCES ").append(columnRelation.columnTableName())
                                    .append("(").append(columnRelation.columnTableColumnName()).append(")");
                            columnDefinition.fieldDefinition = new FieldDefinition(classItem, field);
                            tableDefinition.columnDefinitionList.add(columnDefinition);
                        } else {
                            columnRelationMany = field.getAnnotation(ColumnRelationMany.class);
                            if (columnRelationMany != null) {
                                TableDefinition relationTableDefinition = TableDefinition.setName
                                        (columnRelationMany.tableName());
                                for (int i = 0; i < columnRelationMany.tableColumnNames().length; i++) {
                                    columnDefinition = ColumnDefinition.setName
                                            (columnRelationMany.tableColumnNames()[i]);
                                    columnDefinition.definition.append(columnRelationMany.tableColumnValueTypes()[i]);
                                    if (columnRelationMany.tableColumnReferencesTableName()[i].length() != 0) {
                                        columnDefinition.definition.append(" REFERENCES ")
                                                .append(columnRelationMany.tableColumnReferencesTableName()[i]);
                                        if (columnRelationMany.tableColumnReferencesTableColumnName()[i].length() != 0)
                                            columnDefinition.definition.append("(")
                                                    .append(columnRelationMany.tableColumnReferencesTableColumnName()[i]).append(")");
                                    }
                                    relationTableDefinition.columnDefinitionList.add(columnDefinition);
                                }
                                tableDefinitionList.add(relationTableDefinition);
                            }
                        }
                    }
                }
            }
            tableDefinitionList.add(0, tableDefinition);
            return tableDefinitionList;
        }
        return null;
    }

    @Override
    List<StringBuilder> getSqlPartNames(List<TableDefinition> tableDefinitionList, boolean withDefinition) {
        if (tableDefinitionList == null) return null;
        List<StringBuilder> sqlPartNamesList = new ArrayList<>();
        for (TableDefinition tableDefinition : tableDefinitionList)
            sqlPartNamesList.add(getSqlPartNames(tableDefinition, withDefinition));
        return sqlPartNamesList;
    }

    @Override
    StringBuilder getSqlPartNames(TableDefinition tableDefinition, boolean withDefinition) {
        if (tableDefinition == null) return null;
        boolean firstItem;
        firstItem = true;
        StringBuilder names = new StringBuilder();
        for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitionList) {
            names.append(firstItem ? "" : ", ");
            names.append(columnDefinition.name);
            if (withDefinition) names.append(" ").append(columnDefinition.definition);
            firstItem = false;
        }
        return names;
    }

    @Override
    StringBuilder getSqlPartWildcardedValues(TableDefinition tableDefinition) {
        if (tableDefinition == null) return null;
        boolean firstItem = true;
        StringBuilder sqlPartWildcardedValues = new StringBuilder();
        for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitionList) {
            sqlPartWildcardedValues.append(firstItem ? "?" : ", ?");
            firstItem = false;
        }
        return sqlPartWildcardedValues;
    }

    private <T> void setFieldValue(Object entityObject, Field field, T fieldValue) throws IllegalAccessException {
        if (field.isAccessible()) {
            field.set(entityObject, fieldValue);
        } else {
            field.setAccessible(true);
            field.set(entityObject, fieldValue);
            field.setAccessible(false);
        }
    }

    private <T> T getFieldValue(Object entityObject, Field field) throws IllegalAccessException {
        if (field.isAccessible()) {
            return (T) field.get(entityObject);
        } else {
            field.setAccessible(true);
            T result = (T) field.get(entityObject);
            field.setAccessible(false);
            return result;
        }
    }

    @Override
    TableDefinition getTableDefinitionForNotNullObjectFields(TableDefinition tableDefinition, Object entityObject)
            throws SQLException, IllegalAccessException {
        if (tableDefinition == null) throw new SQLException();
        TableDefinition resultTableDefinition = TableDefinition.setName(tableDefinition.name);
        ColumnDefinition resultColumnDefinition;
        for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitionList) {
            if (columnDefinition.fieldDefinition != null) {
                resultColumnDefinition = ColumnDefinition.copyOf(columnDefinition);
                resultColumnDefinition.fieldDefinition.value =
                        getFieldValue(entityObject, resultColumnDefinition.fieldDefinition.field);
                if (resultColumnDefinition.fieldDefinition.value != null)
                    resultTableDefinition.columnDefinitionList.add(resultColumnDefinition);
            }
        }
        return resultTableDefinition;
    }

    private <T> T getPrimaryKeyValue(Object entityObject, String definition) throws IllegalAccessException {
        List<TableDefinition> tableDefinitionList = getTableDefinitionList(entityObject.getClass());
        int index = definition.indexOf("REFERENCES");
        index = definition.indexOf("(", index);
        if (index > 0) {
            definition = definition.substring(index + 1, definition.indexOf(")", index));
        } else {
            definition = "";
        }
        for (ColumnDefinition columnDefinition : tableDefinitionList.get(0).columnDefinitionList) {
            if (definition.length() == 0) {
                if (columnDefinition.definition.toString().contains("PRIMARY KEY")) {
                    return getFieldValue(entityObject, columnDefinition.fieldDefinition.field);
                }
            } else {
                if (columnDefinition.name.equals(definition)) {
                    return getFieldValue(entityObject, columnDefinition.fieldDefinition.field);
                }
            }
        }
        throw new IllegalAccessException();
    }

    @Override
    <T> void setValuesToPreparedStatement(PreparedStatement preparedStatement,
                                          TableDefinition tableDefinition, Object entityObject)
            throws SQLException, IllegalAccessException, InvocationTargetException {
        if (tableDefinition == null) throw new SQLException();
        T primaryKeyValue;
        int fieldIndex = 0;
        for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitionList) {
            fieldIndex = fieldIndex + 1;
            if (columnDefinition.definition.toString().contains("REFERENCES")) {
                primaryKeyValue = getPrimaryKeyValue
                        (getFieldValue(entityObject, columnDefinition.fieldDefinition.field),
                                columnDefinition.definition.toString());
                Method method = ReflectiveJdbcDao.statementMethodMap.get(primaryKeyValue.getClass());
                if (method == null) throw new IllegalAccessException("Method not found");
                method.invoke(preparedStatement, fieldIndex, primaryKeyValue);
            } else {
                Method method = ReflectiveJdbcDao.statementMethodMap.get(columnDefinition.fieldDefinition.field.getType());
                if (method == null) throw new IllegalAccessException("Method not found");
                method.invoke(preparedStatement, fieldIndex,
                        getFieldValue(entityObject, columnDefinition.fieldDefinition.field));

            }
        }
    }

    @Override
    void setGeneratedKeysToEntityObject(PreparedStatement preparedStatement,
                                        TableDefinition tableDefinition, Object entityObject)
            throws SQLException, IllegalAccessException {
        if (tableDefinition == null) throw new SQLException();
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int fieldIndex = 0;
            String value;
            for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitionList) {
                if (columnDefinition.definition.toString().contains("PRIMARY KEY")) {
                    fieldIndex = fieldIndex + 1;
                    // TODO : make reflective get value instead .getString()
                    value = generatedKeys.getString(fieldIndex);
                    if (columnDefinition.fieldDefinition.fieldClass == Long.class) {
                        setFieldValue(entityObject, columnDefinition.fieldDefinition.field, Long.valueOf(value));
                    }
                }
            }
        }
    }

    @Override
    List<RelationEntity> getRelationEntityList(List<TableDefinition> tableDefinitionList, Object entityObject) {
        return null;
    }

    public static class TableDefinition {
        String name;
        List<ColumnDefinition> columnDefinitionList = new ArrayList<>();

        static TableDefinition setName(String name) {
            TableDefinition tableDefinition = new TableDefinition();
            tableDefinition.name = name;
            return tableDefinition;
        }
    }

    public static class ColumnDefinition {
        String name;
        StringBuilder definition = new StringBuilder();
        FieldDefinition fieldDefinition;

        static ColumnDefinition setName(String name) {
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.name = name;
            return columnDefinition;
        }

        static ColumnDefinition copyOf(ColumnDefinition columnDefinition) {
            ColumnDefinition copyColumnDefinition = new ColumnDefinition();
            copyColumnDefinition.name = columnDefinition.name;
            copyColumnDefinition.definition.append(columnDefinition.definition);
            copyColumnDefinition.fieldDefinition = FieldDefinition.copyOf(columnDefinition.fieldDefinition);
            return copyColumnDefinition;
        }

        @Override
        public String toString() {
            return "ColumnDefinition{" +
                    "name='" + name + '\'' +
                    ", definition=" + definition +
                    ", fieldDefinition=" + fieldDefinition +
                    '}';
        }
    }

    public static class FieldDefinition<T> {
        Class<?> fieldClass;
        Field field;
        T value;

        FieldDefinition(Class<?> fieldClass, Field field) {
            this.fieldClass = fieldClass;
            this.field = field;
        }

        static FieldDefinition copyOf(FieldDefinition fieldDefinition) {
            FieldDefinition resultFieldDefinition = new FieldDefinition(fieldDefinition.fieldClass, fieldDefinition.field);
            resultFieldDefinition.value = fieldDefinition.value;
            return resultFieldDefinition;
        }

        @Override
        public String toString() {
            return "FieldDefinition{" +
                    "fieldClass=" + fieldClass +
                    ", field=" + field +
                    ", value=" + value +
                    '}';
        }
    }
}

