package com.epam.java.rt.lab.dao.factory;

import com.epam.java.rt.lab.dao.ReflectiveJdbcDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * com.epam.java.rt.lab.dao.factory
 */
public class H2DaoFactory extends DaoFactory {
    private static final Logger logger = LoggerFactory.getLogger(H2DaoFactory.class);

    public H2DaoFactory() {
    }

    @Override
    public ReflectiveJdbcDao getReflectiveJdbcDao() {
        try {
            return new ReflectiveJdbcDao();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}