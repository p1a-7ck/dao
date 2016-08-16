package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.reflection.ReflectiveBuilder;

import java.util.List;

/**
 * com.epam.java.rt.lab.dao
 */
public class User {
    private final Long id;
    private final String login;
    private final String pass;
    private final List<Role> roleList;

    public User(ReflectiveBuilder reflectiveBuilder) {
        this.id = reflectiveBuilder.getFieldValue("id");
        this.login = reflectiveBuilder.getFieldValue("login");
        this.pass = reflectiveBuilder.getFieldValue("pass");
        this.roleList = reflectiveBuilder.getFieldValueList("roleList");
        reflectiveBuilder.clearFieldValueMap();
    }

    public Long getId() {
        return this.id;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPass() {
        return this.pass;
    }

    public Role getRole(int index) {
        if (index < 0 || index >= this.roleList.size())
            throw new IllegalArgumentException("Role index out of bound");
        return this.roleList.get(index);
    }

    public int countRoles() {
        return (this.roleList == null) ? 0 : this.roleList.size();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", pass='" + pass + '\'' +
                ", roleList=" + roleList +
                '}';
    }
}
