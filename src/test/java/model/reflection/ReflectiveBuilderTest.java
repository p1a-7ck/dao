package model.reflection;

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
public class ReflectiveBuilderTest {
    ReflectiveBuilder reflectiveBuilder;

    @Before
    public void setUp() throws Exception {
        reflectiveBuilder = new ReflectiveBuilder();
    }

    @After
    public void tearDown() throws Exception {
        reflectiveBuilder.clearFieldValueMap();
        reflectiveBuilder = null;
    }

    @Test
    public void setFieldValue() throws Exception {
        reflectiveBuilder.setFieldValue("id", 100L);
        reflectiveBuilder.setFieldValue("name", "Some Name");
        reflectiveBuilder.setFieldValue("active", true);
        assertEquals("Numeric value set or get error", 100L, (long) reflectiveBuilder.getFieldValue("id"));
        assertEquals("String value set or get error", "Some Name", reflectiveBuilder.getFieldValue("name"));
        assertEquals("Boolean value set or get error", true, reflectiveBuilder.getFieldValue("active"));
    }

    @Test
    public void setFieldValueList() throws Exception {
        List<String> stringList = new ArrayList<>();
        stringList.add("String One");
        stringList.add("String Two");
        stringList.add("String Three");
        reflectiveBuilder.setFieldValueList("stringList", stringList);
        assertEquals("String list value set or get error", stringList, reflectiveBuilder.getFieldValueList("stringList"));
    }

    @Test
    public void addFieldValue() throws Exception {
        List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        reflectiveBuilder.addFieldValue("intList", 1);
        reflectiveBuilder.addFieldValue("intList", 2);
        reflectiveBuilder.addFieldValue("intList", 3);
        assertEquals("Integer list value add or get error", intList, reflectiveBuilder.getFieldValueList("intList"));
    }

    @Ignore (".getFieldValue()-method examined in test of .setFieldValue()-method")
    @Test
    public void getFieldValue() throws Exception {

    }

    @Ignore (".getFieldValueList()-method examined in test of .setFieldValueList()-method")
    @Test
    public void getFieldValueList() throws Exception {

    }

}