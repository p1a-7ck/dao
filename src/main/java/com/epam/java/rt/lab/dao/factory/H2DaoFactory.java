package com.epam.java.rt.lab.dao.factory;

import com.epam.java.rt.lab.dao.ReflectiveJdbcDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * com.epam.java.rt.lab.dao.factory
 */
public class H2DaoFactory extends DaoFactory {
    private static final Logger logger = LoggerFactory.getLogger(H2DaoFactory.class);
    private static Properties PROPERTIES;

    static {
        if (H2DaoFactory.PROPERTIES == null) {
            try {
                H2DaoFactory.PROPERTIES = new Properties();
                H2DaoFactory.PROPERTIES
                        .load(H2DaoFactory.class.getClassLoader().getResourceAsStream("h2dao.properties"));
            } catch (IOException exc) {
                logger.error("File 'h2dao.properties' IO error", exc);
            }
        }
    }

    public H2DaoFactory() {
    }

    public static Connection createConnection() {
        try {
            logger.info("Creating connection");
            return DriverManager.getConnection(H2DaoFactory.PROPERTIES.getProperty("db.URL"),
                    H2DaoFactory.PROPERTIES.getProperty("db.User"), H2DaoFactory.PROPERTIES.getProperty("db.Password"));
        } catch (SQLException exc) {
            logger.error("Connection create error", exc);
        }
        return null;
    }

    @Override
    public ReflectiveJdbcDao getReflectiveJdbcDao() {
        return new ReflectiveJdbcDao(H2DaoFactory.class);
    }

}
