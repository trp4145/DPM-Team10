package main;

import java.util.*;
import lejos.hardware.Button;

/**
 * The main class that manages most of the decision making aspects of the robot.
 *
 * @author Scott Sewell
 */
public class Main
{
    private StartParameters m_startParams;
    private Board m_board;
    private Odometer m_odometer;
    private OdometryCorrection m_odoCorrection;
    private UltrasonicPoller m_usPoller;
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
        m_odometer = new Odometer();
        m_odoCorrection = new OdometryCorrection(m_odometer);
        m_usPoller = new UltrasonicPoller();
        m_driver = new Driver(m_odometer);
        m_display = new Display(m_odometer);

        // start initialization when ready.
        m_display.printString("Press a button!");
        Button.waitForAnyPress();

        /*
        // wait to progress until start information is received via wifi
        m_startParams = new StartParameters();
        while (!m_startParams.hasRecievedData())
        {
            m_startParams.getWifiData();
            // m_startParams.useTestData(); // use for testing to avoid having
            // to launch wifi server
        }
        // get the board
        m_board = m_startParams.getBoard();
        */

        // start threads
        m_odometer.start();
        m_odoCorrection.start();
        m_usPoller.start();
        m_display.start();

        List<Vector2> waypoints = new ArrayList<Vector2>();
        waypoints.add(new Vector2(60, 0));
        waypoints.add(new Vector2(60, -60));
        waypoints.add(new Vector2(0, -60));
        waypoints.add(new Vector2(0, 0));

        // traveling to destination
        while (waypoints.size() > 0)
        {
            m_driver.travelTo(waypoints.get(0));
            while (m_driver.isTravelling() && !m_driver.isNearDestination())
            {
            }
            waypoints.remove(0);
        }

        // finish
        System.exit(0);
    }
}