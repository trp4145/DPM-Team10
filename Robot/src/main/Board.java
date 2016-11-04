package main;

import lejos.robotics.geometry.*;

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
    
    
    // various zone rectangles
    private Rectangle m_board;
    private Rectangle m_dumpZone;
    private Rectangle m_buildZone;
    
    
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
        Vector2 wallLowerCorner = Vector2.one().scale(-TILE_SIZE);
        Vector2 wallUpperCorner = Vector2.one().scale(TILE_SIZE * (BOARD_TILE_COUNT - 1));
        m_board = Utils.toRect(wallLowerCorner, wallUpperCorner);

        Vector2 dumpLowerCorner = new Vector2(lrzx, lrzy).scale(TILE_SIZE);
        Vector2 dumpUpperCorner = new Vector2(urzx, urzy).scale(TILE_SIZE);
        m_dumpZone = Utils.toRect(dumpLowerCorner, dumpUpperCorner);
        
        Vector2 buildLowerCorner = new Vector2(lgzx, lgzy).scale(TILE_SIZE);
        Vector2 buildUpperCorner = new Vector2(ugzx, ugzy).scale(TILE_SIZE);
        m_buildZone = Utils.toRect(buildLowerCorner, buildUpperCorner);
    }
    
    /**
     * Checks if the robot would fit on the board while at a specified position. 
     * @param position to check the validity of.
     * @return true if the robot should fit at the position.
     */
    public boolean inBounds(Vector2 position)
    {
        float padding = -(Robot.RADIUS + WALL_BUFFER);
        return Utils.rectContains(position, Utils.padRect(m_board, padding));
    }
    
    /**
     * Checks if the robot could overlap the build zone while at a specified position. 
     * @param position to check the validity of.
     * @return true if the robot could overlap at the position.
     */
    public boolean inBuildZone(Vector2 position)
    {
        float padding = Robot.RADIUS + ZONE_BUFFER;
        return Utils.rectContains(position, Utils.padRect(m_buildZone, padding));
    }
    
    /**
     * Checks if the robot could overlap the dump zone while at a specified position. 
     * @param position to check the validity of.
     * @return true if the robot could overlap at the position.
     */
    public boolean inDumpZone(Vector2 position)
    {
        float padding = Robot.RADIUS + ZONE_BUFFER;
        return Utils.rectContains(position, Utils.padRect(m_dumpZone, padding));
    }
    
    /**
     * Checks if the robot would overlap the build zone while traveling between two points. 
     * @param lineStart the start of the travel path.
     * @param lineEnd the end of the travel path.
     * @return true if the robot could overlap while traveling.
     */
    public boolean crossesBuildZone(Vector2 lineStart, Vector2 lineEnd)
    {
        float padding = Robot.RADIUS + ZONE_BUFFER;
        return Utils.lineIntersectsRect(lineStart, lineEnd, Utils.padRect(m_buildZone, padding));
    }
    
    /**
     * Checks if the robot would overlap the dump zone while traveling between two points. 
     * @param lineStart the start of the travel path.
     * @param lineEnd the end of the travel path.
     * @return true if the robot could overlap while traveling.
     */
    public boolean crossesDumpZone(Vector2 lineStart, Vector2 lineEnd)
    {
        float padding = Robot.RADIUS + ZONE_BUFFER;
        return Utils.lineIntersectsRect(lineStart, lineEnd, Utils.padRect(m_dumpZone, padding));
    }
}