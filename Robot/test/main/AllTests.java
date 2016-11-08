package main;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    ColorTest.class, 
    UtilsTest.class, 
    Vector2Test.class 
    })
public class AllTests {

}