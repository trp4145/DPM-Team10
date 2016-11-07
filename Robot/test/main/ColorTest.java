/**
 * 
 */
package main;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import org.junit.Test;

/**
 * @author yazami
 *
 */
public class ColorTest {

    /**
     * Test method for {@link main.Color#black()}.
     */
    @Test
    public void testBlack() 
    {
        Color black = Color.black();
        
        float expectedHue = 0;
        float expectedSaturation = 0;
        float expectedBrightness = 0;
        
        assertEquals(expectedHue, black.getH(), 0f);
        assertEquals(expectedSaturation, black.getS(), 0f);
        assertEquals(expectedBrightness, black.getV(), 0f);
    }

    /**
     * Test method for {@link main.Color#white()}.
     */
    @Test
    public void testWhite() 
    {
        Color white = Color.white();
        
        float expectedHue = 0;
        float expectedSaturation = 0;
        float expectedBrightness = 1;
        
        assertEquals(expectedHue, white.getH(), 0f);
        assertEquals(expectedSaturation, white.getS(), 0f);
        assertEquals(expectedBrightness, white.getV(), 0f);
    }
    
    /**
     * Test method for {@link main.Color#Color(float, float, float)}.
     */
    @Test
    public void testColorFloatFloatFloat() 
    {
        float h = 0.5f;
        float s = 0.3f;
        float v = 1f;
        
        Color col1 = new Color(h, s, v);
        
        assertEquals(h, col1.getH(), 0f);
        assertEquals(s, col1.getS(), 0f);
        assertEquals(v, col1.getV(), 0f);
    }

    /**
     * Test method for {@link main.Color#Color(main.Color)}.
     */
    @Test
    public void testColorColor() 
    {
        float h = 0.5f;
        float s = 0.3f;
        float v = 1f;
        
        Color col1 = new Color(h, s, v);
        Color col2 = new Color(col1);
        
        assertEquals(h, col2.getH(), 0f);
        assertEquals(s, col2.getS(), 0f);
        assertEquals(v, col2.getV(), 0f);
    }
    
    /**
     * Test method for {@link main.Color#Color(main.Color)}.
     */
    @Test (expected = InvalidParameterException.class)
    public void testColorColorError() 
    {
        Color col1 = new Color(null);
    }

    /**
     * Test method for {@link main.Color#fromRGB(float, float, float)}.
     */
    @Test
    public void testFromRGB() 
    {
        // Red
        float r = 1;
        float g = 0;
        float b = 0;
        
        Color toHSV = Color.black().fromRGB(r, g, b);
        
        float expectedHue = 0;
        float expectedSaturation = 1;
        float expectedBrightness = 1;
   
        assertEquals(expectedHue, toHSV.getH(), 0f);
        assertEquals(expectedSaturation, toHSV.getS(), 0f);
        assertEquals(expectedBrightness, toHSV.getV(), 0f);        
    }

    /**
     * Test method for {@link main.Color#lerp(main.Color, main.Color, float)}.
     */
    @Test
    public void testLerp() 
    {
        Color c1 = Color.black();
        Color c2 = new Color(0,1,1); // red     
        Color c3 = Color.lerp(c1, c2, 0.5f);
        
        float expectedHue = 0;
        float expectedSaturation = 0.5f;
        float expectedBrightness = 0.5f;
        
        assertEquals(expectedHue, c3.getH(), 0f);
        assertEquals(expectedSaturation, c3.getS(), 0f);
        assertEquals(expectedBrightness, c3.getV(), 0f);   
    }
}
