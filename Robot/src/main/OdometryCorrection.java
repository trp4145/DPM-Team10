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
    // how much the corrected values override the original values for position
    private static final float CORRECTION_WEIGHT_POSITION = .6f;
    // how much the corrected values override the original values for the angle
    private static final float CORRECTION_WEIGHT_ANGLE = .6f;
    //Maximum error that will be corrected
    private static final float CORRECTION_ARC = 15.0f;
    //min angle change that is considered a turn 
    private static final float MIN_TURNING_ANGLE = 1.0f;

    private Odometer m_odometer;
    private LineDetector m_rightLineDetector;
    private LineDetector m_leftLineDetector;
    private LinearRegression m_listPos;
    private float m_firstAngle;
    private int m_listSize; 
    
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
        m_listSize = 0; 
        
    }

    /**
     * Main loop.
     */
    public void run()
    {
        // start the line detector threads
        m_rightLineDetector.start();
        m_leftLineDetector.start();
        m_firstAngle = m_odometer.getTheta();

        long updateStart;
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
            
            if (m_listSize != m_listPos.sampleSize() && m_listPos.sampleSize()> 3 )
            {                
            	m_listSize = m_listPos.sampleSize();               
                if (correctAngle(m_listPos.slope())){
                	
                }else{
                	Sound.beep();
                }
            	      	
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
        dispFromLines.scale(CORRECTION_WEIGHT_POSITION);

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
        
        return m_odometer.getPosition();
    }
    
    /**
     * Correct the theta of the odometer, given that the error is not too large
     * @param angle
     *              The new angle to which we want to set odometer correction
     */
    private boolean correctAngle(float slope)
    {
        float error = (float) Math.toDegrees(Math.atan(slope)) - m_odometer.getTheta();
        while (Math.abs(error)> 90){
            error+= 180;
        }
        
        String filename = "slopeEstimated.txt";
        try (   FileWriter file = new FileWriter(filename, true);
                BufferedWriter writer = new BufferedWriter(file);
                PrintWriter out = new  PrintWriter(writer))
        {
            out.println(m_odometer.getTheta()+"\t"+error+"\t"+m_listPos.sampleSize());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
               
        if (!changedAngle() && Math.abs(error)<CORRECTION_ARC)
        {
            m_odometer.setTheta(m_odometer.getTheta()+error);
            return true;
        }
        return false; 
}
    
    /**
     * @return true if the angle has changed more than the minimum turning angle
     */
    private boolean changedAngle()
    {
    	Vector2 last = m_listPos.getLast();
    	if (Vector2.angleBetweenVectors(Vector2.fromPolar(m_firstAngle, 1), Vector2.fromPolar(m_odometer.getTheta(),1))> MIN_TURNING_ANGLE)
    	{
    		m_listPos.clearList();
    		m_firstAngle = m_odometer.getTheta();
//    		Sound.beep();    		
    		m_listPos.addPoint(last);
    		return true; 
    	}
    	
    	return false;    	
    }

    
}
