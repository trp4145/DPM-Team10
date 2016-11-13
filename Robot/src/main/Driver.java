package main;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import java.io.*;
import java.util.*;

/**
 * Contains the methods responsible for moving the robot.
 * 
 * @author Scott Sewell
 */
public class Driver
{
    // how much error is allowed between the odometer position and destination position.
    private static final float POSITION_TOLERANCE = 2.0f;
    
    private EV3LargeRegulatedMotor m_leftMotor;
    private EV3LargeRegulatedMotor m_rightMotor;
    private Odometer m_odometer;
    
    private Vector2 m_destination;
    
    /**
     * Constructor.
     */
    public Driver(Odometer odometer)
    {
        m_odometer = odometer;
        
        m_leftMotor = Robot.MOTOR_LEFT;
        m_rightMotor = Robot.MOTOR_RIGHT;

        // reset the motors
        for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { m_leftMotor, m_rightMotor })
        {
            motor.stop();
            motor.setAcceleration(Robot.ACCELERATION);
        }
    }
    
    /**
     * Begins moving the robot towards a specific position. The calling 
     * thread is not blocked.
     * @param destination The position to move towards on the board in cm.
     */
    public void travelTo(Vector2 destination)
    {
        m_destination = destination;
        
        Vector2 currentLocation = m_odometer.getPosition();
    	
    	// turn to face new waypoint
    	turnTo(Vector2.subtract(destination, currentLocation).angle());

    	// a short pause to allow time for distance sensor to get some values
    	Utils.sleep(350);
        
    	// calculate the distance to the new location
    	float distance = Vector2.distance(currentLocation, destination); 
    	
    	// set motor speed
		m_leftMotor.setSpeed(Robot.MOVE_SPEED); 
		m_rightMotor.setSpeed(Robot.MOVE_SPEED);
		
		// rotate motors
		m_leftMotor.rotate(convertDistance(Robot.WHEEL_RADIUS, distance), true);
		m_rightMotor.rotate(convertDistance(Robot.WHEEL_RADIUS, distance), true);
    }

    /**
     * Turns the robot to face the specified angle. Blocks the calling
     * thread until the action is complete.
     * @param targetAngle the world space angle to face in degrees.
     */
    public void turnTo(float targetAngle)
    {
    	float bearing = Utils.toBearing(targetAngle - m_odometer.getTheta());
    	
    	// set rotating speeds
    	m_leftMotor.setSpeed(Robot.ROTATE_SPEED);
		m_rightMotor.setSpeed(Robot.ROTATE_SPEED);
		
		// rotate the motors to complete the motion
		m_leftMotor.rotate(-convertAngle(Robot.WHEEL_RADIUS, bearing), true);
		m_rightMotor.rotate(convertAngle(Robot.WHEEL_RADIUS, bearing), false);
    }
    
    /**
     * Turns the robot a specific amount relative to the current facing.
     * The thread is blocked until the action is complete.
     * @param angle The angle in degrees the robot will turn.
     */
    public void turn(float angle)
    {
    	// set rotating speeds
    	m_leftMotor.setSpeed(Robot.ROTATE_SPEED);
		m_rightMotor.setSpeed(Robot.ROTATE_SPEED);
		
		// rotate the motors to complete the motion
		m_leftMotor.rotate(-convertAngle(Robot.WHEEL_RADIUS, angle), true);
        m_rightMotor.rotate(convertAngle(Robot.WHEEL_RADIUS, angle), false);  
    }
    
    /**
     * Immediately stops the robot's motion.
     */
    public void stop()
    {
        m_leftMotor.stop(true);
        m_rightMotor.stop(false);
        m_destination = null;
    }
    
    /**
     * @return true if the robot is still attempting to reach a destination.
     */
    public boolean isTravelling()
    {
        return m_destination != null;
    }
    
    /**
     * @return true if the robot is near to its destination.
     */
    public boolean isNearDestination()
    {
        return Vector2.distance(m_destination, m_odometer.getPosition()) < POSITION_TOLERANCE;
    }

    /**
     * Calculates the number of motor rotations needed to travel a specific distance
     * @param radius the radius of the wheel attached to the motor in cm.
     * @param distance how far to move in cm.
     * @return The amount a motor must rotate to travel the given distance.
     */
    private int convertDistance(float radius, float distance)
	{
		return (int)((180 * distance) / (Math.PI * radius));
	}
    
	/**
	 * Calculates the number of motor rotations needed to rotate the robot a specified angle.
	 * @param radius the radius of the wheel attached to the motor in cm.
	 * @param angle the angle to turn in degrees.
	 * @return The amount the motor must rotate to turn the robot the given angle.
	 */
	private int convertAngle(float radius, float angle)
	{
		return convertDistance(radius, (float)Math.PI * Robot.WHEEL_TRACK * angle / 360f);
	}
}
