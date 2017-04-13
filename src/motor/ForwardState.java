package motor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

//****************************************************************
//This moves the chair forward using increase and decrease
//methods. It increases in 10% duty cycle chunks with an interrupt 
//every half second to pass back control to check for stop commands
//or changes in directions. EmergencyStop brings it to a stop by quickly 
//ramping and neutral disengages the breaks but gives a 0% pwm to the motors
//For pins used see README
//****************************************************************

public class ForwardState extends IState{

	private Timer timer;
	private int stageCount;
	private double offsetRight = 1; //Multiplied by the duty cycle for PID control so it should be between 0 and 1
	private double offsetLeft = 1;
	private int interuptTime = 200; //Milli Seconds
	private int dutycycle;
	private int state; //0 - stopped, 1 - increase, -1 - decrease
	private int neutralTime;
	private int turnCount;

	private GPIOCreator GPIO=null;

	//Forward Event constructor
	//Default uses GPIO_01 and GPIO_24 for Wheel PWM and GPIO_4 for brakes

	ForwardState(GPIOCreator GPIOInput){
		this.GPIO = GPIOInput;

		//Setting initial duty cycle to 0%
		GPIO.setPWMLeft(0);
		GPIO.setPWMRight(0);
		
		//These are specific to the forward method and its internal state calculations

		stageCount = 0;
		turnCount = 5;
		state =0;
		GPIO.setDutyCycleBase(0);
		GPIO.setStopped(true);
		timer = new Timer();

	}	

	protected void createInterupt (int miliseconds, Boolean increase){
		timer.cancel();
		timer = new Timer();

		if(increase) {
			timer.schedule(new interupt_increase(), miliseconds );
		}
		else {
			timer.schedule(new interupt_decrease(), miliseconds );
		}
	}

	public Boolean increase() {
		turnCount = 5;
		
		GPIO.setMotorDirectionRight(1);
		GPIO.setMotorDirectionLeft(1);


		//Calculate the current dutycycle values
		dutycycle = GPIO.getDutyCycle();

		//Checking if wheels are different speeds for error stop
		if (GPIO.getDutyCycleBase() - dutycycle > 15){
			System.out.println("Error: Wheel speeds are too different");
			emergencyStop();
			return false;
		}

		//If it is starting from a stop
		if (stageCount == 0 && dutycycle == 0 && GPIO.getStopped() == true){
			System.out.println("Disengage brakes");	


			GPIO.setBrakes(1);

			System.out.println("Starting from 0 to MIN");

			GPIO.setPWMLeft((int)Math.round(GPIO.getMIN_DUTY() * offsetLeft));
			GPIO.setPWMRight((int)Math.round(GPIO.getMIN_DUTY()*offsetRight));

			GPIO.setDutyCycleBase(GPIO.getMIN_DUTY());
			stageCount = 0;
			GPIO.setStopped(false);
			state = 1;
			return true;
		}		
		
		//If it is able to increase safely, was previously increasing, and is not the last increase in the stagecount 10% increase
		else if (stageCount < 9 && dutycycle <= (GPIO.getMAX_DUTY() - 1) && (state >= 0)){
			System.out.println("Starting increase");
			createInterupt(interuptTime, true);
			System.out.println("Interupt_scheduler called");
			
			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetRight));
		
