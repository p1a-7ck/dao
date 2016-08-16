package com.epam.java.rt.lab.dao.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * dao
 */
public class H2TableFactory {
    private static final Logger logger = LoggerFactory.getLogger(H2TableFactory.class);
    private static Properties PROPERTIES;

    static {
        if (H2TableFactory.PROPERTIES == null) {
            try {
                H2TableFactory.PROPERTIES = new Properties();
                H2TableFactory.PROPERTIES
                        .load(H2TableFactory.class.getClassLoader().getResourceAsStream("h2types.properties"));
            } catch (IOException exc) {
                logger.error("File 'h2types.properties' IO error", exc);
            }
        }
    }

    private H2TableFactory() {
    }

    public static String getDbType(Field field) {
        return H2TableFactory.PROPERTIES.getProperty(field.getType().getSimpleName().toUpperCase());
    }

    public static StringBuilder getCreateTableExpression(Class<?> entityClass) {
        String columnType;
        boolean firstField = true;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS \"").append(entityClass.getSimpleName()).append("\" (");
        for (Field field : entityClass.getDeclaredFields()) {
            columnType = H2TableFactory.PROPERTIES.getProperty(field.getType().getSimpleName().toUpperCase());
            if (columnType != null) {
                if (firstField) firstField = false;
                else stringBuilder.append(", ");
                stringBuilder.append(field.getName());
                stringBuilder.append(" ").append(columnType);
            }
        }
        return stringBuilder.append(");");
    }
}
