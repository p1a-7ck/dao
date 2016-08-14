package model.authentication;

import model.reflection.ReflectiveBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * dao
 */
public class PermissionTest {
    Permission<String> permission;

    @Before
    public void setUp() throws Exception {
        ReflectiveBuilder reflectiveBuilder = new ReflectiveBuilder();
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("name", "Some Permission");
        reflectiveBuilder.setFieldValue("value", "Some Value");
        permission = new Permission<>(reflectiveBuilder);
        reflectiveBuilder = null;
    }

    @After
    public void tearDown() throws Exception {
        permission = null;
    }

    @Test
    public void getId() throws Exception {
        assertEquals("Id field set or get error", (Long) 1L, permission.getId());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("Name field set or get error", "Some Permission", permission.getName());
    }

    @Test
    public void getValue() throws Exception {
        assertEquals("Value field set or get error", "Some Value", permission.getValue());
    }

}