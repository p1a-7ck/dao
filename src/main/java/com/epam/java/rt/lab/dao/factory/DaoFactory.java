package com.epam.java.rt.lab.dao.factory;

import com.epam.java.rt.lab.connection.ConnectionPool;
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
    private static ConnectionPool connectionPool;

    public static DaoFactory createDaoFactory() throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, IOException {
        try {
            if (connectionPool == null) DaoFactory.connectionPool = ConnectionPool.getInstance();
            if (DaoFactory.PROPERTIES == null) {
                DaoFactory.PROPERTIES = new Properties();
                DaoFactory.PROPERTIES
                        .load(DaoFactory.class.getClassLoader().getResourceAsStream("dao.properties"));
            }
            Class<DaoFactory> factoryClass =
                    (Class<DaoFactory>) Class.forName(DaoFactory.PROPERTIES.getProperty("dao.factory.class"));
            return factoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            logger.error("Dao factory class instantioation error", e);
            throw e;
        } catch (IOException e) {
            logger.error("File 'connection.properties' IO error", e);
            throw e;
        }
    }

    public ConnectionPool getConnectionPool() {
        return DaoFactory.connectionPool;
    }

    public abstract ReflectiveJdbcDao getReflectiveJdbcDao() throws IOException, NoSuchMethodException, ClassNotFoundException;

}
