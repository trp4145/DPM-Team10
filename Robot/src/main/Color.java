package main;

import java.security.InvalidParameterException;

/**
 * @author Scott Sewell
 * Stores a color in the HSV format. H,S,V vales are within [0,1]
 */
public class Color
{
    private float m_h;
    private float m_s;
    private float m_v;

    // Define useful colors.
    public static Color black()    { return new Color(0,0,0); }
    public static Color white()    { return new Color(0,0,1); }

    /**
     * Empty Constructor.
     */
    public Color() { }
    
    /**
     * Constructor. Values are clamped to [0,1]
     * @param h the hue (what color).
     * @param s the saturation (how intense that color is).
     * @param v the value (how bright that color is).
     */
    public Color(float h, float s, float v)
    {
        setH(h);
        setS(s);
        setV(v);
    }
    
    /**
     * Clone constructor.
     * @param c color
     */
    public Color(Color c)
    {
        if (c != null)
        {
            m_h = c.getH();
            m_s = c.getS();
            m_v = c.getV();  
        }
        else
        {
            throw new InvalidParameterException("Attempted to clone a null Color!");
        }
    }
    
    /**
     * Converts a color from the RGB color space into an HSV color space.
     * @param r the red component [0,1]
     * @param g the green component [0,1]
     * @param b the blue component [0,1]
     * @return a new HSV color.
     */
    public Color fromRGB(float r, float g, float b)
    {
        float[] hsv = new float[3];
        // convert to 24-bit RGB accepted by the conversion method.
        java.awt.Color.RGBtoHSB((int)(r * 255), (int)(g * 255), (int)(b * 255), hsv);
        return new Color(hsv[0], hsv[1], hsv[2]);
    }
    
    /**
     * Linearly interpolates hsv values of c1 to those of c2
     * using fac as the blend factor.
     * @param c1 the color blended from.
     * @param c2 the color blended towards.
     * @param fac blend factor with range [0,1].
     * @return A new color blended between the input colors.
     */
    public static Color lerp(Color c1, Color c2, float fac)
    {
        fac = Utils.clamp(fac, 0, 1);
        return new Color(
                    c1.getH() * (1 - fac) + c2.getH() * fac,
                    c1.getS() * (1 - fac) + c2.getS() * fac,
                    c1.getV() * (1 - fac) + c2.getV() * fac
                );
    }
    
    /**
     * @return the hue.
     */
    public float getH()
    {
        return m_h;
    }

    /**
     * @return the saturation.
     */
    public float getS()
    {
        return m_s;
    }

    /**
     * @return the brightness.
     */
    public float getV()
    {
        return m_v;
    }
    
    /**
     * Sets the hue.
     * @param h the new hue value.
     */
    public void setH(float h)
    {
        m_h = Utils.clamp(h, 0, 1);
    }

    /**
     * Sets the saturation.
     * @param s the new saturation value.
     */
    public void setS(float s)
    {
        m_s = Utils.clamp(s, 0, 1);
    }

    /**
     * Sets the brightness.
     * @param v the new brightness value.
     */
    public void setV(float v)
    {
        m_v = Utils.clamp(v, 0, 1);
    }
}
