package main;

/**
 * Stores information about encountered blocks.
 */
public class Block
{
    public static final float BLUE_BLOCK_WIDTH = 10.0f;
    
    public enum Type { Wood, Blue }
    
    public Type type;
    public Vector2 position;
    
    /**
     * Constructor.
     * @param type The assumed type of the represented block.
     * @param position The assumed position of the represented block.
     */
    public Block(Type type, Vector2 position)
    {
        this.type = type;
        this.position = position;
    }
}
