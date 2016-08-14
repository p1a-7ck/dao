package model.authentication;

import model.reflection.ReflectiveBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * dao
 */
public class UserTest {
    User user;

    @Before
    public void setUp() throws Exception {
        List<Role> roleList = new ArrayList<>();
        ReflectiveBuilder reflectiveBuilder = new ReflectiveBuilder();
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("name", "Some Role One");
        reflectiveBuilder.setFieldValueList("permissionList", null);
        roleList.add(new Role(reflectiveBuilder));
        reflectiveBuilder.setFieldValue("id", 2L);
        reflectiveBuilder.setFieldValue("name", "Some Role Two");
        reflectiveBuilder.setFieldValueList("permissionList", null);
        roleList.add(new Role(reflectiveBuilder));
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("login", "test_login");
        reflectiveBuilder.setFieldValue("pass", "test_pass");
        reflectiveBuilder.setFieldValueList("roleList", roleList);
        user = new User(reflectiveBuilder);
        reflectiveBuilder = null;
        roleList = null;
    }

    @After
    public void tearDown() throws Exception {
        user = null;
    }

    @Test
    public void getId() throws Exception {
        assertEquals("Id field set or get error", (Long) 1L, user.getId());
    }

    @Test
    public void getLogin() throws Exception {
        assertEquals("Login field set or get error", "test_login", user.getLogin());
    }

    @Test
    public void getPass() throws Exception {
        assertEquals("Pass field set or get error", "test_pass", user.getPass());
    }

    @Test
    public void getRole() throws Exception {
        Role role, roleActual;
        ReflectiveBuilder reflectiveBuilder = new ReflectiveBuilder();
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("name", "Some Role One");
        reflectiveBuilder.setFieldValueList("permissionList", null);
        role = new Role(reflectiveBuilder);
        roleActual = user.getRole(0);
        assertEquals("Role id field set or get error", (Long) 1L, roleActual.getId());
        assertEquals("Role name field set or get error", "Some Role One", role.getName());
        assertEquals("Role permission field set or get error", 0, role.countPermissions());
        reflectiveBuilder.setFieldValue("id", 2L);
        reflectiveBuilder.setFieldValue("name", "Some Role Two");
        reflectiveBuilder.setFieldValueList("permissionList", null);
        role = new Role(reflectiveBuilder);
        roleActual = user.getRole(1);
        assertEquals("Role id field set or get error", (Long) 2L, roleActual.getId());
        assertEquals("Role name field set or get error", "Some Role Two", role.getName());
        assertEquals("Role permission field set or get error", 0, role.countPermissions());
    }

    @Test
    public void countRoles() throws Exception {
        assertEquals("Roles list field set or get error", 2, user.countRoles());
    }
}