package model;

import model.authentication.PermissionTest;
import model.authentication.RoleTest;
import model.authentication.UserTest;
import model.reflection.ReflectiveBuilderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * dao
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
