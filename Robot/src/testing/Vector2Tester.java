package testing;

import static org.junit.Assert.*;
import org.junit.Test;
import main.Vector2;
import java.security.InvalidParameterException;

public class Vector2Tester
{
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
}
