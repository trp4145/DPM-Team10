package main;

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
    // how far the robot sees while localizing
    private static final float LOCALIZATION_DISTANCE = 30.0f;
    
    private StartParameters m_startParams;
    private Board m_board;
    private Odometer m_odometer;
    private OdometryCorrection m_odoCorrection;
    private UltrasonicPoller m_usMain;
    private UltrasonicPoller m_usUpper;
    private Driver m_driver;
    private HeldBlockManager m_blockManager;
    private Display m_display;

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
            }
        }
        else
        {
        	m_startParams.useTestData();
        }

        // get the board
        m_board = m_startParams.getBoard();

        // start threads
        m_usMain.start();
        m_usUpper.start();
        m_odometer.start();
        m_display.start();
        
        //initialize claw 
        m_blockManager.initializeClaw();
        
        // localize
//        localize(true);
        
        // start odometry correction now that localization is done
        m_odoCorrection.start();
                
        //testing 
        m_driver.travelTo(Vector2.zero(), true);
       // m_driver.turnTo(0, true);
        m_driver.travelTo(new Vector2(0,180) , true);

        
        
        
        // temp block search
//        m_driver.turn(90, Robot.ROTATE_SPEED / 3, false);
//        while (m_usMain.getFilteredDistance() > 80) {}
//        Utils.sleep(1625);
//        m_driver.stop();
//        float blockDistance = m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX();
//        
//        float checkDistance = 25f;
//        m_driver.goForward(blockDistance - checkDistance, true);
//        m_driver.turn(-90, Robot.ROTATE_SPEED, true);
//        boolean isBlueBlock = m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY() > (checkDistance + 10);
//        m_driver.turn(90, Robot.ROTATE_SPEED, true);
//        if (isBlueBlock)
//        {
//            Sound.buzz();
//            m_driver.goForward(checkDistance - 8, true);
//            m_blockManager.captureBlock();
//        }
//        else
//        {
//            Sound.twoBeeps();
//        }
//        Utils.sleep(500);
//        
//        m_driver.travelTo(m_board.getBuildZoneCenter(), true);
//
//
//        if (m_blockManager.getBlockCount() > 0)
//        {
//            m_blockManager.releaseBlock();
//        }
        
        

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
    public void localize(boolean moveToOrigin)
    {
        // account for the starting corner the robot is in
        int corner =  m_startParams.getStartCorner();
        float cornerAngOffset = 90 * corner;
        Vector2 cornerPos = new Vector2(
                            corner == 2 || corner == 3 ? (Board.TILE_COUNT - 2) * Board.TILE_SIZE : 0,
                            corner == 3 || corner == 4 ? (Board.TILE_COUNT - 2) * Board.TILE_SIZE : 0
                         );

        // start the robot turning one revolution and record the seen distances
        // along with the angles they were captured at
        List<Float> orientations = new ArrayList<Float>();
        List<Float> distances = new ArrayList<Float>();
        m_driver.turn(-360, Robot.LOCALIZATION_SPEED, false);
        while (m_driver.isTravelling())
        {
            orientations.add(m_odometer.getTheta());
            distances.add(m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY());
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
                    angle = 315 - (bearing / 2) + (m_odometer.getTheta() - risingAng) - 90;
                }
            }
        }

        // set odometer angle accounting for start corner
        m_odometer.setTheta(angle + cornerAngOffset);
        
        Vector2 startPos = new Vector2(
                distances.get(Utils.closestIndex(Utils.normalizeAngle(-angle), orientations)) - Board.TILE_SIZE,
                Board.TILE_SIZE - distances.get(Utils.closestIndex(Utils.normalizeAngle(90 - angle), orientations))
                );
        
        m_odometer.setPosition(cornerPos.add(startPos.rotate(cornerAngOffset)));
        
        Sound.beepSequenceUp();
        
        // if applicable, move to the nearest line intersection
        if (moveToOrigin)
        {
            m_driver.travelTo(Board.getNearestIntersection(m_odometer.getPosition()), true);
            m_driver.turnTo(cornerAngOffset - 90, true);
        }
    }
}