package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
    private static final float CORRECTION_WEIGHT = .4f;
    //error before correcting 
    private static final float MIN_CORRECTION_ARC = 0.5f;
    private static final float MAX_CORRECTION_ARC = 1.5f;

    private Odometer m_odometer;
    private LineDetector m_rightLineDetector;
    private LineDetector m_leftLineDetector;
    private LinearRegression m_listPos;
    
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
        
        m_listPos = new LinearRegression();        
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
        float angle = m_odometer.getTheta(); 
        while (true)
        {
            updateStart = System.currentTimeMillis();

            if (m_leftLineDetector.detectedLine())
            {
            	m_listPos.addPoint(correctPosition(Robot.CSL_OFFSET));
            }

            if (m_rightLineDetector.detectedLine())
            {
            	m_listPos.addPoint(correctPosition(Robot.CSR_OFFSET));
            }
            
            if (m_listPos.sampleSize()> 3 )
            {                
                float slope = m_listPos.slope();
                correctAngle(slope);     
            	      	
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
    private Vector2 correctPosition(Vector2 colorSensorOffset)
    {
        // play sound indicating a correction is occuring.
//        Sound.beep();
        
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
        Vector2 newPos = m_odometer.getPosition().add(correction);
        m_odometer.setPosition(newPos);
        
        return newPos;
    }
    
    /**
     * Correct the theta of the odometer, given that the error is not too large
     * @param angle
     *              The new angle to which we want to set odometer correction
     */
    private void correctAngle(float slope)
    {
        float odometerAngle = m_odometer.getTheta();
        float error = (float) Math.toDegrees(Math.atan(slope)) - odometerAngle;
        while (Math.abs(error)> 90){
            error+= 180;
        }
        
        	
        String filename = "angleError.txt";
        try (   FileWriter file = new FileWriter(filename, true);
                BufferedWriter writer = new BufferedWriter(file);
                PrintWriter out = new  PrintWriter(writer))
        {
            out.println(error);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        
        if (error > MIN_CORRECTION_ARC && error < MAX_CORRECTION_ARC ){
            //m_odometer.setTheta(m_odometer.getTheta()+error*CORRECTION); 
        }else if (error > MAX_CORRECTION_ARC ){
            Sound.buzz();
            m_listPos.clearList();
        }

    }

    
}
