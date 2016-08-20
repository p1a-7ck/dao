package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.connection.ConnectionPool;
import com.epam.java.rt.lab.dao.factory.DaoFactory;
import com.epam.java.rt.lab.entity.authentication.Permission;
import com.epam.java.rt.lab.entity.authentication.Role;
import com.epam.java.rt.lab.entity.authentication.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.assertTrue;

/**
 * dao
 */
public class DaoTest {
    DaoFactory factory;
    Dao dao;


    @Before
    public void setUp() throws Exception {
        factory = DaoFactory.createDaoFactory();
        dao = factory.getReflectiveJdbcDao();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void createTest() throws Exception {
        Connection connection = ConnectionPool.getInstance().getConnection();
        String sqlExpression = dao.createTableExpression(Permission.class);
        dao.execute(connection, sqlExpression);
        assertTrue("Table create error", dao.execute(connection, "SELECT * FROM \"Permission\""));
        sqlExpression = dao.createTableExpression(Role.class);
        dao.execute(connection, sqlExpression);
        assertTrue("Table create error", dao.execute(connection, "SELECT * FROM \"Role\""));
        sqlExpression = dao.createTableExpression(User.class);
        dao.execute(connection, sqlExpression);
        assertTrue("Table create error", dao.execute(connection, "SELECT * FROM \"User\""));
    }

    @Test
    public void insertTest() throws Exception {
        Connection connection = ConnectionPool.getInstance().getConnection();
        User user = new User();
        user.setId(1L);
        user.setLogin("login one");
        user.setPass("pass one");
        dao.insert(connection, user);
        user = new User();
        user.setId(2L);
        user.setLogin("login two");
        user.setPass("pass two");
        dao.insert(connection, user);
        user = new User();
        user.setId(3L);
        user.setLogin("login three");
        user.setPass("pass three");
        dao.insert(connection, user);
    }

}