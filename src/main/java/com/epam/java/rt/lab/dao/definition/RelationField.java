package com.epam.java.rt.lab.dao.definition;

import com.epam.java.rt.lab.entity.BaseEntity;

/**
 * dao
 */
public class RelationField<T> {
    private T value;

    public RelationField() {
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
