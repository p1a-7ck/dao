package com.epam.java.rt.lab.entity.authentication;

import com.epam.java.rt.lab.entity.BaseEntity;

import java.util.List;

/**
 * com.epam.java.rt.lab.dao
 */
public class User extends BaseEntity {
    private String login;
    private String pass;
    private List<Role> roleList;

    public User() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Role getRole(int index) {
        if (index < 0 || index >= this.roleList.size())
            throw new IllegalArgumentException("Role index out of bound");
        return this.roleList.get(index);
    }

    public boolean addRole(Role role) {
        return this.roleList.add(role);
    }

    public Role removeRole(int index) {
        if (index < 0 || index >= this.roleList.size())
            throw new IllegalArgumentException("Role index out of bound");
        return this.roleList.remove(index);
    }

    public int countRoles() {
        return (this.roleList == null) ? 0 : this.roleList.size();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + super.getId() + '\'' +
                ", login='" + login + '\'' +
                ", pass='" + pass + '\'' +
                ", roleList=" + roleList +
                '}';
    }
}
