package com.epam.java.rt.lab.dao.definition;

import com.epam.java.rt.lab.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * dao
 */
public class RelationEntity extends BaseEntity {
    private String name;
    private List<RelationField<?>> relationFieldList;

    public RelationEntity() {
        this.relationFieldList = new ArrayList<>();
    }

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RelationField<?> getRelationField(int index) {
        return relationFieldList.get(index);
    }

    public void addRelationField(RelationField<?> relationField) {
        this.relationFieldList.add(relationField);
    }

    public void setRelationField(int index, RelationField<?> relationField) {
        this.relationFieldList.set(index, relationField);
    }

}
