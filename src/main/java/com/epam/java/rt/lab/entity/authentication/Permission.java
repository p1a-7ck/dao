package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.BaseEntity;

/**
 * com.epam.java.rt.lab.dao
 */
public class Permission<T> extends BaseEntity {
    private String name;
    private T value;

    public Permission() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + super.getId() +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
