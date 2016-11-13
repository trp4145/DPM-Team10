package main;

/**
 * Contains the methods responsible for correcting the odometer
 * based on the inputs received by two light sensors 
 *  
 * @author Aimee Ascencio
 */
public class OdometryCorrection extends Thread
{
	//how much the corrected values override the original values
	private static final float CORRECTION_WEIGHT = 0.5f;

	private Odometer m_odometer;
	private LineDetector m_rightLineDetector;
	private LineDetector m_leftLineDetector;

	
	/**
	 * Constructor 
	 * @param odometer : the odometer object to which it will apply the correction
	 */
	public OdometryCorrection(Odometer odometer)
	{
		m_odometer = odometer;
		m_rightLineDetector = new LineDetector(Robot.COLOR_RIGHT);
		m_leftLineDetector = new LineDetector(Robot.COLOR_LEFT);		
	}
	
	/**
	 * Main loop 
	 */
	public void run()
	{
		//start the line detector threads 
		m_rightLineDetector.start();
		m_leftLineDetector.start();
		
		//position and orientation of the robot (odometer)
		Vector2 odometerPos;
		float odometerOrientation;
		
		//the position of the light sensors on the board
		Vector2 posCSR;
		Vector2 posCSL;
	
		
		while (true)
		{	
			//has to take less than 30 ms so that if both sensors detected something, it considers it
			if (m_rightLineDetector.detectedLine()) 
			{
				odometerPos = m_odometer.getPosition();
				odometerOrientation = m_odometer.getTheta();
				//calculate the position of the light sensor on the board
				posCSR = odometerPos.add(basisRobotToBoard(odometerOrientation, Robot.CSR_OFFSET));
				//correct odometer
				m_odometer.setPosition(correctedOdometer(odometerPos,posCSR));	
			}
			
			if (m_leftLineDetector.detectedLine())
			{
				odometerPos = m_odometer.getPosition();
				odometerOrientation = m_odometer.getTheta();
				//calculate the position of the light sensor on the board
				posCSL = odometerPos.add(basisRobotToBoard(odometerOrientation, Robot.CSL_OFFSET));
				//correct odometer
				m_odometer.setPosition(correctedOdometer(odometerPos,posCSL));
			}
		}
	}
	
	/**
	 * Calculate the lines that are closest to given coordinate 
	 * @return a vector 2 with the closest line on the 
	 */
	private Vector2 closestLine(Vector2 position)
	{
		float x = (float)Math.floor(position.getX()/Board.TILE_SIZE + 0.5);
		float y = (float)Math.floor(position.getY()/Board.TILE_SIZE + 0.5);
		return new Vector2(x,y).scale(Board.TILE_SIZE);
	}
	
	/**
	 * Change of basis:
	 * From the Robot's frame of reference
	 * To the board's frame of reference
	 * @param orientation of the robot 
	 * @param vec to be changed
	 * @return the vector in the board's frame of reference
	 */
	private Vector2 basisRobotToBoard(float orientation, Vector2 vec)
	{
		float x = vec.getX();
		float y = vec.getY();
		
		float sin = (float)Math.sin( Math.toRadians(orientation));
		float cos = (float)Math.cos( Math.toRadians(orientation));
		return new Vector2 (x*sin + y*cos,y*sin-x*cos);
	}
	
	/**
	 * Correct the component of the odometer (x or y) closest to a line 
	 * @param odometerPos
	 * @param posCS : position of the color sensor on the board's frame of reference
	 * @return the vector of the corrected position of the center of the robot.
	 */
	private Vector2 correctedOdometer (Vector2 odometerPos, Vector2 posCS)
	{
		Vector2 intersection = closestLine(posCS);
		Vector2 newPos = odometerPos.add(intersection.subtract(posCS).scale(CORRECTION_WEIGHT));
		if (Math.abs(posCS.getX() - intersection.getX()) > Math.abs(posCS.getY() - intersection.getY()))
		{
			//correct X
			return new Vector2(newPos.getX(),odometerPos.getY());
		}
		else
		{
			//correct Y
			return new Vector2(odometerPos.getX(),newPos.getY());
		}
	}
	

}
