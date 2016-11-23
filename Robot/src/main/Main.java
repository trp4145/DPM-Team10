package main;

import java.nio.file.attribute.AclEntry.Builder;
import java.util.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;

/**
 * The main class that manages most of the decision making aspects of the robot.
 *
 * @author Scott Sewell
 */
public class Main
{
    // the duration of the match in seconds
    private static final long MATCH_DURATION = 5 * 60;
    // how far the robot sees while localizing in cm
    private static final float LOCALIZATION_DISTANCE = 45;
    // the distance in cm the robot is from a block it is identifying
    private static final float LOCALIZATION_DISTANCE = 45;
    // number of blocks the the robot will try to stack before dropping them off
    private static final int BLOCK_STACK_SIZE = 1;
    // the distance in cm ahead of the robot in which obstacles are seen 
    private static final float OBSTACLE_DISTANCE = 6;
    // the distance in cm the robot moves to either side of an obstacle when trying to avoid it
    private static final float AVOID_DISTANCE = 30;
    
    private StartParameters m_startParams;
    private Board m_board;
    private Odometer m_odometer;
    private OdometryCorrection m_odoCorrection;
    private UltrasonicPoller m_usMain;
    private UltrasonicPoller m_usUpper;
    private Driver m_driver;
    private HeldBlockManager m_blockManager;
    private Display m_display;

    private long m_startTime;
    
    /**
     * Launches the main program.
     */
    public static void main(String[] args)
    {
        Main main = new Main();
        main.launch();
    }

    /**
     * Gets starting info, runs threads, and begins the main logic loop.
     */
    private void launch()
    {
        // initialize
        m_usMain = new UltrasonicPoller(Robot.ULTRASOUND_MAIN);
        m_usUpper = new UltrasonicPoller(Robot.ULTRASOUND_UPPER);
        m_odometer = new Odometer();
        m_odoCorrection = new OdometryCorrection(m_odometer);
        m_driver = new Driver(m_odometer);
        m_blockManager = new HeldBlockManager();
        m_display = new Display(m_odometer);

        // choose whether to use wifi or test parameters.
        m_startParams = new StartParameters();
        if (m_display.getMenuResponse("Use Wifi", "Use Test Data") == Button.ID_LEFT)
        {
            // wait to progress until start information is received via wifi
            while (!m_startParams.hasRecievedData())
            {
                m_startParams.getWifiData();
                Utils.sleep(100);
            }
        }
        else
        {
            m_startParams.useTestData();
        }
        
        // record the starting time
        m_startTime = System.currentTimeMillis();

        // get the board
        m_board = m_startParams.getBoard();

        // start threads
        m_usMain.start();
        m_usUpper.start();
        m_odometer.start();
        m_display.start();
        
        // localize
        localize(true);
        
        // start odometry correction now that localization is done
        m_odoCorrection.start();
        
        // initialize the claw
        m_blockManager.initializeClaw();
        
        // temp block search
        m_driver.turn(90, Robot.ROTATE_SPEED / 3, false);
        while (m_usMain.getFilteredDistance() > 80) {}
        Utils.sleep(1625);
        m_driver.stop();
        float blockDistance = m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX();
        
        float checkDistance = 5f + Robot.RADIUS;
        m_driver.goForward(blockDistance - checkDistance, true);
        m_driver.turn(-90, Robot.ROTATE_SPEED, true);
        boolean isBlueBlock = m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY() > (checkDistance + 10);
        m_driver.turn(90, Robot.ROTATE_SPEED, true);
        if (isBlueBlock)
        {
            Sound.buzz();
            m_driver.goForward(checkDistance - 8, true);
            m_blockManager.captureBlock();
        }
        else
        {
            Sound.twoBeeps();
        }
        Utils.sleep(500);

        // drop off any held blocks once we have enough
        if (m_blockManager.getBlockCount() >= BLOCK_STACK_SIZE)
        {
            // move to the appropriate zone
            moveWhileAvoiding(m_startParams.isBuilder() ? m_board.getBuildZoneCenter() : m_board.getDumpZoneCenter());
            m_blockManager.releaseBlock();
        }
        
        // we must move back to the start corner before the end of the match
        moveWhileAvoiding(m_board.getStartPos());
        
        // finish
        System.exit(0);
    }

