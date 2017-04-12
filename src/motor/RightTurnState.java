package motor;

//import motor.ForwardState.interupt_reminder;

import java.util.Timer;
import java.util.TimerTask;
//***************************
//Turn Method turns at a set duty cycle of 20% or <turnspeed> with each wheel turning just in 
//opposite directions. It returns after the increase is called and schedules an interrupt for 
//every 200 ms or <interuptTime>. It does this 15 times or 3 seconds and will stop
//unless a decrease command is received first. Adjust method is not implemented. Direction
//pins are a guess. Might need to change polarity depending on motor controller.
//For pins used see README
//***************************


public class RightTurnState extends IState{
	private Timer timer;
	private int interuptTime = 200; //Milli Seconds
	private int dutycycle;
	private int turnspeed = 15;
	private int turntimeout;
	private GPIOCreator GPIO = null;

	//Right Turn State constructor 
	//Default uses GPIO_01 and GPIO_24 for Wheel PWM and GPIO_4 for brakes
	RightTurnState(GPIOCreator GPIOInput){
		GPIO = GPIOInput;
		//Setting initial duty cycle to 0%
		GPIO.setPWMLeft(0);
		GPIO.setPWMRight(0);

		GPIO.setDutyCycleBase(0);
		turntimeout = 15; //Should be decrementing 1 every 200 milliseconds so turn will last for 3 seconds before stopping
		GPIO.setStopped(true);


}	


public Boolean increase() {
	// This is the turn method. It will turn for 3 seconds or until it gets a decrease command. Whichever comes first
	//returning with the interrupt similar to forwardState to help counting and check for stop command
	//Currently turns at a set 15% duty cycle
	
	GPIO.setMotorDirectionRight(0);
	GPIO.setMotorDirectionLeft(1);

	
	dutycycle = GPIO.getDutyCycle();

	if (GPIO.getStopped() == true && dutycycle < 1 && turntimeout == 15){
		System.out.println("Disenganging Brakes");
		GPIO.setBrakes(1);
		
		System.out.println("Turning to the Right");
		GPIO.setPWMRight(turnspeed);
		GPIO.setPWMLeft(turnspeed);

		createInterupt(interuptTime, true);
		turntimeout = turntimeout - 1;
		GPIO.setDutyCycleBase(turnspeed);
		GPIO.setStopped(false);

		return true;
	}
	else if (turntimeout > 1){
		createInterupt(interuptTime, true);
		turntimeout = turntimeout - 1;
		return true;
	}
	else if (turntimeout <= 1){
		System.out.println("Turn timed out. Regive turn command to continue");
		turntimeout = 15;
		decrease();
		return true;
	}
	else{
		System.out.println("Chair is already turning. Give command to stop");
		return false;
	}
}

public Boolean decrease() {
	GPIO.setMotorDirectionRight(0);
	GPIO.setMotorDirectionLeft(1);

	
	dutycycle = GPIO.getDutyCycle();
	if (GPIO.getStopped() == false && dutycycle >= 1){
		System.out.println("Stopping turn to Right");
		System.out.println("Enganging Brakes");
		GPIO.setPWMRight(0);
		GPIO.setPWMLeft(0);
		GPIO.setBrakes(0);

		GPIO.setDutyCycleBase(0);
		GPIO.setStopped(true);
		timer.cancel();
		turntimeout = 15;
		return true;
	}
	else{
		System.out.println("Chair is already stopped");
		return false;
	}
}

public Boolean emergencyStop() {
	System.out.println("Emergency Stop");
	GPIO.setPWMRight(0);
	GPIO.setPWMLeft(0);

	System.out.println("Enganging Brakes");
	GPIO.setBrakes(0);

	GPIO.setDutyCycleBase(0);
	GPIO.setStopped(true);
	timer.cancel();
	turntimeout = 15;
	return true;
}

public Boolean adjust() {
	//Not sure if this will be implemented with turning
	return null;
}

public Boolean neutral() {
	//Stop and keep brakes not engaged
	System.out.println("Chair going into neutral");
	GPIO.setBrakes(1);
	GPIO.setPWMLeft(0);
	GPIO.setPWMRight(0);


	return true;
}

protected void createInterupt(int milliseconds, Boolean increase) {
	timer = new Timer();
	timer.schedule(new interupt_reminder(), milliseconds );

} 

class interupt_reminder extends TimerTask{
	public void run() {
		timer.cancel();
		increase();
	}

}

@Override
public int getDutyCycle() {
	// TODO Auto-generated method stub
	return GPIO.getDutyCycle();
}


}
