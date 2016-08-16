package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.reflection.ReflectiveBuilder;

/**
 * com.epam.java.rt.lab.dao
 */
public class Permission<T> {
    private final Long id;
    private final String name;
    private final T value;

    public Permission(ReflectiveBuilder reflectiveBuilder) {
        this.id = reflectiveBuilder.getFieldValue("id");
        this.name = reflectiveBuilder.getFieldValue("name");
        this.value = reflectiveBuilder.getFieldValue("value");
        reflectiveBuilder.clearFieldValueMap();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