    /**
     * Attempts to set the odometer's angle to match the board's coordinates by
     * rotating near a board corner. Uses the ultrasonic sensor to determine
     * angles at which the walls are seen.
     * 
     * @param moveToOrigin 
     *            if true moves the robot to the line intersection nearest to
     *            the corner after calculating its position.
     */
    private void localize(boolean moveToOrigin)
    {
        m_odometer.setTheta(0);
        m_odometer.setPosition(Vector2.zero());

        // start the robot turning one revolution and record the seen distances
        // along with the angles they were captured at
        List<Float> orientations = new ArrayList<Float>();
        List<Float> distances = new ArrayList<Float>();
        m_driver.turn(360, Robot.LOCALIZATION_SPEED, false);
        while (m_driver.isTravelling())
        {
            orientations.add(Utils.normalizeAngle(m_odometer.getTheta() + 90));
            distances.add(m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY());
            Utils.sleep(UltrasonicPoller.UPDATE_PERIOD * 2);
        }
        
        // find all the angles that correspond to when the distance rises above
        // LOCALIZATION_DISTANCE and when it lowers below LOCALIZATION_DISTANCE
        List<Float> risingAngles = new ArrayList<Float>();
        List<Float> fallingAngles = new ArrayList<Float>();
        for (int i = 0; i < orientations.size(); i++)
        {
            float dist = distances.get(i);
            float nextDist = distances.get((i + 1) % distances.size());
            
            if (dist < LOCALIZATION_DISTANCE && nextDist > LOCALIZATION_DISTANCE)
            {
                risingAngles.add(orientations.get(i));
            }
            
            if (dist > LOCALIZATION_DISTANCE && nextDist < LOCALIZATION_DISTANCE)
            {
                fallingAngles.add(orientations.get(i));
            }
        }

        // determine which falling and rising edge angles correspond to the wall
        // as to filter out any blocks new the start point. We know that rising
        // falling angle pair with the largest angle between them is the pair
        // that belong to the wall.
        float largestBearing = Float.MIN_VALUE;
        float angle = 0;
        for (float risingAng : risingAngles)
        {
            for (float fallingAng : fallingAngles)
            {
                float bearing = Math.abs(Utils.toBearing(risingAng - fallingAng));
                if (bearing > largestBearing)
                {
                    largestBearing = bearing;
                    angle = 315 - (bearing / 2) - risingAng;
                }
            }
        }
        
        // account for the starting corner the robot is in
        float cornerAngOffset = 90 * m_startParams.getStartCorner();

        // set odometer angle accounting for start corner
        m_odometer.setTheta(angle + cornerAngOffset);
        
        Vector2 startPos = new Vector2(
                distances.get(Utils.closestIndex(Utils.normalizeAngle(180 - angle), orientations)) - Board.TILE_SIZE,
                Board.TILE_SIZE - distances.get(Utils.closestIndex(Utils.normalizeAngle(90 - angle), orientations))
                );
        
        m_odometer.setPosition(m_board.getStartPos().add(startPos.rotate(cornerAngOffset)));
        
        Sound.beepSequenceUp();
        
        // if applicable, move to the nearest line intersection
        if (moveToOrigin)
        {
            m_driver.travelTo(Board.getNearestIntersection(m_odometer.getPosition()), true);
            m_driver.turnTo(cornerAngOffset - 90, true);
        }
    }

    /**
     * Moves the robot to a position while avoiding obstacles on the way.
     * 
     * @param position
     *            the destination point.
     */
    private void moveWhileAvoiding(Vector2 position)
    {
        m_driver.setDestination(position);
        while (!m_driver.isNearDestination())
        {
            m_driver.travelTo(position, false);
            while ( m_driver.isTravelling() &&
                    m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX() > Robot.RADIUS + OBSTACLE_DISTANCE
                    ) {}
            if (m_driver.isTravelling() && Vector2.distance(position, m_odometer.getPosition()) > OBSTACLE_DISTANCE)
            {
                m_driver.stop();
                avoidObstacle();
            }
        }
    }

    /**
     * Moves the robot some distance to the left or right, depending on which
     * direction is clearer of other obstacles.
     */
    private void avoidObstacle()
    {
        // check if there is no obstacle nearby on the left using the left ultrasound sensor
        // also check if the avoidance detour will cross into a invalid position
        Vector2 leftAvoidWaypoint = m_odometer.toWorldSpace(new Vector2(0, AVOID_DISTANCE));
        Vector2 rightAvoidWaypoint = m_odometer.toWorldSpace(new Vector2(0, -AVOID_DISTANCE));

        float leftDistance = m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY();
        if (leftDistance > Robot.RADIUS + AVOID_DISTANCE && checkValidity(leftAvoidWaypoint))
        {
            m_driver.travelTo(leftAvoidWaypoint, true);
        }
        else
        {
            // if the left way around is invalid, try looking right at check if it is clear
            m_driver.turn(-90, Robot.ROTATE_SPEED, true);

            float rightDistance = m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX();
            if (rightDistance > Robot.RADIUS + AVOID_DISTANCE && checkValidity(rightAvoidWaypoint))
            {
                m_driver.goForward(AVOID_DISTANCE, true);
            }
            else if (leftDistance > rightDistance) // if both invalid, pick the better bet
            {
                m_driver.turn(180, Robot.ROTATE_SPEED, true);
                m_driver.goForward(AVOID_DISTANCE, true);
            }
        }
    }

    /**
     * Determines if a point on the board in a valid place for the robot to go.
     * 
     * @param destination
     *            the world space position to check.
     * @return true if the destination point doesn't overlap any invalid
     *         regions.
     */
    private boolean checkValidity(Vector2 destination)
    {
        return m_board.inBounds(destination) && !(m_startParams.isBuilder() ? m_board.inDumpZone(destination) : m_board.inBuildZone(destination));
    }

    /**
     * Gets the time remaining in the match.
     * 
     * @return the time in seconds.
     */
    private float getTimeRemaining()
    {
        return (System.currentTimeMillis() - m_startTime) / 1000f;
    }
}