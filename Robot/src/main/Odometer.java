package main;

/**
 * Keeps track of the robot's assumed position by using the wheel movements.
 * 
 * @author Scott Sewell
 */
public class Odometer extends Thread
{
    // odometer update period in ms
    private static final int UPDATE_PERIOD = 25;

    // robot transform
    private Vector2 m_position; // position in centimeters
    private float m_theta; // rotation from x-axis counter-clockwise in degrees

    // lock object for mutual exclusion
    private Object m_lock;

    /**
     * Constructor.
     */
    public Odometer()
    {
        m_position = Vector2.zero();
        m_lock = new Object();
    }

    /**
     * Main loop run periodically to update the assumed robot transform using
     * the motor tachometers.
     */
    public void run()
    {
        long updateStart;

        // initialize tachometers
        int lastTachoL, lastTachoR;
        Robot.MOTOR_LEFT.resetTachoCount();
        Robot.MOTOR_RIGHT.resetTachoCount();
        lastTachoL = Robot.MOTOR_LEFT.getTachoCount();
        lastTachoR = Robot.MOTOR_RIGHT.getTachoCount();

        while (true)
        {
            updateStart = System.currentTimeMillis();

            // compute delta in forward direction and angle since last tick
            int tachoL = Robot.MOTOR_LEFT.getTachoCount();
            int tachoR = Robot.MOTOR_RIGHT.getTachoCount();
            double distL = Math.PI * Robot.WHEEL_RADIUS * (tachoL - lastTachoL) / 180;
            double distR = Math.PI * Robot.WHEEL_RADIUS * (tachoR - lastTachoR) / 180;
            lastTachoL = tachoL;
            lastTachoR = tachoR;
            float deltaD = (float) (0.5 * (distL + distR));

            // computes new transform from deltas
            // only manipulate transform variables here
            synchronized (m_lock)
            {
                // using small angle approximation
                m_theta += Math.toDegrees((distR - distL) / Robot.WHEEL_TRACK);
                // bring into [0,360] range
                m_theta = (m_theta + 360) % 360;
                m_position.add(new Vector2(deltaD, 0).rotate(m_theta));
            }

            // if finished before the next update should occur, wait the
            // remaining time
            Utils.sleepToNextPeroid(UPDATE_PERIOD, updateStart);
        }
    }

    /**
     * Transforms a vector from the robot's local space to world space.
     * 
     * @param v
     *            a local space vector.
     * @return a new vector in world space.
     */
    public Vector2 toWorldSpace(Vector2 v)
    {
        synchronized (m_lock)
        {
            return new Vector2(v).rotate(m_theta).add(m_position);
        }
    }

    /**
     * Transforms a vector from the world space to robot's local space.
     * 
     * @param v
     *            a world space vector.
     * @return a new vector in local space.
     */
    public Vector2 toLocalSpace(Vector2 v)
    {
        synchronized (m_lock)
        {
            return new Vector2(v).subtract(m_position).rotate(-m_theta);
        }
    }

    /**
     * @return a new vector with the robot's position in cm.
     */
    public Vector2 getPosition()
    {
        synchronized (m_lock)
        {
            return new Vector2(m_position);
        }
    }

    /**
     * @return the robot's orientation in degrees.
     */
    public float getTheta()
    {
        synchronized (m_lock)
        {
            return m_theta;
        }
    }

    /**
     * Sets the robot's position.
     * 
     * @param v
     *            new position in cm.
     */
    public void setPosition(Vector2 v)
    {
        synchronized (m_lock)
        {
            m_position = new Vector2(v);
        }
    }

    /**
     * Sets the robot's orientation.
     * 
     * @param theta
     *            new orientation in degrees.
     */
    public void setTheta(float theta)
    {
        synchronized (m_lock)
        {
            m_theta = (theta + 360) % 360;
        }
    }
}