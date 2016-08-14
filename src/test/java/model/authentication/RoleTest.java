package model.authentication;

import model.reflection.ReflectiveBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * dao
 */
public class RoleTest {
    Role role;

    @Before
    public void setUp() throws Exception {
        List<Permission<String>> permissionList = new ArrayList<>();
        ReflectiveBuilder reflectiveBuilder = new ReflectiveBuilder();
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("name", "Some Permission One");
        reflectiveBuilder.setFieldValue("value", "Some Value One");
        permissionList.add(new Permission<>(reflectiveBuilder));
        reflectiveBuilder.setFieldValue("id", 2L);
        reflectiveBuilder.setFieldValue("name", "Some Permission Two");
        reflectiveBuilder.setFieldValue("value", "Some Value Two");
        permissionList.add(new Permission<>(reflectiveBuilder));
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("name", "Some Role");
        reflectiveBuilder.setFieldValueList("permissionList", permissionList);
        role = new Role(reflectiveBuilder);
        reflectiveBuilder = null;
        permissionList = null;
    }

    @After
    public void tearDown() throws Exception {
        role = null;
    }

    @Test
    public void getId() throws Exception {
        assertEquals("Id field set or get error", (Long) 1L, role.getId());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("Id field set or get error", "Some Role", role.getName());
    }

    @Test
    public void getPermission() throws Exception {
        Permission<String> permission, permissionActual;
        ReflectiveBuilder reflectiveBuilder = new ReflectiveBuilder();
        reflectiveBuilder.setFieldValue("id", 1L);
        reflectiveBuilder.setFieldValue("name", "Some Permission One");
        reflectiveBuilder.setFieldValue("value", "Some Value One");
        permission = new Permission<>(reflectiveBuilder);
        permissionActual = role.getPermission(0);
        assertEquals("Permission id field set or get error", permission.getId(), permissionActual.getId());
        assertEquals("Permission name field set or get error", permission.getName(), permissionActual.getName());
        assertEquals("Permission value field set or get error", permission.getValue(), permissionActual.getValue());
        reflectiveBuilder.setFieldValue("id", 2L);
        reflectiveBuilder.setFieldValue("name", "Some Permission Two");
        reflectiveBuilder.setFieldValue("value", "Some Value Two");
        permission = new Permission<>(reflectiveBuilder);
        permissionActual = role.getPermission(1);
        assertEquals("Permission id field set or get error", permission.getId(), permissionActual.getId());
        assertEquals("Permission name field set or get error", permission.getName(), permissionActual.getName());
        assertEquals("Permission value field set or get error", permission.getValue(), permissionActual.getValue());
    }

    @Test
    public void countPermissions() throws Exception {
        assertEquals("Permission list field set or get error", 2, role.countPermissions());
    }

}