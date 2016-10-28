package main;

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
}
