package main;

import java.util.LinkedList;

/**
 * Contains methods for computing linear regression and approximating an 
 * angle based on a set of points
 * 
 * @author Aimee Ascencio
 *
 */
public class LinearRegression
{

	private LinkedList<Vector2> m_listPoints;
	
	/**
	 * Constructor
	 */
	public LinearRegression()
	{
		m_listPoints = new LinkedList<Vector2>();

	}
	
	/**
	 * Add a new point to the set 
	 * @param point
	 * 
	 */
	public void addPoint(Vector2 point)
	{
		m_listPoints.add(point);
	}
	
	/**
	 * @return the first point in the list
	 */
	public Vector2 getFirst()
	{
		return m_listPoints.peek();
	}
	/**
	 * @return the last point in the list
	 */
	public Vector2 getLast()
	{
		return m_listPoints.peekLast();
	}
	
	/**
	 * @return the size of the set 
	 */
	public int sampleSize()
	{
		return m_listPoints.size();
	}
	
	/**
	 * Delete all the values stored in the set
	 */
	public void clearList()
	{
		m_listPoints = new LinkedList<Vector2>();
	}
	
	/**
	 * @return the linear regression approximation of slope based on the set
	 */
	public float slope(){
		int n = m_listPoints.size();
		float s = n*sumXY() - sumX()*sumY();
		s /= (n*sumX2()- sumX()*sumX());
		return s;	
	}
		
	//Methods required for linear regression 
	/**
	 * @return the sum of the X- components of the point in the set 
	 */
	private float sumX()
	{
		float sum = 0; 
		for (Vector2 v: m_listPoints)
		{
			sum += v.getX();
		}
		return sum;
	}
	/**
	 * @return the sum of the Y-components of the point in the set 
	 */
	private float sumY()
	{
		float sum = 0; 
		for (Vector2 v: m_listPoints)
		{
			sum += v.getY();
		}
		return sum;
	}
	/**
	 * @return the sum of the X-components squared for each point 
	 * in the set
	 */
	private float sumX2()
	{
		float sum = 0; 
		for (Vector2 v: m_listPoints)
		{
			sum += v.getX()*v.getX();
		}
		return sum;
	}
	/**
	 * @return the sum of the multiplication of X and Y 
	 * components of the points in the set
	 * 
	 */
	private float sumXY()
	{
		float sum = 0; 
		for (Vector2 v: m_listPoints)
		{
			sum += v.getY()*v.getX();
		}
		return sum;
	} 
}
