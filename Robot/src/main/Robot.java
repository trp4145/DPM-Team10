package main;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

/**
 * Contains constants related to robot hardware.
 * 
 * @author Scott Sewell
 */
public class Robot
{
    // sensors
    public static final EV3UltrasonicSensor     ULTRASOUND_MAIN     = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
    public static final EV3ColorSensor          COLOR_LEFT          = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
    public static final EV3ColorSensor          COLOR_RIGHT         = new EV3ColorSensor(LocalEV3.get().getPort("S4"));

    // motors
    public static final EV3LargeRegulatedMotor  MOTOR_LEFT          = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
    public static final EV3LargeRegulatedMotor  MOTOR_RIGHT         = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
    
    // screen
    public static final TextLCD                 SCREEN              = LocalEV3.get().getTextLCD();
    
    // robot dimensions
    public static final float   RADIUS          = 18.0f;                // furthest point on robot from the center of rotation in cm
    public static final float   WHEEL_RADIUS    = 2.13f;                // radius of wheels in cm
    public static final float   WHEEL_TRACK     = 8.25f;                // distance between wheels in cm
    public static final Vector2 US_OFFSET       = new Vector2(0,0);     // the position offset of the ultrasound sensor from the robot center in cm
    public static final Vector2 CSL_OFFSET      = new Vector2(0,0);     // the position offset of the left color sensor from the robot center in cm
    public static final Vector2 CSR_OFFSET      = new Vector2(0,0);     // the position offset of the right color sensor from the robot center in cm
    
    // dynamic properties 
    public static final int     ACCELERATION    = 800;      // acceleration of the wheel motors
    public static final int     MOVE_SPEED      = 150;      // maximum wheel rotation speed while moving forwards in deg/sec
    public static final int     ROTATE_SPEED    = 100;      // maximum wheel rotation speed while turning in deg/sec
}
