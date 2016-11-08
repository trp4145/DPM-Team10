package main;

import static org.junit.Assert.*;

import org.junit.Test;

import lejos.robotics.geometry.Rectangle;

/**
 * @author yazami
 *
 */
public class UtilsTest
{
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
    public void testToRect()
    {
        Vector2 corner1 = new Vector2(-4, 2);
        Vector2 corner2 = new Vector2(10, 10);
        Rectangle rect = Utils.toRect(corner1, corner2);
        
        assertEquals(-4f, rect.x, 0f);
        assertEquals(2f, rect.y, 0f);
        assertEquals(14f, rect.width, 0f);
        assertEquals(8f, rect.height, 0f);
    }

    /**
     * Test method for {@link main.Utils#padRect(lejos.robotics.geometry.Rectangle, float)}.
     */
    @Test
    public void testPadRect() 
    {
        Vector2 corner1 = new Vector2(5, 5);
        Vector2 corner2 = new Vector2(10, 10);
        Rectangle rect = Utils.toRect(corner1, corner2);
        
        Rectangle paddedRect = Utils.padRect(rect, 2f);

        assertEquals(rect.getCenterX(), paddedRect.getCenterX(), 0f);
        assertEquals(rect.getCenterY(), paddedRect.getCenterY(), 0f);
        assertEquals(3f, paddedRect.getMinX(), 0f);
        assertEquals(3f, paddedRect.getMinY(), 0f);
        assertEquals(12f, paddedRect.getMaxX(), 0f);
        assertEquals(12f, paddedRect.getMaxY(), 0f);
    }

    /**
     * Test method for {@link main.Utils#rectContains(main.Vector2, lejos.robotics.geometry.Rectangle)}.
     */
    @Test
    public void testRectContains() 
    {
        Vector2 corner1 = new Vector2(0, 0);
        Vector2 corner2 = new Vector2(10, 10);
        Rectangle rect = Utils.toRect(corner1, corner2);
        
        Vector2 point = new Vector2(5f, 5f);
        
        boolean containsPoint = Utils.rectContains(point, rect);
        boolean expectedContains = true;
        
        assertEquals(expectedContains, containsPoint);       
    }

    /**
     * Test method for {@link main.Utils#lineIntersectsRect(main.Vector2, main.Vector2, lejos.robotics.geometry.Rectangle)}.
     */
    @Test
    public void testLineIntersectsRect() 
    {
        float c1x = 0f;
        float c1y = 10f;
        float c2x = 10f;
        float c2y = 0f;
        Vector2 corner1 = new Vector2(c1x, c1y);
        Vector2 corner2 = new Vector2(c2x, c2y);
        Rectangle rect = Utils.toRect(corner1, corner2);
        
        float lineStartX = -1f;
        float lineStartY = 1f;
        Vector2 lineStart = new Vector2(lineStartX, lineStartY);
        
        float lineEndX = 2f;
        float lineEndY = 1f;
        Vector2 lineEnd = new Vector2(lineEndX, lineEndY);
   
        boolean lineIntersectsRect = Utils.lineIntersectsRect(lineStart, lineEnd, rect);
        boolean expectedLineIntersects = true;
        
        assertEquals(expectedLineIntersects, lineIntersectsRect);
    }
}
