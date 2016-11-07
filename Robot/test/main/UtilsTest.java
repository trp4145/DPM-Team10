/**
 * 
 */
package main;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author yazami
 *
 */
public class UtilsTest {

    /**
     * Test method for {@link main.Utils#clamp(float, float, float)}.
     */
    @Test
    public void testClamp() 
    {
        float clamped = Utils.clamp(10, 3, 5);
        
        float expectedClampedValue = 5;
        
        assertEquals(expectedClampedValue, clamped, 0f);
    }

    /**
     * Test method for {@link main.Utils#toBearing(float)}.
     */
    @Test
    public void testToBearing() 
    {
        float angle = 270;
        
        float correctedAngle = Utils.toBearing(angle);
        
        float expectedAngle = -90;
        
        assertEquals(expectedAngle, correctedAngle, 0f);
    }

    /**
     * Test method for {@link main.Utils#toRect(main.Vector2, main.Vector2)}.
     */
    @Test
    public void testToRect() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Utils#padRect(lejos.robotics.geometry.Rectangle, float)}.
     */
    @Test
    public void testPadRect() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Utils#rectContains(main.Vector2, lejos.robotics.geometry.Rectangle)}.
     */
    @Test
    public void testRectContains() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Utils#lineIntersectsRect(main.Vector2, main.Vector2, lejos.robotics.geometry.Rectangle)}.
     */
    @Test
    public void testLineIntersectsRect() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Utils#sleep(long)}.
     */
    @Test
    public void testSleep() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link main.Utils#sleepToNextPeroid(int, long)}.
     */
    @Test
    public void testSleepToNextPeroid() {
        fail("Not yet implemented");
    }

}
