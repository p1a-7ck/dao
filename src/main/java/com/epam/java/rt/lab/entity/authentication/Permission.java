package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.BaseEntity;
import com.epam.java.rt.lab.entity.TableColumn;

/**
 * com.epam.java.rt.lab.dao
 */
public class Permission<T> extends BaseEntity {
    @TableColumn("? VARCHAR(255)")
    private String name;
    @TableColumn("? VARCHAR(255)") // FOREIGN KEY(value) REFERENCES
    private T value;

    public Permission() {
    }

    public Long getId() { return super.getId(); }

    public void setId(Long id) { super.setId(id); }

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
