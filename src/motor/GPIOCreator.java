package motor;

import java.util.Timer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.util.CommandArgumentParser;

import shared.Enumerations.MotorState;
import shared.OSChecker;

//***********************************
//This sets up the GPIO pins on the Pi that each state uses
//Pins used shown in README
//***********************************

public class GPIOCreator {

	//Debug flag turns off any GPIO usage in the program so it only outputs text 
	//This makes it runnable on any computer instead of only on the Pi
	static protected Boolean debug = !OSChecker.isPiUnix;
	static private Boolean instanciated = false;
	
	static protected GpioController gpio;
	static protected GpioPinPwmOutput pwmRight;
	static protected GpioPinPwmOutput pwmLeft;
	static protected GpioPinDigitalOutput brakes1;
	static protected GpioPinDigitalOutput brakes2;
	static protected GpioPinDigitalOutput motorDirectionRight1;
	static protected GpioPinDigitalOutput motorDirectionRight2;
	static protected GpioPinDigitalOutput motorDirectionLeft1;
	static protected GpioPinDigitalOutput motorDirectionLeft2;
	static protected Pin pinPwmRight;
	static protected Pin pinPwmLeft;
	static protected Pin pinBrakes1;
	static protected Pin pinBrakes2;
	static protected Pin pinDirectionRight1; //This is for the motor controller directional pin Assuming HIGH is forwad
	static protected Pin pinDirectionRight2;
	static protected Pin pinDirectionLeft1;
	static protected Pin pinDirectionLeft2;
	static private int rightDirection = 0;
	static private int leftDirection = 0;
	static private Timer timer;
	static private int stageCount;
	static private int MAX_DUTY = 100;
	static private int MIN_DUTY = 15;
	static private boolean stopped = false;
	static protected double offsetRight = 1; //Multiplied by the duty cycle for PID control so it should be between 0 and 1
	static protected double offsetLeft = 1;
	static private int DutyCycleBase = 0;
	static private int interuptTime = 200; //Milli Seconds
	//static private int dutycycle;
	
	public GPIOCreator(){
		if (instanciated == false){
			if (debug == false){
		gpio = GpioFactory.getInstance();

		//This is code to bypass args method since it is not being run with user input for GPIO pins
		String[] args = new String[0];
		//configures the Right PWM signal
		pinPwmRight = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_01,
				args);

		pinPwmLeft = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_24,
				args);

		pinBrakes1 = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_04,
				args);

