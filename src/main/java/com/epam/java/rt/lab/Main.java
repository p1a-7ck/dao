package com.epam.java.rt.lab;

import com.epam.java.rt.lab.dao.Dao;
import com.epam.java.rt.lab.dao.factory.DaoFactory;
import com.epam.java.rt.lab.dao.factory.H2TableFactory;
import com.epam.java.rt.lab.entity.authentication.User;
import com.epam.java.rt.lab.entity.reflection.ReflectiveBuilder;

/**
 * com.epam.java.rt.lab.dao
 */
public class Main {
    public static void main(String[] args) {
        DaoFactory factory = DaoFactory.createDaoFactory();
        Dao reflectiveDao = factory.getReflectiveJdbcDao();

        reflectiveDao.createTable(User.class);

        ReflectiveBuilder reflectiveBuilder = new ReflectiveBuilder();
        reflectiveBuilder.setFieldValue("id", 5L);
        reflectiveBuilder.setFieldValue("login", "new");
        reflectiveBuilder.setFieldValue("pass", "new");
        reflectiveBuilder.setFieldValue("roleList", null);
        reflectiveDao.insert(new User(reflectiveBuilder));

        reflectiveDao = null;
        factory = null;

/*
        if (factory != null) {
            try {
                Connection connection = factory.createConnection();
                Statement statement = connection.createStatement();
                statement.execute("CREATE TABLE USER (ID INT PRIMARY KEY, LOGIN VARCHAR(255), PASS VARCHAR(255))");
                statement.execute("INSERT INTO USER VALUES (1, 'admin', 'admin')");
                statement.execute("INSERT INTO USER VALUES (2, 'user', 'user')");
                statement.execute("INSERT INTO USER VALUES (3, 'test', 'test')");
                statement.execute("SELECT * FROM USER");
                while (statement.getResultSet().next()) {
                    System.out.println(statement.getResultSet().getInt(1));
                    System.out.println(statement.getResultSet().getString(2));
                    System.out.println(statement.getResultSet().getString(2));
                }
                statement.close();
                connection.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
        }
*/

    }
}
