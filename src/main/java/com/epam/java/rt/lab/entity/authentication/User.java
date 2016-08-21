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
@Table(tableName = "USER")
public class User extends BaseEntity {

    @Column(columnName = "LOGIN", columnValueType = "VARCHAR(255)")
    private String login;

    @Column(columnName = "PASS", columnValueType = "VARCHAR(255)")
    private String pass;

    @ColumnRelationMany(tableName = "USERROLE",
            tableColumnNames = {"ID", "USERID", "ROLEID"},
            tableColumnValueTypes = {"IDENTITY PRIMARY KEY", "BIGINT", "BIGINT"},
            tableColumnReferencesTableName = {"", "USER", "ROLE"},
            tableColumnReferencesTableColumnName = {"", "ID", "ID"})
    private List<Role> roleList;

    public User() {
        this.roleList = new ArrayList<>();
    }

    @Override
    public Long getId() { return super.getId(); }

    @Override
    public void setId(Long id) { super.setId(id); }

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
