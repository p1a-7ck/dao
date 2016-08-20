package com.epam.java.rt.lab.entity.some;

import com.epam.java.rt.lab.entity.BaseEntity;
import com.epam.java.rt.lab.entity.TableColumn;

/**
 * dao
 */
public class SomeEntity extends BaseEntity {
    @TableColumn("? VARCHAR(255)")
    private String name;

    public Long getId() { return super.getId(); }

    public void setId(Long id) { super.setId(id); }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
