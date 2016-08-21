package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.dao.definition.Column;
import com.epam.java.rt.lab.dao.definition.ColumnRelationMany;
import com.epam.java.rt.lab.dao.definition.Table;
import com.epam.java.rt.lab.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * com.epam.java.rt.lab.dao
 */
@Table(tableName = "ROLE")
public class Role extends BaseEntity {

    @Column(columnName = "NAME", columnValueType = "VARCHAR(255)")
    private String name;

    @ColumnRelationMany(tableName = "ROLEPERMISSION",
            tableColumnNames = {"ID", "ROLEID", "PERMISSOINID"},
            tableColumnValueTypes = {"IDENTITY PRIMARY KEY", "BIGINT", "BIGINT"},
            tableColumnReferencesTableName = {"", "ROLE", "PERMISSION"},
            tableColumnReferencesTableColumnName = {"", "ID", "ID"})
    private List<Permission> permissionList;

    public Role() {
        this.permissionList = new ArrayList<>();
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

    public Permission getPermission(int index) {
        if (index < 0 || index >= this.permissionList.size())
            throw new IllegalArgumentException("Permission index out of bound");
        return this.permissionList.get(index);
    }

    public boolean addPermission(Permission permission) {
        return this.permissionList.add(permission);
    }

    public Permission removePermission(int index) {
        if (index < 0 || index >= this.permissionList.size())
            throw new IllegalArgumentException("Permission index out of bound");
        return this.permissionList.remove(index);
    }

    public int countPermissions() {
        return (this.permissionList == null) ? 0 : this.permissionList.size();
    }

    @Override
    public String toString() {
        return "Role{" +
                "id='" + super.getId() + '\'' +
                ", name='" + name + '\'' +
                ", permissionList=" + permissionList +
                '}';
    }
}
