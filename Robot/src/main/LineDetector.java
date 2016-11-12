package main;

import java.util.LinkedList;
import java.util.Queue;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * Contains the methods responsible for fetching and processing 
 * the samples taken by the LightSensor
 * Responsible for detecting lines
 * 
 * @author Aimee Ascencio
 */
public class LineDetector extends Thread
{
	//sensor polling period 
	private static final int UPDATE_PERIOD = 30;
	//how many past brightness values are store 
	private static final int BUFFER_SIZE = 20;
	//time that has to have passed to detect another line 
	private static final int MIN_TIME_BETWEEN_LINES = 90;
	//The minimum change in brightness to consider it a line 
	private static final float BRIGHTNESS_DELTA_THRESHOLD = 0.125f;
	
	
	
	private SampleProvider m_colorSensor;
	private Queue<Float> m_brigthnessBuffer;
	private long m_lastLineTime; 
	private boolean m_detectedLine; 
	private Object m_lock; 
	
	
	/**
	 * Constructor
	 */
	public LineDetector(EV3ColorSensor colorSensor)
	{
		m_colorSensor = colorSensor.getMode("Red");
		m_brigthnessBuffer = new LinkedList<Float>();
		m_detectedLine = false;
		m_lock = new Object();
	}
	
	/**
	 * Main loop to detect brightness 
	 */	
	public void run()
	{
		long updateStart;
		float[] collectedSample = new float [m_colorSensor.sampleSize()];
		float averageBrightness; 
		while(true)
		{
			updateStart = System.currentTimeMillis();
			
			//sample color sensor
			m_colorSensor.fetchSample(collectedSample, 0);
			
			//add new brightness value to the buffer
			m_brigthnessBuffer.add(collectedSample[0]);
			
			//remove values older sample values, if the buffer size is too large
			if (m_brigthnessBuffer.size() > BUFFER_SIZE)
			{
				m_brigthnessBuffer.remove();
			}
			
			//calculate the average brigthness of the values in the buffer
			averageBrightness = 0;
			for (Float sample : m_brigthnessBuffer)
			{
				averageBrightness += sample;
			}
			averageBrightness /= m_brigthnessBuffer.size();
			
			//IF there is a relative dark value compared to the past values
			//AND no other change has been detected in the last interval of time X
			//we consider it a line
			if (Math.abs(averageBrightness - collectedSample[0]) > BRIGHTNESS_DELTA_THRESHOLD &&
					updateStart - m_lastLineTime > MIN_TIME_BETWEEN_LINES )
			{
				m_lastLineTime = System.currentTimeMillis();
				synchronized(m_lock)
				{
					m_detectedLine = true;
				}
			}
			
			//60 ms after detecting the line, 
			//the robot has most likely moved past the line 
			//set the variable to no line detected 
			synchronized(m_lock)
			{
				if (m_detectedLine && updateStart - m_lastLineTime > MIN_TIME_BETWEEN_LINES+60)
				{
					m_detectedLine = false;
				}
			}
			
			Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart);	
		}
	}
	
	/**
	 * Gets the value of the boolean detectedLine
	 * @return true when a line is detected else false 
	 */
	public boolean detectedLine()
	{
		synchronized (m_lock)
		{
			if (m_detectedLine)
			{
				m_detectedLine = false;
				return true;
			}
			return false;
			 
		}	
	}
	
}
