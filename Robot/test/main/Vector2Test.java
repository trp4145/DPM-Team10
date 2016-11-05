/**
 * 
 */
package main;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author yazami
 *
 */
public class Vector2Test {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }
    
    
    /**
     * Tests construction of vector and retrieval of values.
     */
    @Test
    public void testConstruct()
    {
        float x = -204.0f;
        float y = 93.057f;
        Vector2 vec = new Vector2(x, y);
        
        assertEquals(x, vec.getX(), 0f);
        assertEquals(y, vec.getY(), 0f);
    }
    
    /**
     * Tests null clone constructor.
     */
    @Test
    public void testCloneConstruct()
    {
        float x = -54.025f;
        float y = 23.057f;
        Vector2 vec1 = new Vector2(x, y);
        Vector2 vec2 = new Vector2(vec1);
        
        assertEquals(x, vec2.getX(), 0f);
        assertEquals(y, vec2.getY(), 0f);
    }
    
    /**
     * Tests null clone constructor.
     */
    @Test
    public void testNullConstruct()
    {
        String message = "";
        try
        {            
            Vector2 vec = new Vector2(null);
        }
        catch (InvalidParameterException e)
        {
            message = e.getMessage();
        }
        assertEquals("Attempted to clone a null vector!", message);
    }
    

    /**
     * Test method for {@link main.Vector2#zero()}.
     */
    @Test
    public void testZero() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#one()}.
     */
    @Test
    public void testOne() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#unitX()}.
     */
    @Test
    public void testUnitX() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#unitY()}.
     */
    @Test
    public void testUnitY() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#Vector2(float, float)}.
     */
    @Test
    public void testVector2FloatFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#Vector2(main.Vector2)}.
     */
    @Test
    public void testVector2Vector2() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#add(main.Vector2)}.
     */
    @Test
    public void testAddVector2() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#subtract(main.Vector2)}.
     */
    @Test
    public void testSubtractVector2() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#scale(float)}.
     */
    @Test
    public void testScaleFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#rotate(float)}.
     */
    @Test
    public void testRotateFloat() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#magnitude()}.
     */
    @Test
    public void testMagnitude() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#angle()}.
     */
    @Test
    public void testAngle() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#toString()}.
     */
    @Test
    public void testToString() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#add(main.Vector2, main.Vector2)}.
     */
    @Test
    public void testAddVector2Vector2() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#subtract(main.Vector2, main.Vector2)}.
     */
    @Test
    public void testSubtractVector2Vector2() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#scale(main.Vector2, float)}.
     */
    @Test
    public void testScaleVector2Float() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#rotate(main.Vector2, float)}.
     */
    @Test
    public void testRotateVector2Float() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#distance(main.Vector2, main.Vector2)}.
     */
    @Test
    public void testDistance() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#fromPolar(float, float)}.
     */
    @Test
    public void testFromPolar() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#getX()}.
     */
    @Test
    public void testGetX() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#getY()}.
     */
    @Test
    public void testGetY() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#setX(float)}.
     */
    @Test
    public void testSetX() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Vector2#setY(float)}.
     */
    @Test
    public void testSetY() {
        fail("Not yet implemented");
    }

}