		pinBrakes2 = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_05,
				args);

		pinDirectionRight1 = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_15,//Was 06
				args);

		pinDirectionRight2 = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_22,//was 13
				args);

		pinDirectionLeft1 = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_16,//was 11
				args);
		
		pinDirectionLeft2 = CommandArgumentParser.getPin(
				RaspiPin.class,
				RaspiPin.GPIO_23,//was 14
				args);

		pwmLeft = gpio.provisionPwmOutputPin(pinPwmLeft);

		pwmRight = gpio.provisionPwmOutputPin(pinPwmRight);

		brakes1 = gpio.provisionDigitalOutputPin(pinBrakes1, "brakes", PinState.HIGH);
		brakes2 = gpio.provisionDigitalOutputPin(pinBrakes2, "brakes", PinState.HIGH);

		
		motorDirectionRight1 = gpio.provisionDigitalOutputPin(pinDirectionRight1, "Direction", PinState.LOW);
		motorDirectionRight2 = gpio.provisionDigitalOutputPin(pinDirectionRight2, "Direction", PinState.LOW);

		motorDirectionLeft1 = gpio.provisionDigitalOutputPin(pinDirectionLeft1, "Direction", PinState.LOW);
		motorDirectionLeft2 = gpio.provisionDigitalOutputPin(pinDirectionLeft2, "Direction", PinState.LOW);

		//default closing value is pin low so brakes will be engaged when program exits
		brakes1.setShutdownOptions(false, PinState.LOW);
		brakes2.setShutdownOptions(false, PinState.LOW);

		
		motorDirectionRight1.setShutdownOptions(false, PinState.LOW);
		motorDirectionRight2.setShutdownOptions(false, PinState.LOW);

		motorDirectionLeft1.setShutdownOptions(false, PinState.LOW);
		motorDirectionLeft2.setShutdownOptions(false, PinState.LOW);

		//Enabling the Pwm with the desired frequency, and mode
		com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);


		//Range is the number of increments until it resets. So the duty cycle is now out of 100
		//Clock is the divisor to the PWM clock that specifies when a "tick" or count occurs
		//Pwm clock is 1.92MHz. in general  output frequency (Hz) = 19.2e6 Hz / pwmRange / pwmClock
		//Clock of 192 gives a output frequency of 1kHz
		com.pi4j.wiringpi.Gpio.pwmSetRange(100);
		com.pi4j.wiringpi.Gpio.pwmSetClock(192); 
			}
			setInstanciated(true);
	}}
	protected void setInstanciated (boolean input){
		instanciated = input;
	}
	public int getMAX_DUTY(){
		return MAX_DUTY;
	}
	public int getMIN_DUTY(){
		return MIN_DUTY;
	}
	public boolean getDebug(){
		return debug;
	}
	public Timer getTimer(){
		return timer;
	}
	public int getStageCount(){
		return stageCount;
	}
	public boolean setStageCount(int input){
		stageCount = input;
		return true;
	}
	public boolean getStopped(){
		return stopped;
	}
	public boolean setStopped(boolean input){
		stopped = input;
		return true;
	}
	public int getDutyCycleBase(){
		return DutyCycleBase;
	}
	public boolean setDutyCycleBase(int input){
		DutyCycleBase = input;
		String msg = "";
		if(input == 0) msg = "Stopped";
		else {
			if(rightDirection == 0) {
				if(leftDirection == 0) msg = "Backward " + input;
				else msg = "Right Turn";
			}
			else {
				if(leftDirection == 0) msg = "Left Turn";
				else msg = "Forward " + input;
			}
			
		}
		MotorController.getInstance().postMessage(msg);
		return true;
	}
	public int getInteruptTime(){
		return interuptTime;
	}
	public int getDutyCycle(){
		if(debug == false){
			return (pwmRight.getPwm() + pwmLeft.getPwm())/2;
		}
		else{
			return (int)((getDutyCycleBase() + getDutyCycleBase())/2);
		}
	}
	public boolean setPWMRight(int NewDutyCycle){
		if (debug == false){
			pwmRight.setPwm(NewDutyCycle);
		}
		return true;
	}
	public boolean setPWMLeft(int NewDutyCycle){
		if (debug == false){
			pwmLeft.setPwm(NewDutyCycle);
		}
		return true;
	}
	public boolean setMotorDirectionRight(int HighorLow){
		rightDirection = HighorLow;
		if(debug == false){
			if (HighorLow == 0){ //For Backwards motion
				motorDirectionRight1.low();
				//System.out.println("*******"+motorDirectionRight1.isHigh());
				//assert motorDirectionRight1.isLow() == true;
				motorDirectionRight2.high();
				assert (motorDirectionRight1.isHigh() != motorDirectionRight2.isHigh());
			}
			else if (HighorLow == 1){ //For Forwards motion
				motorDirectionRight2.low();
				//assert motorDirectionRight2.isLow() == true;
				motorDirectionRight1.high();
				assert (motorDirectionRight1.isHigh() != motorDirectionRight2.isHigh());

			}
		}
		return true;
	}
	public boolean setMotorDirectionLeft(int HighorLow){
		leftDirection = HighorLow;
		if(debug == false){
			if (HighorLow == 0){
				motorDirectionLeft1.low();
				//assert motorDirectionLeft1.isLow();
				motorDirectionLeft2.high();
				assert (motorDirectionLeft1.isHigh() != motorDirectionLeft2.isHigh());

			}
			else if (HighorLow == 1){
				motorDirectionLeft2.low();
				//assert motorDirectionLeft2.isLow();
				motorDirectionLeft1.high();
				assert (motorDirectionLeft1.isHigh() != motorDirectionLeft2.isHigh());

			}
		}
		return true;
	}
	public boolean setBrakes(int HighorLow){
		if(debug == false){
			if (HighorLow == 0){
				brakes1.low();
				brakes2.low();
			}
			else if (HighorLow == 1){
				brakes1.high();
				brakes2.high();
			}
		}
		return true;
	}

}
