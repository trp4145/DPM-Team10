package main;

import lejos.hardware.Sound;

/**
 * Contains the methods responsible for correcting the odometer based on the
 * inputs received by two color sensors.
 * 
 * @author Aimee Ascencio
 */
public class OdometryCorrection extends Thread
{
    // how much the corrected values override the original values
    private static final float CORRECTION_WEIGHT = 0.5f;

    private Odometer m_odometer;
    private LineDetector m_rightLineDetector;
    private LineDetector m_leftLineDetector;

    /**
     * Constructor.
     * 
     * @param odometer
     *            the odometer to which correction is applied.
     */
    public OdometryCorrection(Odometer odometer)
    {
        m_odometer = odometer;
        m_rightLineDetector = new LineDetector(Robot.COLOR_RIGHT);
        m_leftLineDetector = new LineDetector(Robot.COLOR_LEFT);
    }

    /**
     * Main loop.
     */
    public void run()
    {
        // start the line detector threads
        m_rightLineDetector.start();
        m_leftLineDetector.start();

        long updateStart;
        while (true)
        {
            updateStart = System.currentTimeMillis();

            if (m_leftLineDetector.detectedLine())
            {
                correctPosition(Robot.CSL_OFFSET);
            }

            if (m_rightLineDetector.detectedLine())
            {
                correctPosition(Robot.CSR_OFFSET);
            }

            Utils.sleepToNextPeroid(LineDetector.UPDATE_PERIOD, updateStart);
        }
    }

    /**
     * Correct a component of the odometer position to what is probably the
     * nearest line.
     * 
     * @param colorSensorOffset
     *            the position of the color sensor in local space.
     */
    private void correctPosition(Vector2 colorSensorOffset)
    {
        // play sound indicating a correction is occuring.
        Sound.beep();
        
        // get the position of the color sensor on the board
        Vector2 colorSensorPos = m_odometer.toWorldSpace(colorSensorOffset);
        // find the board line intersection closest to the color sensor
        Vector2 intersection = Board.getNearestIntersection(colorSensorPos);
        // get the displacement of the line intersection from the color sensor
        Vector2 dispFromLines = intersection.subtract(colorSensorPos);
        // reduce the displacement error so that the correction is smaller
        dispFromLines.scale(CORRECTION_WEIGHT);

        // correct the axis that is closer to a line, as this is likely the line
        // that triggered the sensor
        Vector2 correction;
        if (Math.abs(dispFromLines.getX()) < Math.abs(dispFromLines.getY()))
        {
            correction = new Vector2(dispFromLines.getX(), 0);
        }
        else
        {
            correction = new Vector2(0, dispFromLines.getY());
        }
        m_odometer.setPosition(m_odometer.getPosition().add(correction));
    }
}
