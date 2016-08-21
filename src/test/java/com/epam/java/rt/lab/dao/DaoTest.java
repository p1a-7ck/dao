package com.epam.java.rt.lab.dao;

import com.epam.java.rt.lab.dao.factory.DaoFactory;
import com.epam.java.rt.lab.entity.authentication.*;
import com.epam.java.rt.lab.entity.some.SomeAnotherEntity;
import com.epam.java.rt.lab.entity.some.SomeEntity;
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
        Connection connection = factory.getConnectionPool().getConnection();
        String sqlExpression = dao.generateCreateTableExpression(Permission.class);
        dao.execute(connection, sqlExpression);
        assertTrue("Table create error", dao.execute(connection, "SELECT * FROM \"PERMISSION\""));
        sqlExpression = dao.generateCreateTableExpression(Role.class);
        dao.execute(connection, sqlExpression);
        assertTrue("Table create error", dao.execute(connection, "SELECT * FROM \"ROLE\""));
        sqlExpression = dao.generateCreateTableExpression(User.class);
        dao.execute(connection, sqlExpression);
        assertTrue("Table create error", dao.execute(connection, "SELECT * FROM \"USER\""));
        connection.close();
    }

    @Test
    public void insertTest() throws Exception {
        Connection connection = factory.getConnectionPool().getConnection();
        User user = new User();
        user.setLogin("login");
        user.setPass("pass");
        Role role = new Role();
        role.setName("role");
        user.addRole(role);

        dao.insert(connection, role);
        dao.insert(connection, user);

        connection.close();
    }

    @Test
    public void createAndInsertSomeTest() throws Exception {
//        Connection connection = factory.getConnectionPool().getConnection();
//        String sqlExpression = dao.generateCreateTableExpression(SomeEntity.class);
//        dao.execute(connection, sqlExpression);
//
//        sqlExpression = dao.generateCreateTableExpression(SomeAnotherEntity.class);
//        dao.execute(connection, sqlExpression);
//
//        SomeEntity someEntity = new SomeEntity();
//        someEntity.setName("someEntity2");
//        dao.insert(connection, someEntity);
//
//        SomeAnotherEntity someAnotherEntity = new SomeAnotherEntity();
//        someAnotherEntity.setName("someAnotherEntity2");
//        someAnotherEntity.setSomeEntity(someEntity);
//        dao.insert(connection, someAnotherEntity);

    }

}