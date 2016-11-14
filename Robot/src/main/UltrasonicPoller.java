package main;

import java.util.LinkedList;
import java.util.Queue;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * Contains the methods responsible for fetching and processing the samples
 * taken by the ultrasonic sensor.
 * 
 * @author Aimee Ascencio
 */
public class UltrasonicPoller extends Thread
{
    // period at which the sensor is polling in ms
    private static final int UPDATE_PERIOD = 25;
    // maximum distance that distance measured is clamped under in cm
    private static final int MAX_RANGE = 200;
    // how many past sensor values are stored
    private static final int BUFFER_SIZE = 10;

    private SampleProvider m_sensor;
    private Queue<Float> m_buffer;
    private float m_filteredDistance;
    private float m_lastDistance;

    // lock object for mutual exclusion
    private Object m_lock;

    /**
     * Constructor.
     */
    public UltrasonicPoller(EV3UltrasonicSensor sensor)
    {
        m_sensor = sensor.getMode("Distance");
        m_buffer = new LinkedList<Float>();
        m_lock = new Object();
    }

    /**
     * Continuously samples the ultrasonic sensor.
     */
    public void run()
    {
        long updateStart;
        while (true)
        {
            updateStart = System.currentTimeMillis();

            // get a new distance sample
            float[] usSample = new float[m_sensor.sampleSize()];
            m_sensor.fetchSample(usSample, 0);
            float newDistance = Math.min(usSample[0] * 100, MAX_RANGE);

            synchronized (m_lock)
            {
                m_lastDistance = newDistance;
            }

            // store the newest distance value in the buffer
            m_buffer.add(newDistance);

            // remove the oldest values from the buffer
            if (m_buffer.size() > BUFFER_SIZE)
            {
                m_buffer.remove();
            }

            // compute filtered distance
            float weightedDistance = 0;
            if (m_buffer.size() > 0)
            {
                for (float distance : m_buffer)
                {
                    weightedDistance += (1 / distance);
                }
                weightedDistance = m_buffer.size() / weightedDistance;
            }

            synchronized (m_lock)
            {
                m_filteredDistance = weightedDistance;
            }

            Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart);
        }
    }

    /**
     * Gets the filtered distance value. This value is a weighted average of the
     * last number of samples.
     * 
     * @return the filtered distance value from the sensor in cm.
     */
    public float getFilteredDistance()
    {
        synchronized (m_lock)
        {
            return m_filteredDistance;
        }
    }

    /**
     * @return the more recent distance value from the sensor in cm.
     */
    public float getLastDistance()
    {
        synchronized (m_lock)
        {
            return m_lastDistance;
        }
    }
}
