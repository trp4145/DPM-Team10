package main;

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
        
        float checkDistance = 25f;
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
        
        m_driver.travelTo(m_board.getBuildZoneCenter(), true);

        if (m_blockManager.getBlockCount() > 0)
        {
            m_blockManager.releaseBlock();
        }

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
        Utils.sleep(1200);
        while (m_usMain.getFilteredDistance() < LOCALIZATION_DISTANCE) {}
        
        // store the angle of the other wall
        m_driver.stop();
        angleB = m_odometer.getTheta();
        
        // set the odometer using the measured angles
        float angle = 315 - (Math.abs(angleB - angleA) / 2);
        m_odometer.setTheta(angle + cornerAngOffset);
        
        // turn to face an ultrasound sensors at each wall, wait a bit, then grab distance sample
        m_driver.turnTo(90 + cornerAngOffset, true);
        Utils.sleep(200);
        Vector2 startPos = new Vector2(
                m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY() - Board.TILE_SIZE,
                Board.TILE_SIZE - (m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX())
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