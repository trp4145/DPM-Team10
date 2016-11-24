package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;

/**
 * The main class that manages most of the decision making aspects of the robot.
 *
 * @author Scott Sewell
 */
public class Main
{

    // the duration of the match in seconds
    private static final long MATCH_DURATION = 5 * 60;
    // how far the robot sees while localizing in cm
    private static final float LOCALIZATION_DISTANCE = 45;
    // number of blocks the the robot will try to stack before dropping them off
    private static final int BLOCK_STACK_SIZE = 1;
    // the distance in cm ahead of the robot in which obstacles are seen 
    private static final float OBSTACLE_DISTANCE = 6;
    // the distance in cm the robot moves to either side of an obstacle when trying to avoid it
    private static final float AVOID_DISTANCE = 30;
    
    private StartParameters m_startParams;
    private Board m_board;
    private Odometer m_odometer;
    private OdometryCorrection m_odoCorrection;
    private UltrasonicPoller m_usMain;
    private UltrasonicPoller m_usUpper;
    private Driver m_driver;
    private HeldBlockManager m_blockManager;
    private Display m_display;
    
    // search algorithm
    private float m_usPreviousDistance;
    private boolean m_usHasStartedCollectingData = false;
    private static final float OFFSET = 8; // to give enough space for the robot to turn around
    private float m_discontinuityStartAngle;
    private float m_discontinuityEndAngle;
    private boolean  m_discontinuitySpotted = false;


    private long m_startTime;
    
    /**
     * Launches the main program.
     */
    public static void main(String[] args)
    {
        Main main = new Main();
        main.launch();
    }

