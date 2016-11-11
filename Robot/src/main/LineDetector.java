package main;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * Contains the methods responsible for fetching and processing 
 * the samples taken by the LightSensor
 * 
 * @author Aimee Ascencio
 */
public class LineDetector extends Thread
{
	//sensor polling period 
	private static final int UPDATE_PERIOD = 30;
	
	private SampleProvider m_colorSensor;
	private float m_brightness;
	private Object m_lock; 
	
	
	/**
	 * Constructor
	 */
	public LineDetector(EV3ColorSensor colorSensor)
	{
		m_colorSensor = colorSensor.getMode("Red");
		m_lock = new Object();
	}
	
	/**
	 * Main loop to detect brightness 
	 */	
	public void run()
	{
		long updateStart;
		while(true)
		{
			updateStart = System.currentTimeMillis();
			float[] collectedSample = new float [m_colorSensor.sampleSize()];
			//sample color sensor
			m_colorSensor.fetchSample(collectedSample, 0);
			synchronized(m_lock)
			{
				m_brightness = 	collectedSample[0];			
			}
			
			Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart);	
		}
	}
	
	/**
	 * Brightness getter 		
	 * @return the brightness detected by the color sensor
	 */
	public float getBrightness()
	{
		synchronized (m_lock)
		{
			return m_brightness; 
		}	
	}
	
}