			stageCount = stageCount + 1;
			GPIO.setDutyCycleBase( GPIO.getDutyCycleBase() + 1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			state = 1;
			return true;
		}
		//If it is being called after a decrease method and the decrease only partially completed so only some stagecounts need to be undone before reaching the 10%base
		else if (stageCount <= 9 && dutycycle <= (GPIO.getMAX_DUTY() - 1) && (state < 0)){
			System.out.println("Starting increase");
			createInterupt(interuptTime, true);
			System.out.println("Interupt_scheduler called");

			
			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetRight));
			
			//flip stageCount so it is just completing the partial states that have been completed before finishing the cycle
			//It will increase the amount that was decreased until it reaches the starting base of the 10% cycle
			//This keeps the increases and decreases to the 10% levels throughout the program
			if(stageCount != 0){
				stageCount = 10 - stageCount;
			}
			stageCount = stageCount + 1;
			GPIO.setDutyCycleBase( GPIO.getDutyCycleBase() + 1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			state = 1;
			
			return true;
		}

		
		//If it is able to increase safely and is on the last stagecount while at an increasing state
		else if (dutycycle <= GPIO.getMAX_DUTY() - 1 && (state >= 0)){
			System.out.println("Starting final increase");

			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetRight));
			
			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() + 1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			stageCount = 0;
			state = 1;
			return true;
		}
		else{
			System.out.println("Max Forward Speed already achieved.");
			return false;
		}

	}

	public Boolean decrease(){
		turnCount = 5;
		
		GPIO.setMotorDirectionRight(1);
		GPIO.setMotorDirectionLeft(1);


		System.out.println(stageCount);
		dutycycle = GPIO.getDutyCycle();

		System.out.println("Decreasing Speed from: ");
		System.out.println(GPIO.getDutyCycleBase());

		if (dutycycle <= GPIO.getMIN_DUTY() && GPIO.getStopped() == false){
			neutral();
			GPIO.setPWMLeft(0);
			GPIO.setPWMRight(0);
			
			GPIO.setDutyCycleBase(0);
			stageCount = 0;
			try {
				TimeUnit.SECONDS.sleep(neutralTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Chair decreased to a stop.");
			GPIO.setStopped(true);

			System.out.println("Engaged brakes.");
			
			GPIO.setBrakes(0);
			
			state = 0;
			return true;
		}

		else if (stageCount < 9 && dutycycle >= (GPIO.getMIN_DUTY() + 1) && (state <= 0)){
			System.out.println("Starting decrease");
			createInterupt(interuptTime, false);
			System.out.println("Interupt_scheduler called");

			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() - 1)*offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() - 1)*offsetRight));
			
			stageCount = stageCount + 1;
			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() -1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			state = -1;
			return true;
		}
		//Handles the case if a decrease is given during any stage of an increase if it has not completed the 10% increase cycle
		else if (stageCount <= 9 && dutycycle >= (GPIO.getMIN_DUTY() + 1) && (state > 0)){
			System.out.println("Starting decrease");

			//flip stageCount so it is just completing the partial states that have been completed before finishing the cycle
			//It will decrease the amount that was increased until it reaches the starting base of the 10% cycle
			//This keeps the increases and decreases to the 10% levels throughout the program
			
			//If it comes in at 0 this state catch is not needed and it can proceed normally after the state switch
			//Else we need to convert it to find the number of steps we need to reach our 10% benchmark going the other direction 
			if (stageCount != 0){
				stageCount = 10 - stageCount;
				System.out.println("stage count: " + stageCount);
			}

			//Check if this is the only decrease that is needed to reach the 10% base(only one opposing command had been called)
			if(stageCount < 9){
				createInterupt(interuptTime, false);
				System.out.println("Interupt_scheduler called");
				stageCount = stageCount + 1;
			}
			else{
				System.out.println("Starting final decrease");
				stageCount = 0;
			}

			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() - 1)*offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() - 1)*offsetRight));
			
						
			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() -1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			state = -1;
			return true;
		}
		
		
		
		else if (dutycycle >= GPIO.getMIN_DUTY() + 1 && (state <= 0)){
			System.out.println("Starting final decrease");

			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() - 1)*offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() - 1)*offsetRight));
			
			stageCount = 0;
			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() - 1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );

			state = -1;
			return true;
		}

		else{
			System.out.println("Min Forward Speed already achieved.");
			return false;
		}
	}
	
	public Boolean rightTurn() {
		System.out.println("Turning Right while going forward");
		dutycycle = GPIO.getDutyCycle();
		if(turnCount == 5){
			createInterupt(interuptTime, false);
			System.out.println("Interupt_scheduler called");
			if((dutycycle - 10) >= GPIO.getMIN_DUTY()){
				GPIO.setPWMRight((int)dutycycle - 10);
			}
			else if((dutycycle - 5) >= GPIO.getMIN_DUTY()){
				GPIO.setPWMRight((int)dutycycle - 5);
			}
			else{
				GPIO.setPWMLeft(dutycycle + 5);
			}
			turnCount = turnCount - 1;
			return true;
		}
		else if (turnCount <5 && turnCount > 0){
			createInterupt(interuptTime, false);
			turnCount = turnCount -1;
			return true;
		}
		else{
			System.out.println("Stopping turn while moving");
			GPIO.setPWMLeft(dutycycle);
			GPIO.setPWMRight(dutycycle);
			turnCount = 5;
			return true;
		}
		
	}
	
	public Boolean leftTurn() {
		System.out.println("Turning Left while going forward");
		dutycycle = GPIO.getDutyCycle();
		if(turnCount == 5){
			createInterupt(interuptTime, false);
			System.out.println("Interupt_scheduler called");
			if((dutycycle - 10) >= GPIO.getMIN_DUTY()){
				GPIO.setPWMLeft((int)dutycycle - 10);
			}
			else if((dutycycle - 5) >= GPIO.getMIN_DUTY()){
				GPIO.setPWMLeft((int)dutycycle - 5);
			}
			else{
				GPIO.setPWMRight(dutycycle + 5);
			}
			turnCount = turnCount - 1;
			return true;
		}
		else if (turnCount <5 && turnCount > 0){
			createInterupt(interuptTime, false);
			turnCount = turnCount -1;
			return true;
		}
		else{
			System.out.println("Stopping turn while moving");
			GPIO.setPWMLeft(dutycycle);
			GPIO.setPWMRight(dutycycle);
			turnCount = 5;
			return true;
		}
		
	}


	public Boolean emergencyStop() {
		System.out.println("Emergency Stop");

		//cancel interrupt
		timer.cancel();
		//Get the current speed
		dutycycle = GPIO.getDutyCycle();
		
		//decrease the speed to MIN and then set to 0 to prevent current spikes through the system at smaller duty cycles
		/*for (int i = 0; i < (GPIO.getDutyCycleBase() - GPIO.getMIN_DUTY()); i++){
			GPIO.setPWMLeft(GPIO.getDutyCycleBase() - 1);
			GPIO.setPWMRight(GPIO.getDutyCycleBase() -1);


			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() - 1);
			try{
				Thread.sleep(100);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}*/
		neutral();
		try {
			if(dutycycle <= 30){
				TimeUnit.SECONDS.sleep(neutralTime);
			}
			else if(dutycycle <= 50 && dutycycle >30){
				TimeUnit.SECONDS.sleep(neutralTime+1);
			}
			else if(dutycycle <=80 && dutycycle > 50){
				TimeUnit.SECONDS.sleep(neutralTime+2);
			}
			else if(dutycycle > 80){
				TimeUnit.SECONDS.sleep(neutralTime+3);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		GPIO.setPWMLeft(0);
		GPIO.setPWMRight(0);
		

		GPIO.setDutyCycleBase(0);
		GPIO.setStopped(true);
		System.out.println("Engaged brakes");

		GPIO.setBrakes(0);
		
		state = 0;
		stageCount = 0;
		return null;
	}

	public Boolean adjust(WheelAdjust WA) {
		// Implement the math of wheel checking
		// Adjusting the offset values from 0-1.
		// To change it to less than the max
		offsetRight = WA.calculateAdjustmentRight();
		offsetLeft = WA.calculateAdjustmentLeft();
		return null;
	}

	public Boolean neutral() {
		//Stop and keep brakes not engaged
		System.out.println("Chair going into neutral");


		GPIO.setBrakes(1);
		GPIO.setPWMLeft(0);
		GPIO.setPWMRight(0);


		return null;
	}

	class interupt_increase extends TimerTask{
		public synchronized void run() {
			timer.cancel();
			increase();
		}

	}


	
	class interupt_decrease extends TimerTask{
		public synchronized void run() {
			timer.cancel();
			decrease();
		}

	}

	@Override
	public Boolean adjust() {
		// Implement the math of wheel checking
		// Adjusting the offset values from 0-1.
		// To change it to less than the max
		return null;
	}
	@Override
	public int getDutyCycle() {
		return GPIO.getDutyCycle();
	}

}
