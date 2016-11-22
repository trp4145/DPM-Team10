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
    private static final float LOCALIZATION_DISTANCE = 40;
    
    private StartParameters m_startParams;
    private Board m_board;
    private Odometer m_odometer;
    private OdometryCorrection m_odoCorrection;
    private UltrasonicPoller m_usMain;
    private UltrasonicPoller m_usUpper;
    private Driver m_driver;
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
        
        // localize
//        localize(true);
        
        m_odoCorrection.start();

        List<Vector2> waypoints = new ArrayList<Vector2>();
        waypoints.add(new Vector2(60, 0));
        waypoints.add(new Vector2(60, 60));
        waypoints.add(new Vector2(0, 60));
        waypoints.add(new Vector2(0, 0));
//        waypoints.add(new Vector2(45, 45));

        // traveling to destination
        while (waypoints.size() > 0)
        {
            m_driver.travelTo(waypoints.get(0));
            while (m_driver.isTravelling()) {}
            if (!m_driver.isNearDestination())
            {
                continue;
            }
            waypoints.remove(0);
        }
        
        m_driver.turnTo(0);
        // finish
//        System.exit(0);
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
        float angleA = 0;
        float angleB = 0;
        
        // start the robot turning one revolution
        m_driver.turn(-360, Robot.LOCALIZATION_SPEED, false);
        
        // turn until wall is seen
        while (m_usMain.getFilteredDistance() > LOCALIZATION_DISTANCE) {}
        Utils.sleep(500);

        // continue turning until no wall is seen
        while (m_usMain.getFilteredDistance() < LOCALIZATION_DISTANCE) {}

        // store the angle of the first wall and start turning the other way
        m_driver.stop();
        angleA = m_odometer.getTheta();
        m_driver.turn(360, Robot.LOCALIZATION_SPEED, false);

        // turn back facing wall until no wall is seen
        Utils.sleep(2000);
        while (m_usMain.getFilteredDistance() < LOCALIZATION_DISTANCE) {}
        
        // store the angle of the other wall
        m_driver.stop();
        angleB = m_odometer.getTheta();
        
        // set the odometer using the measured angles
        float angle = 315 - (Math.abs(angleB - angleA) / 2);
        m_odometer.setTheta(angle);
        
        // turn to face an ultrasound sensors at each wall, wait a bit, then grab distance sample
        m_driver.turnTo(90);
        Utils.sleep(200);
        Vector2 startPos = new Vector2(
                m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY() - Board.TILE_SIZE,
                Board.TILE_SIZE - (m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX())
                );
        
        // account for the starting corner the robot is in
        int corner =  m_startParams.getStartCorner();
        float cornerAngOffset = 90 * corner;
        Vector2 cornerPos = new Vector2(
                            corner == 2 || corner == 3 ? (Board.TILE_COUNT - 2) * Board.TILE_SIZE : 0,
                            corner == 3 || corner == 4 ? (Board.TILE_COUNT - 2) * Board.TILE_SIZE : 0
                         );
        
        // update the odometer
        m_odometer.setTheta(m_odometer.getTheta() + cornerAngOffset);
        m_odometer.setPosition(cornerPos.add(startPos.rotate(cornerAngOffset)));
        
        // if applicable, move to the nearest line intersection
        if (moveToOrigin)
        {
            m_driver.travelTo(Board.getNearestIntersection(m_odometer.getPosition()));
            while (m_driver.isTravelling()) {}
            m_driver.turnTo(cornerAngOffset - 90);
        }
    }
}