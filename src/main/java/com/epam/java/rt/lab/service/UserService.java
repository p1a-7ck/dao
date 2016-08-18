package com.epam.java.rt.lab.service;

import com.epam.java.rt.lab.dao.factory.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

/**
 * dao
 */
public class UserService implements Runnable {

    @Override
    public void run() {
        try {
            Connection connection = JdbcConnectionFactory.getInstance().getConnection();
            for (int i = 0; i < (new Random()).nextInt(); i++) ;
            JdbcConnectionFactory.getInstance().releaseConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
