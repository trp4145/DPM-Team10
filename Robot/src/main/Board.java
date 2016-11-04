package main;

/**
 * Keeps track of properties of the board being driven on.
 */
public class Board
{
    // the number of tiles along one side of the board
    public static final int BOARD_TILE_COUNT = 12;
    // the width of a board tile in cm
    public static final float TILE_SIZE = 30.4f;
    // the distance the robot tries to leave between itself and the walls to prevent a collision in cm
    public static final float WALL_BUFFER = 5.0f;
    // the distance the robot tries to leave between itself and a zone to prevent entering in cm
    public static final float ZONE_BUFFER = 5.0f;
    
    
    // positions of the zone rectangles 
    private Vector2 m_dumpLowerCorner;
    private Vector2 m_dumpUpperCorner;
    private Vector2 m_buildLowerCorner;
    private Vector2 m_buildUpperCorner;
    
    
    /**
     * Constructor for the board.
     * @param lrzx the red zone lower corner x position
     * @param lrzy the red zone lower corner y position
     * @param urzx the red zone upper corner x position
     * @param urzy the red zone upper corner y position
     * @param lgzx the green zone lower corner x position
     * @param lgzy the green zone lower corner y position
     * @param ugzx the green zone upper corner x position
     * @param ugzy the green zone upper corner y position
     */
    public Board(int lrzx, int lrzy, int urzx, int urzy, int lgzx, int lgzy, int ugzx, int ugzy)
    {
        m_dumpLowerCorner = new Vector2(lrzx, lrzy).scale(TILE_SIZE);
        m_dumpUpperCorner = new Vector2(urzx, urzy).scale(TILE_SIZE);
        m_buildLowerCorner = new Vector2(lgzx, lgzy).scale(TILE_SIZE);
        m_buildUpperCorner = new Vector2(ugzx, ugzy).scale(TILE_SIZE);
    }
    
    /**
     * Checks if the robot would fit on the board while at a specified position. 
     * @param position to check the validity of.
     * @return true if the robot should fit at the position.
     */
    public boolean inBounds(Vector2 position)
    {
        float buffer = Robot.RADIUS + WALL_BUFFER;
        Vector2 lowerCorner = Vector2.one().scale(-TILE_SIZE + buffer);
        Vector2 upperCorner = Vector2.one().scale((TILE_SIZE * BOARD_TILE_COUNT) - buffer);
        return Utils.rectContains(position, lowerCorner, upperCorner);
    }
    
    /**
     * Checks if the robot would fit on the board while at a specified position. 
     * @param position to check the validity of.
     * @return true if the robot should fit at the position.
     */
    public boolean inBuildZone(Vector2 position)
    {
        float buffer = Robot.RADIUS + ZONE_BUFFER;
        Vector2 lowerCorner = new Vector2(m_buildLowerCorner).subtract(Vector2.one().scale(buffer));
        Vector2 upperCorner = new Vector2(m_buildUpperCorner).add(Vector2.one().scale(buffer));
        return Utils.rectContains(position, lowerCorner, upperCorner);
    }
}