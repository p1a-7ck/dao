package com.epam.java.rt.lab.entity;

import com.epam.java.rt.lab.entity.authentication.PermissionTest;
import com.epam.java.rt.lab.entity.authentication.RoleTest;
import com.epam.java.rt.lab.entity.authentication.UserTest;
import com.epam.java.rt.lab.entity.reflection.ReflectiveBuilderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * com.epam.java.rt.lab.dao
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({
        PermissionTest.class,
        RoleTest.class,
        UserTest.class,
        ReflectiveBuilderTest.class
})

public class ModelSuiteTest{

}
