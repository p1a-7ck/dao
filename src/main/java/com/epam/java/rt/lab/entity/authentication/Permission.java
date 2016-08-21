package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.dao.definition.Table;
import com.epam.java.rt.lab.entity.BaseEntity;
import com.epam.java.rt.lab.dao.definition.Column;

/**
 * com.epam.java.rt.lab.dao
 */
@Table(tableName = "PERMISSION")
public class Permission<T> extends BaseEntity {

    @Column(columnName = "NAME", columnValueType = "VARCHAR(255)")
    private String name;

    @Column(columnName = "VALUE", columnValueType = "VARCHAR(255)") // FOREIGN KEY(value) REFERENCES
    private T value;

    public Permission() {
    }

    @Override
    public Long getId() { return super.getId(); }

    @Override
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
