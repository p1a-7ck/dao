package com.epam.java.rt.lab.entity;

/**
 * dao
 */
public abstract class BaseEntity {
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
