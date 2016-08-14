package model.authentication;

import model.reflection.ReflectiveBuilder;

import java.util.List;

/**
 * dao
 */
public class Role {
    private final Long id;
    private final String name;
    private final List<Permission> permissionList;

    public Role(ReflectiveBuilder reflectiveBuilder) {
        this.id = reflectiveBuilder.getFieldValue("id");
        this.name = reflectiveBuilder.getFieldValue("name");
        this.permissionList = reflectiveBuilder.getFieldValueList("permissionList");
        reflectiveBuilder.clearFieldValueMap();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Permission getPermission(int index) {
        if (index < 0 || index >= this.permissionList.size())
            throw new IllegalArgumentException("Permission index out of bound");
        return this.permissionList.get(index);
    }

    public int countPermissions() {
        return (this.permissionList == null) ? 0 : this.permissionList.size();
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", permissionList=" + permissionList +
                '}';
    }
}
