package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.BaseEntity;

import java.util.List;

/**
 * com.epam.java.rt.lab.dao
 */
public class Role extends BaseEntity {
    private String name;
    private List<Permission> permissionList;

    public Role() {
    }

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
