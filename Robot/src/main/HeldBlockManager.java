package main;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class HeldBlockManager 
{   
    private final int MOTOR_CLAW_STALL_THRESHOLD = 4;
    private final int MOTOR_PULLEY_STALL_THRESHOLD = 7;

    private EV3LargeRegulatedMotor m_clawMotor;
    private EV3LargeRegulatedMotor m_pulleyMotor;
    private int m_blocksHeld;

    public HeldBlockManager() 
    {
        m_blocksHeld = 0;
        m_clawMotor = Robot.MOTOR_CLAW;
        m_pulleyMotor = Robot.MOTOR_PULLEY;


        m_clawMotor.setSpeed(Robot.CLAW_SPEED);
        m_pulleyMotor.setSpeed(Robot.PULLEY_SPEED);
        rotateUntilStall(m_pulleyMotor, true,MOTOR_PULLEY_STALL_THRESHOLD);

    }

    public void captureBlock() 
    {   

        //ASSUMING THAT THE CLAW WILL BE AT THE MAX POINT AT THE START OF THE PROGRAM
        //Open claw to release all blocks that the claw is holding
        m_clawMotor.rotateTo(0);
        m_clawMotor.waitComplete();

        //Lower claw completely
        
        m_pulleyMotor.rotateTo(0);
        m_pulleyMotor.waitComplete();

        //Close claw to capture the lowest block
        rotateUntilStall(m_clawMotor, true,MOTOR_CLAW_STALL_THRESHOLD);

        //Rise claw
        rotateUntilStall(m_pulleyMotor, true,MOTOR_PULLEY_STALL_THRESHOLD);



        m_blocksHeld++;




    }

    public void releaseBlock() 
    {
        //Lower claw completely
        m_pulleyMotor.rotateTo(0);
        m_pulleyMotor.waitComplete();
        //Open claw to release all blocks that the claw is holding
        m_clawMotor.rotateTo(0);
        m_clawMotor.waitComplete();

        //Rise claw
        rotateUntilStall(m_pulleyMotor, true,MOTOR_PULLEY_STALL_THRESHOLD);


        m_blocksHeld = 0;
    }

    public int getBlockCount() 
    {
        return m_blocksHeld;
    }

    public static void rotateUntilStall(EV3LargeRegulatedMotor motor, boolean forward, int threshold) {

        if(forward) {
            motor.forward();
        }else {
            motor.backward();
        }

        int currentTacho;
        int lastTacho = -999;

        boolean stall= false;
        while(!stall) {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }

            currentTacho = motor.getTachoCount();

            if(Math.abs(currentTacho - lastTacho) < threshold) {
                stall = true;
            }

            lastTacho = currentTacho;

        }

        motor.stop();

    }

}
