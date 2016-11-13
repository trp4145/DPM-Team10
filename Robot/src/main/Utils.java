package main;

import lejos.robotics.geometry.*;

/**
 * Contains useful utility methods frequently used throughout the code.
 * 
 * @author Scott Sewell
 */
public class Utils
{
    /**
     * Clamps a value within a range.
     * 
     * @param value
     *            the value to limit.
     * @param min
     *            the minimum value.
     * @param max
     *            the maximum value.
     * @return the clamped value.
     */
    public static float clamp(float value, float min, float max)
    {
        return Math.max(Math.min(value, max), min);
    }

    /**
     * Transforms a [0, 360] angle to a [-180, 180] angle.
     * 
     * @param angle
     *            an angle in degrees.
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
     * Creates a rectangle defined by two opposite corners.
     * 
     * @param corner1
     *            a corner of the rectangle.
     * @param corner2
     *            the opposite corner of the rectangle.
     * @return a new rectangle.
     */
    public static Rectangle toRect(Vector2 corner1, Vector2 corner2)
    {
        return new Rectangle(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.abs(corner1.getX() - corner2.getX()),
                Math.abs(corner1.getY() - corner2.getY())
                );
    }
    
    /**
     * Expands all sides of a rectangle outwards by a certain amount.
     * 
     * @param rect
     *            the rectangle to add padding to.
     * @param padding
     *            the amount to push each edge outwards. Can be negative to
     *            shrink rectangle.
     * @return a new padded rectangle.
     */
    public static Rectangle padRect(Rectangle rect, float padding)
    {
        return new Rectangle(
                rect.x - padding,
                rect.y - padding,
                rect.width + (padding * 2),
                rect.height + (padding * 2)
                );
    }

    /**
     * Checks if a point is contained within a rectangle.
     * 
     * @param point
     *            the point to check if contained.
     * @param rect
     *            the rectangle to check against.
     * @return true if point is contained.
     */
    public static boolean rectContains(Vector2 point, Rectangle rect)
    {
        return rect.contains(new Point(point.getX(), point.getY()));
    }

    /**
     * Checks if a line intersects a rectangle defined by two opposite corners.
     * 
     * @param lineStart
     *            the start of the line.
     * @param lineEnd
     *            the end of the line.
     * @param rect
     *            the rectangle to check against.
     * @return true if the given line is intersecting.
     */
    public static boolean lineIntersectsRect(Vector2 lineStart, Vector2 lineEnd, Rectangle rect)
    {
        return rect.intersectsLine(lineStart.getX(), lineStart.getY(), lineEnd.getX(), lineEnd.getY());
    }

    /**
     * Halts the calling thread for short time.
     * 
     * @param milliseconds
     *            how long to wait until continuing.
     */
    public static void sleep(long milliseconds)
    {
        if (milliseconds > 0)
        {
            try
            {
                Thread.sleep(milliseconds);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Halts the thread until the next period begins.
     * 
     * @param period
     *            the desired period of the executing loop in milliseconds.
     * @param periodStart
     *            the system time in milliseconds at the start of the current
     *            period.
     */
    public static void sleepToNextPeroid(int period, long periodStart)
    {
        sleep(period - (System.currentTimeMillis() - periodStart));
    }
}
