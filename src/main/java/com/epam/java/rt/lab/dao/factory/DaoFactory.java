package com.epam.java.rt.lab.dao.factory;

import com.epam.java.rt.lab.dao.ReflectiveJdbcDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

/**
 * com.epam.java.rt.lab.dao
 */
public abstract class DaoFactory {
    private static final Logger logger = LoggerFactory.getLogger(DaoFactory.class);
    private static Properties PROPERTIES;

    static {
        if (DaoFactory.PROPERTIES == null) {
            try {
                DaoFactory.PROPERTIES = new Properties();
                DaoFactory.PROPERTIES
                        .load(DaoFactory.class.getClassLoader().getResourceAsStream("dao.properties"));
            } catch (IOException exc) {
                logger.error("File 'connection.properties' IO error", exc);
            }
        }
    }

    public static DaoFactory createDaoFactory() {
        try {
            Class<DaoFactory> factoryClass =
                    (Class<DaoFactory>) Class.forName(DaoFactory.PROPERTIES.getProperty("dao.factory.class"));
            return factoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exc) {
            exc.printStackTrace();
        }
        return null;
    }

    public abstract ReflectiveJdbcDao getReflectiveJdbcDao();

}