    /**
     * Gets starting info, runs threads, and begins the main logic loop.
     */
    private void launch()
    {
        // initialize
        m_usMain = new UltrasonicPoller(Robot.ULTRASOUND_MAIN);
        m_usUpper = new UltrasonicPoller(Robot.ULTRASOUND_UPPER);
        m_odometer = new Odometer();
        m_odoCorrection = new OdometryCorrection(m_odometer);
        m_driver = new Driver(m_odometer);
        m_blockManager = new HeldBlockManager();
        m_display = new Display(m_odometer);

        // choose whether to use wifi or test parameters.
        m_startParams = new StartParameters();
        if (m_display.getMenuResponse("Use Wifi", "Use Test Data") == Button.ID_LEFT)
        {
            // wait to progress until start information is received via wifi
            while (!m_startParams.hasRecievedData())
            {
                m_startParams.getWifiData();
                Utils.sleep(100);
            }
        }
        else
        {
        	m_startParams.useTestData();
        }
        
        // record the starting time
        m_startTime = System.currentTimeMillis();

        // get the board
        m_board = m_startParams.getBoard();

        // start threads
        m_usMain.start();
        m_usUpper.start();
        m_odometer.start();
        m_display.start();
                
        // localize
        localize(true);
        
        // start odometry correction now that localization is done
        m_odoCorrection.start();
        
        //initialize the claw 
        m_blockManager.initializeClaw();
        
        // temp block search
        m_driver.turn(90, Robot.ROTATE_SPEED / 3, false);
        while (m_usMain.getFilteredDistance() > 80) {}
        Utils.sleep(1625);
        m_driver.stop();
        float blockDistance = m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX();   

        float checkDistance = 5f + Robot.RADIUS;
        m_driver.goForward(blockDistance - checkDistance, true);
        m_driver.turn(-90, Robot.ROTATE_SPEED, true);
        boolean isBlueBlock = m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY() > (checkDistance + 10);
        m_driver.turn(90, Robot.ROTATE_SPEED, true);
        if (isBlueBlock)
        {
            Sound.buzz();
            m_driver.goForward(checkDistance - 8, true);
            m_blockManager.captureBlock();
        }
        else
        {
            Sound.twoBeeps();
        }
        Utils.sleep(500);
        
        m_driver.travelTo(m_board.getBuildZoneCenter(), true);

        if (m_blockManager.getBlockCount() > 0)
        {
            m_blockManager.releaseBlock();
//        }

        Utils.sleep(500);

        // drop off any held blocks once we have enough
        if (m_blockManager.getBlockCount() >= BLOCK_STACK_SIZE)
        {
            // move to the appropriate zone
            moveWhileAvoiding(m_startParams.isBuilder() ? m_board.getBuildZoneCenter() : m_board.getDumpZoneCenter());
            m_blockManager.releaseBlock();
        }
        
        // we must move back to the start corner before the end of the match
        moveWhileAvoiding(m_board.getStartPos());
		
        // Uncomment for search algo
        // The algo currently assumes it is at 0 degrees, at position (0,0)
        // It will drive towards the block it sees.
        
        //m_odometer.setPosition(Vector2.zero());
        //m_odometer.setTheta(0);
        //searchForBlocks();
        
        // finish
        System.exit(0);
    }

    /**
     * Attempts to set the odometer's angle to match the board's coordinates by
     * rotating near a board corner. Uses the ultrasonic sensor to determine
     * angles at which the walls are seen.
     * 
     * @param moveToOrigin 
     *            if true moves the robot to the line intersection nearest to
     *            the corner after calculating its position.
     */
    private void localize(boolean moveToOrigin)
    {
        m_odometer.setTheta(0);
        m_odometer.setPosition(Vector2.zero());

        // start the robot turning one revolution and record the seen distances
        // along with the angles they were captured at
        List<Float> orientations = new ArrayList<Float>();
        List<Float> distances = new ArrayList<Float>();
        m_driver.turn(360, Robot.LOCALIZATION_SPEED, false);
        while (m_driver.isTravelling())
        {
            orientations.add(Utils.normalizeAngle(m_odometer.getTheta() + 90));
            distances.add(m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY());
            Utils.sleep(UltrasonicPoller.UPDATE_PERIOD * 2);
        }
        
        // find all the angles that correspond to when the distance rises above
        // LOCALIZATION_DISTANCE and when it lowers below LOCALIZATION_DISTANCE
        List<Float> risingAngles = new ArrayList<Float>();
        List<Float> fallingAngles = new ArrayList<Float>();
        for (int i = 0; i < orientations.size(); i++)
        {
            float dist = distances.get(i);
            float nextDist = distances.get((i + 1) % distances.size());
            
            if (dist < LOCALIZATION_DISTANCE && nextDist > LOCALIZATION_DISTANCE)
            {
                risingAngles.add(orientations.get(i));
            }
            
            if (dist > LOCALIZATION_DISTANCE && nextDist < LOCALIZATION_DISTANCE)
            {
                fallingAngles.add(orientations.get(i));
            }
        }

        // determine which falling and rising edge angles correspond to the wall
        // as to filter out any blocks new the start point. We know that rising
        // falling angle pair with the largest angle between them is the pair
        // that belong to the wall.
        float largestBearing = Float.MIN_VALUE;
        float angle = 0;
        for (float risingAng : risingAngles)
        {
            for (float fallingAng : fallingAngles)
            {
                float bearing = Math.abs(Utils.toBearing(risingAng - fallingAng));
                if (bearing > largestBearing)
                {
                    largestBearing = bearing;
                    angle = 315 - (bearing / 2) - risingAng;
                }
            }
        }
        
        // account for the starting corner the robot is in
        float cornerAngOffset = 90 * m_startParams.getStartCorner();

        // set odometer angle accounting for start corner
        m_odometer.setTheta(angle + cornerAngOffset);
        
        Vector2 startPos = new Vector2(
                distances.get(Utils.closestIndex(Utils.normalizeAngle(180 - angle), orientations)) - Board.TILE_SIZE,
                Board.TILE_SIZE - distances.get(Utils.closestIndex(Utils.normalizeAngle(90 - angle), orientations))
                );
        
        m_odometer.setPosition(m_board.getStartPos().add(startPos.rotate(cornerAngOffset)));
        
        Sound.beepSequenceUp();
        
        // if applicable, move to the nearest line intersection
        if (moveToOrigin)
        {
            m_driver.travelTo(Board.getNearestIntersection(m_odometer.getPosition()), true);
            m_driver.turnTo(cornerAngOffset - 90, true);
        }
    }

    /**
     * Moves the robot to a position while avoiding obstacles on the way.
     * 
     * @param position
     *            the destination point.
     */
    private void moveWhileAvoiding(Vector2 position)
    {
        m_driver.setDestination(position);
        while (!m_driver.isNearDestination())
        {
            m_driver.travelTo(position, false);
            while ( m_driver.isTravelling() &&
                    m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX() > Robot.RADIUS + OBSTACLE_DISTANCE
                    ) {}
            if (m_driver.isTravelling() && Vector2.distance(position, m_odometer.getPosition()) > OBSTACLE_DISTANCE)
            {
                m_driver.stop();
                avoidObstacle();
            }
        }
    }

    /**
     * Moves the robot some distance to the left or right, depending on which
     * direction is clearer of other obstacles.
     */
    private void avoidObstacle()
    {
        // check if there is no obstacle nearby on the left using the left ultrasound sensor
        // also check if the avoidance detour will cross into a invalid position
        Vector2 leftAvoidWaypoint = m_odometer.toWorldSpace(new Vector2(0, AVOID_DISTANCE));
        Vector2 rightAvoidWaypoint = m_odometer.toWorldSpace(new Vector2(0, -AVOID_DISTANCE));

        float leftDistance = m_usUpper.getFilteredDistance() + Robot.US_UPPER_OFFSET.getY();
        if (leftDistance > Robot.RADIUS + AVOID_DISTANCE && checkValidity(leftAvoidWaypoint))
        {
            m_driver.travelTo(leftAvoidWaypoint, true);
        }
        else
        {
            // if the left way around is invalid, try looking right at check if it is clear
            m_driver.turn(-90, Robot.ROTATE_SPEED, true);

            float rightDistance = m_usMain.getFilteredDistance() + Robot.US_MAIN_OFFSET.getX();
            if (rightDistance > Robot.RADIUS + AVOID_DISTANCE && checkValidity(rightAvoidWaypoint))
            {
                m_driver.goForward(AVOID_DISTANCE, true);
            }
            else if (leftDistance > rightDistance) // if both invalid, pick the better bet
            {
                m_driver.turn(180, Robot.ROTATE_SPEED, true);
                m_driver.goForward(AVOID_DISTANCE, true);
            }
        }
    }

    /**
     * Determines if a point on the board in a valid place for the robot to go.
     * 
     * @param destination
     *            the world space position to check.
     * @return true if the destination point doesn't overlap any invalid
     *         regions.
     */
    private boolean checkValidity(Vector2 destination)
    {
        return m_board.inBounds(destination) && !(m_startParams.isBuilder() ? m_board.inDumpZone(destination) : m_board.inBuildZone(destination));
    }

    /**
     * Gets the time remaining in the match.
     * 
     * @return the time in seconds.
     */
    private float getTimeRemaining()
    {
        return (System.currentTimeMillis() - m_startTime) / 1000f;
    }
    
    /**
     * Searches for blocks. Sweeps from current angle to +90 degrees,
     * collects data into a Map then analyzes that data and moves the robot
     * accordingly.
     */
    private void searchForBlocks()
    {
        int SCANNING_SPEED = 50;
        float currentAngle;
        float lastAngle;
        float distance;
        Map<Float, Float> angleDistanceMap = new HashMap<Float, Float>();   
        
        // store current angle
        currentAngle = m_odometer.getTheta();
        
        // store angle where data gathering should stop
        lastAngle = currentAngle + 90;
        
        // turn the robot 90 degrees, ccw
        m_driver.turn(90, SCANNING_SPEED, false);
        
        // while the current angle hasn't reached +90, dont stop 
        while(currentAngle < lastAngle)
        {
            distance = getDistanceMain();
            currentAngle = m_odometer.getTheta();
            angleDistanceMap.put(currentAngle, distance);
        }
        
        
        
        // analyze data
        analyze(angleDistanceMap);

    }
    
    /**
     * This method is called from the searchForBlocks method to 
     * analyze the data it collected. 
     * 
     * @param data: A Map containing all the measured angles and their
     * corresponding distances.
     */
    private void analyze(Map<Float, Float> data)
    {
        float currentAngle;
        float currentDistance;
        float previousAngle;
        float previousDistance;
        float minDistanceGap = 15; // gap > minGap to be considered a discontinuity
        float absGap;
        int discontinuities = 0; // number of discontinuities
        Map<Float, Float> sortedData = new TreeMap<Float, Float>(data);
        Iterator<Map.Entry<Float, Float>> entries;
        Map.Entry<Float, Float> entry;
        Map<Float, Float> discontinuitiesMap = new HashMap<Float, Float>();
         
        
        // DEBUG
        // writes to file for debugging purposes
        writeToFile(sortedData, "sorted.txt");
        
        // sanitized data
        // means no incorrect discontinuities are there
        // new Map passed by reference
        Map<Float, Float> sanitizedData =  sanitize(sortedData);
        
        writeToFile(sanitizedData, "sanitized.txt");
             
        // set the iterator to the sanitized data
        entries = sanitizedData.entrySet().iterator();
        
        
        // set previousAngle and previousDistance to first data point
        entry = entries.next();    
        previousAngle = entry.getKey();
        previousDistance = entry.getValue(); 
        
        // need to sort the data in the hasmap. 
        
        // check for discontinuities
        while(entries.hasNext())
        {
            entry = entries.next();
            currentAngle = entry.getKey();
            currentDistance = entry.getValue();  
            
            absGap = Math.abs(currentDistance - previousDistance);
            
            // if there is a discontinuity
            // increase the discontinuity counter
            // and store the data point on another Map
            if(absGap > minDistanceGap)
            {
                discontinuities += 1;
                discontinuitiesMap.put(currentAngle, currentDistance);
            }
            
            // updating for next iteration
            previousAngle = currentAngle;
            previousDistance = currentDistance;
        }
       
        
        
        Map<Float, Float> sortedDiscontinuities = new TreeMap<Float, Float>(discontinuitiesMap);
        
        if(discontinuities == 0)
        {
            Sound.buzz();
            noDiscontinuities(sortedData);
        }
        else if(discontinuities == 1)
        {    
            Sound.beep();
            writeToFile(sortedDiscontinuities, "disc1.txt");
            oneDiscontinuity(sortedData, sortedDiscontinuities);
        }
        else
        {
            Sound.twoBeeps();
            writeToFile(sortedDiscontinuities, "disc2.txt");
            manyDiscontinuities(sortedDiscontinuities);
        }
    }
    
    /**
     * This method removes any false positives from the data.
     * 
     * @param rawData
     * @return cleaned up Map containing the angles and the distances
     * recorded by the ultrasonic sensor. 
     */
    private Map<Float, Float> sanitize(Map<Float, Float> rawData)
    {
        float currentAngle;
        float currentDistance;
        float previousAngle;
        float previousDistance;
        float angleGap;
        float minAngleGap = 8;
        float absDistanceGap;
        float minDistanceGap = 0;
        Iterator<Map.Entry<Float, Float>> entries;
        Map.Entry<Float, Float> entry;
        
        // in case we can't modify a parameter
        // creating new reference
        //Map<Float, Float> localRawData = new TreeMap<Float, Float>(rawData);
        
        // setting the discontinuities         
        entries = rawData.entrySet().iterator();
        
        // set previousAngle and previousDistance to first data point
        entry = entries.next();    
        previousAngle = entry.getKey();   
        previousDistance = entry.getValue();
        
        // hardcoding 55 for the first value
        rawData.put(entry.getKey(), 55f);
        previousDistance = 55f;
        

        // set m_discontinuityStartAngle to first angle
        m_discontinuityStartAngle = previousAngle;
        
        
        // check for discontinuities
//        loop:
        while(entries.hasNext())
        {
            entry = entries.next();
            currentAngle = entry.getKey();
            currentDistance = entry.getValue();  
            
            absDistanceGap = Math.abs(currentDistance - previousDistance);
            
            // some optimization
//            if(Math.abs(currentAngle - m_discontinuityStartAngle) > 8)
//            {
//                m_discontinuityStartAngle = currentAngle;
//                m_discontinuitySpotted = false;
//                continue loop;
//            }
            
            if(absDistanceGap > minDistanceGap)
            {
                // so here we know the sensor detected something
                // let's see what angle does it span
                // if it's less than 8 degress, we change the data
                // meaning at currentAngle, change distance
                
                // if 55 prev and now 30
                // the discontinuity was detected at THIS angle
                // so set m_start to this angle, ie currentAngle
                if(!m_discontinuitySpotted) //if discon has been spotted
                {
                    m_discontinuityStartAngle = currentAngle; 
                    m_discontinuitySpotted = true;

                }
                else
                {
                    m_discontinuityEndAngle = currentAngle;
                    m_discontinuitySpotted = false;
                    
                    angleGap = Math.abs(m_discontinuityEndAngle - m_discontinuityStartAngle);
                    
                    writeDebug("Angle gap is: " + angleGap);
                    
                    if(angleGap < minAngleGap) //if we have a discontinuity lower than this, delete
                    {
                        writeDebug("will delete from: " + m_discontinuityStartAngle + " to: " + m_discontinuityEndAngle);
                        
                        // modifiying this reference
                        filterOutFalsePositives(rawData, m_discontinuityStartAngle, m_discontinuityEndAngle);
                    }
                }        
            }
            
            previousAngle = currentAngle;
            previousDistance = currentDistance;
        }
        
        return rawData;
        
    }
    
    /**
     * Method that iterates through the data from one angle to the
     * other to remove any discontinuities in between. 
     * 
     * @param rawData
     * @param discontinuityStartAngle
     * @param discontinuityEndAngle
     */
    private void filterOutFalsePositives(Map<Float, Float> rawData, float discontinuityStartAngle, float discontinuityEndAngle)
    {
        float correctedDistance = 55f;
        float currentAngle;
        float currentDistance;
        
        for(Map.Entry<Float, Float> entry : rawData.entrySet())
        {
            currentAngle = entry.getKey();
            currentDistance = entry.getValue();
            
            // do not include endangle as it is the other discontinuity
            if((currentAngle >= discontinuityStartAngle) && (currentAngle < discontinuityEndAngle))
            {
                rawData.put(currentAngle, correctedDistance);
            }         
        }
    }
    
    
    /**
     * This method is to be called for analyzing a dataset containing
     * no discontinuities.
     * 
     * @param data : A Map containing all the angles and the distances
     * for each of them. 
     */
    private void noDiscontinuities(Map<Float, Float> data)
    {
        float[] meanAngleDistance = getMeanAngleDistance(data);
        
        float meanAngle = meanAngleDistance[0];
        float meanDistance = meanAngleDistance[1];

        Vector2 destination = Vector2.fromPolar(meanAngle, meanDistance - OFFSET);
        
        // move robot to mean distance, at mean angle
        m_driver.travelTo(destination, true); 
        while (m_driver.isTravelling()) {}
    }
    
    /**
     * This method is called when only one discontinuity is detected
     * by the main ultrasonic sensor. 
     * 
     * @param data : A map containing all the angles measured and their
     * corresponding distances.
     * @param discontinuitiesMap : A Map containing the only discontinuity
     * found. 
     */
    private void oneDiscontinuity(Map<Float, Float> data, Map<Float, Float> discontinuitiesMap)
    {
        
        // partitioning data in 2, on either side of the single discontinuity
        float dividingAngle = discontinuitiesMap.keySet().iterator().next();
        Map<Float, Float> partitionOne = new HashMap<Float, Float>();
        Map<Float, Float> partitionTwo = new HashMap<Float, Float>();
        
        for(Map.Entry<Float, Float> entry : data.entrySet())
        {
            if(entry.getKey() < dividingAngle)
            {
                partitionOne.put(entry.getKey(), entry.getValue());
            }
            else
            {
                partitionTwo.put(entry.getKey(), entry.getValue());
            }
        }
        
        // get the mean angle and mean distance of each partitions
        // first eleement of the array is the angle, second is distance
        float[] meansPartitionOne = getMeanAngleDistance(partitionOne);
        float[] meansPartitionTwo = getMeanAngleDistance(partitionTwo);
       
        float meanAnglePartitionOne = meansPartitionOne[0];
        float meanDistancePartitionOne = meansPartitionOne[1];
        float meanAnglePartitionTwo = meansPartitionTwo[0];
        float meanDistancePartitionTwo = meansPartitionTwo[1];
        
        Vector2 destination;
        
        // selecting the partition with the smaller average for the
        // distance measured
        if(meanDistancePartitionOne < meanDistancePartitionTwo)
        {
            destination = Vector2.fromPolar(meanAnglePartitionOne, meanDistancePartitionOne - OFFSET);
        }
        else
        {
            destination = Vector2.fromPolar(meanAnglePartitionTwo, meanDistancePartitionTwo - OFFSET);
        }
        
        m_driver.travelTo(destination, true);
        while (m_driver.isTravelling()) {}
    }
    
    /**
     * This method is called when the main ultrasonic sensor has
     * detected more than one discontinuity. The robot will move towards
     * the first blue block it detects.
     * 
     * @param data : A Map that includes all the angles and the distances
     * of the detected discontinuities.
     * 
     */
    private void manyDiscontinuities(Map<Float, Float> data)
    {
        float currentAngle;
        float currentDistance;
        float previousAngle;
        float previousDistance;
        Iterator<Map.Entry<Float, Float>> entries;
        Map.Entry<Float, Float> entry;
        float width;
        ArrayList<Float> objectWidths = new ArrayList<Float>(); //delete later if not used
        float destinationAngle;
        float destinationDistance;

        // An Iterator to iterate through all the discontinuities in the Map          
        entries = data.entrySet().iterator();
        
        // initialize the previous variables with first element on the Map
        entry = entries.next();    
        previousAngle = entry.getKey();
        previousDistance = entry.getValue(); 
        
        
        // do while to go through the loop once in case there are only
        // 2 discontinuities in the Map
        do
        {
            
            entry = entries.next();
            currentAngle = entry.getKey();
            currentDistance = entry.getValue(); 
            

            //calculating widths and adding to the arraylist
            width = calculateWidth(previousAngle, previousDistance, currentAngle, currentDistance);
            objectWidths.add(width);
            
            // TODO need a better width calculator
            // mb get some data and then calculate what could be considered
            // a block and what is more than than.
            // for example, if width exceeds 100, then assume it's two blocks and do something else
            // this could also be just checking the angle instead of the width
            if(width < 120)
            {   
                // trying to aim for the middle of the block
                destinationAngle = (currentAngle + previousAngle)/2.0f;
                
                // an offset might be needed here to not land on top of the block
                destinationDistance = (currentDistance + previousDistance)/2.0f; 
                
                m_driver.travelTo(Vector2.fromPolar(destinationAngle, destinationDistance - OFFSET), true);
                while (m_driver.isTravelling()) {}
                break;
               
            }
            
            // updating the previous variables for next iteration
            previousAngle = currentAngle;
            previousDistance = currentDistance;
        }
        while(entries.hasNext());

        
    }
    
    /**
     * This method calculates the width of a perceived object.
     * 
     * @param angleA : angle of first edge of the perceived object
     * @param distanceA : distance to the first edge of the perceived object
     * @param angleB : angle of the second edge of the perceived object
     * @param distanceB : distance to the second edge of the perceived object
     * @return : the calculated width
     */
    private float calculateWidth(float angleA, float distanceA, float angleB, float distanceB)
    {
        float width;
        float meanDistance;
        float arcSize;
        
        meanDistance = (distanceA + distanceB)/2.0f;
        arcSize = angleB - angleA;
        
        width = (float) (meanDistance * Math.sqrt(2*(1-Math.cos(arcSize))));
        
        return width;
    }
    
    /**
     * This method calculates the average of all angles and all distances
     * passed to it in a Map. 
     * 
     * @param data : A Map containing angles as keys and distances as values.
     * @return : The average of all the angles and all the distances that were
     * on the Map in an array of size 2. The first element is the angle, distance is the 
     * second. 
     */
    private float[] getMeanAngleDistance(Map<Float, Float> data)
    {
        float meanDistance= 0;
        float meanAngle = 0;
        float dataSampleSize = data.size();
    
        for(Map.Entry<Float, Float> entry : data.entrySet())
        {
            meanAngle += entry.getKey();
            meanDistance += entry.getValue();         
        }
        
        meanDistance = meanDistance/dataSampleSize;
        meanAngle = meanAngle/dataSampleSize;
        
        float[] angleDistanceArray = {meanAngle, meanDistance};
        
        return angleDistanceArray;    
    }
 
    /**
     * Wrapper to get the main ultrasonic sensor's instantaneous reading.
     * 
     * NOTE: if the difference between the first value and the second is 
     * higher than 8, then keep the previous value. otherwise go with the 
     * newest value.
     * 
     * @return : The distance detected by the ultrasonic sensor. 
     */
    private float getDistanceMain()
    {   
        float distance = m_usMain.getLastDistance();
        float gap;
        
        if(distance > 55)
        {
            distance = 55;
        }
        
        // if it's the first time running this method
        // intialize the previous distance as the current distance
        if(!m_usHasStartedCollectingData)
        {
            m_usHasStartedCollectingData = true; 
            m_usPreviousDistance = distance;
            
            return distance;
        }
        else
        {
            // if the difference between the previous value 
            // and the current value is less than 7f, keep the
            // previous value
            gap = Math.abs(distance - m_usPreviousDistance);
   
            if(gap < 15)
            {
                distance = m_usPreviousDistance;
            }
            
            // if the difference between the previous and the current
            // value is greater, then keep distance as it is, but 
            // update the previous distance;
            else
            {
                m_usPreviousDistance = distance;
            }
            
            return distance; 
        }
        
    } 
    
    /**
     * This method writes to a file called the main ultrasonic sensor's 
     * readings.
     * 
     * @param data : Data gathered by the main ultrasonic sensor.
     */
    private void writeToFile(Map<Float, Float> data, String filename)
    {
        float angle;
        float distance;
        int index = 0;
        
        //Print output on to a txt file 
        File file = new File(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename)))
        {
            if (!file.exists()){
                file.createNewFile();
            }
            for(Map.Entry<Float, Float> entry : data.entrySet())
            {
                angle = entry.getKey();
                distance = entry.getValue();  
                writer.write(index + "\t" + String.format("%.1f", angle) + "\t" + String.format("%.1f", distance) + "\n");
                index += 1;
            }            

        }catch (IOException e){
            System.out.println(e.getMessage());
        }  
    }
    
    

    /**
     * Method for writing to a debug file 
     * debug metrics. Appends to same file.
     * 
     * @param data Any string.
     */
    private static void writeDebug(String data)
    {
        //Print output on to a txt file 
        File file = new File("Debug.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Debug.txt", true)))
        {
            if (!file.exists())
            {
                file.createNewFile();
            }
            
            writer.write(data + "\n");   
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }  
    }
}