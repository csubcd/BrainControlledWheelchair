package motor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

//****************************************************************
//This moves the chair Backward using increase and decrease
//methods. It increases in 10% duty cycle chunks with an interrupt 
//every half second to pass back control to check for stop commands
//or changes in directions. EmergencyStop brings it to a stop by quickly 
//ramping and neutral disengages the breaks but gives a 0% pwm to the motors
//For pins used see README
//****************************************************************

public class BackwardState extends IState{

	private Timer timer;
	private int stageCount;
	private double offsetRight = 1; //Multiplied by the duty cycle for PID control so it should be between 0 and 1
	private double offsetLeft = 1;
	private int interuptTime = 200; //Milli Seconds
	private int dutycycle;
	private int state; //0 - stopped, 1 - increased previously, -1 - decreased previously=
	private int neutralTime = 1;
	private int turnCount;

	private GPIOCreator GPIO=null;

	//Backward Event constructor
	//Default uses GPIO_01 and GPIO_24 for Wheel PWM and GPIO_4 for brakes
	BackwardState(GPIOCreator GPIOinput){
		GPIO = GPIOinput;
		GPIO.setPWMLeft(0);
		GPIO.setPWMRight(0);
		
		stageCount = 0;
		turnCount = 5;
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

	public Boolean increase(){
		turnCount = 5;
		GPIO.setMotorDirectionRight(0);
		GPIO.setMotorDirectionLeft(0);
		
		dutycycle = GPIO.getDutyCycle();
		
		if (GPIO.getDutyCycleBase() - dutycycle > GPIO.getMIN_DUTY()){
			System.out.println("Error: Wheel speeds are too different");
			emergencyStop();
			return false;
		}
		//Starting from a stop
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
		//Increasing if it was previously increasing and not at the 10% cycle
		else if (stageCount < 9 && dutycycle <= (GPIO.getMAX_DUTY() - 1) && (state >=0)){
			System.out.println("Starting increase");
			createInterupt(interuptTime, true);
			System.out.println("Interupt_scheduler called");
			GPIO.setPWMLeft((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetLeft));
			GPIO.setPWMRight((int)Math.round((GPIO.getDutyCycleBase() + 1)* offsetRight));

			stageCount = stageCount + 1;
			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() + 1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			state = 1;
			return true;
		}
		//Increasing if it was previously decreasing and only partly made it to the 10% cycle
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
			GPIO.setDutyCycleBase(GPIO.getDutyCycleBase() + 1);

			System.out.println("Duty Cycle changed to: " + GPIO.getDutyCycleBase() );
			state = 1;
			return true;
		}

		
		else if (dutycycle <= GPIO.getMAX_DUTY() - 1){
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
		GPIO.setMotorDirectionRight(0);
		GPIO.setMotorDirectionLeft(0);
		
		dutycycle = GPIO.getDutyCycle();
		System.out.println("Decreasing Speed from: ");
		System.out.println(GPIO.getDutyCycleBase());

		//Decreasing from a stop
		if (dutycycle <= GPIO.getMIN_DUTY() && GPIO.getStopped() == false){
			neutral();
			try {
				TimeUnit.SECONDS.sleep(neutralTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			GPIO.setPWMLeft(0);
			GPIO.setPWMRight(0);

			GPIO.setDutyCycleBase(0);
			stageCount = 0;
			System.out.println("Chair decreased to a stop.");
			GPIO.setStopped(true);
			System.out.println("Engaged brakes.");
			GPIO.setBrakes(0);

			state = -1;
			return true;
		}

		//Decreasing while in the middle of a 10% decrease cycle
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
		
		//Decreasing during a partially completed increase cycle. Will decrease to 10% base and stop
		else if (stageCount <= 9 && dutycycle >= (GPIO.getMIN_DUTY() + 1) && (state > 0)){
			System.out.println("Starting decrease");
			
			//flip stageCount so it is just completing the partial states that have been completed before finishing the cycle
			//It will increase the amount that was decreased until it reaches the starting base of the 10% cycle
			//This keeps the increases and decreases to the 10% levels throughout the program
			if (stageCount != 0){
				stageCount = 10 - stageCount;
			}

			if (stageCount < 9){//create interrupt like normal
				createInterupt(interuptTime, false);
				System.out.println("Interupt_scheduler called");
				stageCount = stageCount + 1;

			}
			else {//call it the last time without an interrupt
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

		//Final decrease in a decrease cycle 
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
	
	
	public Boolean emergencyStop() {
		System.out.println("Emergency Stop");
		
		//End any interrupts that are waiting
		timer.cancel();
		
		//Find the current speed
		dutycycle = GPIO.getDutyCycle();
		
		//Decrease the motors to MIN before stopping to reduce current spikes at duty cycles below min
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

	public Boolean adjust() {
		// Implement the math of wheel checking
		// Adjusting the offset values from 0-1.
		// To change it to less than the max
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
		public void run() {
			timer.cancel();
			increase();
		}

	}
	
	class interupt_decrease extends TimerTask{
		public void run() {
			timer.cancel();
			decrease();
		}

	}

	@Override
	public int getDutyCycle() {
		return GPIO.getDutyCycle();
		}

	@Override
	public Boolean rightTurn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean leftTurn() {
		// TODO Auto-generated method stub
		return null;
	}
}
