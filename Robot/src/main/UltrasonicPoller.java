package main;
import lejos.robotics.SampleProvider;
import java.util.*;

/**
 * Contains the methods (basic) responsible for fetching and processing 
 * the samples taken by the Ultrasonic sensor
 * 
 * @author Aimee Ascencio 
 */
public class UltrasonicPoller extends Thread
{
	//Period (in ms) at which the sensor is polling 
	private static final int UPDATE_PERIOD = 20;
	//Maximum distance (in cm) that distance measured is clamped under
	private static final int MAX_RANGE = 200;
	
	private SampleProvider m_sensor;
	private float m_distance; 
	
	//lock object for mutual exclusion 
	private Object m_lock;
	
	/**
	 * Constructor 
	 */
	public UltrasonicPoller()
	{
		m_sensor = Robot.ULTRASOUND_MAIN.getMode("Distance");
		m_lock = new Object ();		
	}
	
	/**
	 * Basic sampler for the Ultrasonic Sensor (no filtering)   
	 */
	public void run()
	{
		long updateStart;
		while (true)
		{
			updateStart = System.currentTimeMillis();
			//get distance sample 
			float[] collectedSample = new float[m_sensor.sampleSize()];
			m_sensor.fetchSample(collectedSample, 0);
			synchronized (m_lock)
			{
				m_distance = Utils.clamp(collectedSample[0], 0, MAX_RANGE);
			}
			
			Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart); 	
		}	
	}
	
	/**
	 * Distance getter 
	 * @return the distance detected by the sensor 
	 */
	public float getDistance()
	{
		synchronized(m_lock)
		{
			return m_distance; 
		}
	}
}
