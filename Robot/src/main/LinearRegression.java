package main;

import java.util.LinkedList;

public class LinearRegression {
	
	private LinkedList<Vector2> m_listPoints;
	
	
	public LinearRegression(){
		m_listPoints = new LinkedList<Vector2>();
	}
	
	public void addPoint(Vector2 point){
		m_listPoints.add(point);
	}
	public int sampleSize(){
		return m_listPoints.size();
	}
	public void clearList(){
		m_listPoints = new LinkedList<Vector2>();
	}
	public float angle(){
		int quadrant = getQuadrant();
		float angle = (float)Math.toDegrees(Math.atan((double)slope()));
		
		switch (quadrant){
		case 1:
			//do nothing to angle;
			break;
		case 2:
			angle+=180;
			break; 
		case 3:
			angle+=180;
			break; 
		case 4: 
			angle+=360;
			break; 
		default: 
			//no angle can be calculated 
			angle = -1;
			System.out.println("no angle ");
		}
		return angle; 
	}
	
	//linear regression approx of slope 
	private float slope(){
		int n = m_listPoints.size();
		float s = n*sumXY() - sumX()*sumY();
		s /= (n*sumX2()- sumX()*sumX());
		return s;	
	}
	
	//Methods required for linear regression 
	private float sumX(){
		float sum = 0; 
		for (Vector2 v: m_listPoints){
			sum += v.getX();
		}
		return sum;
	}
	private float sumY(){
		float sum = 0; 
		for (Vector2 v: m_listPoints){
			sum += v.getY();
		}
		return sum;
	}
	private float sumX2(){
		float sum = 0; 
		for (Vector2 v: m_listPoints){
			sum += v.getX()*v.getX();
		}
		return sum;
	}
	private float sumXY(){
		float sum = 0; 
		for (Vector2 v: m_listPoints){
			sum += v.getY()*v.getX();
		}
		return sum;
	} 
	
	//returns the orientations of the slope 
	private int getQuadrant(){
		if (m_listPoints.size() < 2){
			return 0; // there are not enough values to compute this 
		}
		
		float direction = m_listPoints.peekLast().subtract(m_listPoints.peek()).angle();
		
		if (direction >= 0.f && direction < 90.0f){
			return 1;		
		}else if (direction >= 90.f && direction < 180.0f){
			return 2;
		}else if (direction >= 180.f && direction < 270.0f){
			return 3;
		}else {
			return 4; 			
		}
		
	}
	

}
