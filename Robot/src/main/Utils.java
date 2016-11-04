package main;

import lejos.robotics.geometry.Rectangle;

/**
 * @author Scott Sewell
 * Contains useful utility methods.
 */
public class Utils
{
    /**
     * Clamps a value within a range.
     * @param value the value to limit.
     * @param min the minimum value.
     * @param max the maximum value.
     * @return the clamped value.
     */
    public static float clamp(float value, float min, float max)
    {
        return Math.max(Math.min(value, max), min);
    }
    
    /**
     * Transforms a [0, 360] angle to a [-180, 180] angle.
     * @param angle in degrees.
     * @return the transformed angle.
     */
    public static float toBearing(float angle)
    {
        while (angle < 0)
        {
            angle += 360;
        }
        return (((angle + 540) % 360) - 180);
    }
    
    /**
     * Checks if a point is contained within a rectangle defined by two opposite corners.
     * @param point to check if contained.
     * @param corner1 a corner of the rectangle.
     * @param corner2 the opposite corner of the rectangle.
     * @return true if point is contained.
     */
    public static boolean rectContains(Vector2 point, Vector2 corner1, Vector2 corner2)
    {
        return  Math.min(corner1.getX(), corner2.getX()) < point.getX() && 
                Math.max(corner1.getX(), corner2.getX()) > point.getX() &&
                Math.min(corner1.getY(), corner2.getY()) < point.getY() &&
                Math.max(corner1.getY(), corner2.getY()) > point.getY();
    }
    
    /**
     * Checks if a line intersects a rectangle defined by two opposite corners.
     * @param lineStart the start of the line.
     * @param lineEnd the end of the line.
     * @param corner1 a corner of the rectangle.
     * @param corner2 the opposite corner of the rectangle.
     * @return true if the given line is intersecting.
     */
    public static boolean lineIntersectsRect(Vector2 lineStart, Vector2 lineEnd, Vector2 corner1, Vector2 corner2)
    {
        // create a rectangle that can have line intersection tests run against it
        Rectangle rect = new Rectangle(
                Math.min(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.abs(corner1.getX() - corner2.getX()),
                Math.abs(corner1.getY() - corner2.getY())
                );
        
        return rect.intersectsLine(lineStart.getX(), lineStart.getY(), lineEnd.getX(), lineEnd.getY());
    }
}
