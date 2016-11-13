package main;

import java.util.LinkedList;
import java.util.Queue;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * Contains the methods responsible for fetching and processing the samples
 * taken by the color sensor.
 * 
 * @author Aimee Ascencio
 */
public class LineDetector extends Thread
{
    // sensor polling period
    public static final int UPDATE_PERIOD = 30;
    // how many past brightness values are store
    private static final int BUFFER_SIZE = 6;
    // time that has to have passed to detect another line
    private static final int MIN_TIME_BETWEEN_LINES = 90;
    // the minimum change in brightness to consider it a line
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
     * Main loop to detect brightness.
     */
    public void run()
    {
        long updateStart;
        while (true)
        {
            updateStart = System.currentTimeMillis();

            // sample color sensor
            float[] collectedSample = new float[m_colorSensor.sampleSize()];
            m_colorSensor.fetchSample(collectedSample, 0);

            // add new brightness value to the buffer
            m_brigthnessBuffer.add(collectedSample[0]);

            // remove values older sample values if the buffer is above capacity
            if (m_brigthnessBuffer.size() > BUFFER_SIZE)
            {
                m_brigthnessBuffer.remove();
            }

            // calculate the average brightness of the values in the buffer
            float averageBrightness = 0;
            if (m_brigthnessBuffer.size() > 0)
            {
                for (float sample : m_brigthnessBuffer)
                {
                    averageBrightness += sample;
                }
                averageBrightness /= m_brigthnessBuffer.size();
            }

            // if a line was very recently seen, don't bother checking again for
            // a line.
            if (updateStart - m_lastLineTime > MIN_TIME_BETWEEN_LINES)
            {
                synchronized (m_lock)
                {
                    m_detectedLine = false;
                }

                // if there is a very different value compared to recent values
                // we've probably passed over a line.
                if (Math.abs(averageBrightness - collectedSample[0]) > BRIGHTNESS_DELTA_THRESHOLD)
                {
                    m_lastLineTime = System.currentTimeMillis();
                    synchronized (m_lock)
                    {
                        m_detectedLine = true;
                    }
                }
            }

            Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart);
        }
    }

    /**
     * @return true when a line was just detected.
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
