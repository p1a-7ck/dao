package com.epam.java.rt.lab.entity;

import com.epam.java.rt.lab.dao.definition.Column;

/**
 * dao
 */
public abstract class BaseEntity {
    @Column(columnName = "ID", columnValueType = "IDENTITY PRIMARY KEY")
    private Long id;

    public BaseEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (this.id != null) throw new IllegalArgumentException("Field 'id' not null");
        this.id = id;
    }

}
