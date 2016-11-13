package main;

import lejos.hardware.Button;
import lejos.hardware.lcd.TextLCD;

/**
 * Handles display output and user interface.
 * 
 * @author Scott Sewell
 */
public class Display extends Thread
{
    // screen update period in ms
    private static final int UPDATE_PERIOD = 400;

    private TextLCD m_screen;
    private Odometer m_odometer;

    /**
     * Constructor.
     */
    public Display(Odometer odometer)
    {
        m_screen = Robot.SCREEN;
        m_odometer = odometer;
    }

    /**
     * Main display loop that runs once the thread is started.
     */
    public void run()
    {
        long updateStart;

        while (true)
        {
            updateStart = System.currentTimeMillis();

            // clear display
            m_screen.clear();

            // display robot transform
            Vector2 pos = m_odometer.getPosition();
            if (pos != null)
			{
			    m_screen.drawString(
	                    String.format("%.2f", pos.getX()) + " " +
                        String.format("%.2f", pos.getY()) + " " +
	                    String.format("%.1f", m_odometer.getTheta()),
	                    0, 0);
			}

			// if finished before the next update should occur, wait the remaining time
			Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart);
		}
	}

    /**
     * Prints out a single string to the screen.
     * 
     * @param str
     *            the string to display.
     */
    public void printString(String str)
    {
        m_screen.clear();
        m_screen.drawString(str, 0, 0);
    }

    /**
     * Prints out a menu for selecting between two options and returns once
     * there is a response.
     * 
     * @param optionLeft
     *            the text for the left option.
     * @param optionRight
     *            the text for the right option.
     * @return the id for the button pressed.
     */
    public int getMenuResponse(String optionLeft, String optionRight)
    {
        // clear the screen and display the two options
        m_screen.clear();
        m_screen.drawString("left:  " + optionLeft, 0, 0);
        m_screen.drawString("right: " + optionRight, 0, 1);

        // return the selected option
        return Button.waitForAnyPress();
    }
}