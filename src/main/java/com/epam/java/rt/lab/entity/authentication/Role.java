package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.BaseEntity;
import com.epam.java.rt.lab.entity.RelationTable;
import com.epam.java.rt.lab.entity.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * com.epam.java.rt.lab.dao
 */
public class Role extends BaseEntity {
    @TableColumn("? VARCHAR(255)")
    private String name;
    //@TableColumn("FOREIGN KEY(id) REFERENCES \"Permission\"(id)")
    @RelationTable("\"RolePermission\" " +
            "id IDENTITY PRIMARY KEY, " +
            "roleId BIGINT REFERENCES \"Role\", " +
            "permissionId BIGINT REFERENCES \"Permission\"")
    private List<Permission> permissionList;

    public Role() {
        this.permissionList = new ArrayList<>();
    }

    public Long getId() { return super.getId(); }

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
