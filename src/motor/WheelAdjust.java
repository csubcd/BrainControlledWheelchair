package motor;

import java.util.Scanner;

import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterruptCallback;


public class WheelAdjust{
	private static boolean debug = false;

	private GpioController gpio;
	private GpioPinDigitalInput RightWheel;
	private GpioPinDigitalInput LeftWheel;
	private static int RightWheelCounter;
	private static int LeftWheelCounter;
	private static double RightAdjust;
	private static double LeftAdjust;

	WheelAdjust(){

		RightWheelCounter = 0;
		LeftWheelCounter = 0;
		RightAdjust = 1;
		LeftAdjust = 1;

		//if(debug == false){
		if (Gpio.wiringPiSetup() == -1){
			System.out.println("GPIO setup failed");
		}

		Gpio.pinMode(0, Gpio.INPUT);
		Gpio.pinMode(3, Gpio.INPUT);

		Gpio.pullUpDnControl(0, Gpio.PUD_DOWN);
		Gpio.pullUpDnControl(3, Gpio.PUD_DOWN);

		Gpio.wiringPiISR(0, Gpio.INT_EDGE_RISING, new GpioInterruptCallback() {
	           @Override
	            public void callback(int pin) {
	                System.out.println(" ==>> GPIO PIN " + pin + " - INTERRUPT DETECTED <RISING>");
	                RightWheelCounter = RightWheelCounter + 1;
	                System.out.println("Right: "+ RightWheelCounter);
	            }
	    });

		Gpio.wiringPiISR(3, Gpio.INT_EDGE_RISING, new GpioInterruptCallback() {
	           @Override
	            public void callback(int pin) {
	                System.out.println(" ==>> GPIO PIN " + pin + " - INTERRUPT DETECTED <RISING>");
	                LeftWheelCounter = LeftWheelCounter + 1;
	                System.out.println("Left: "+ LeftWheelCounter);
	            }
	    });

		/*final GpioController gpio = GpioFactory.getInstance();

		RightWheel = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);

		LeftWheel = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN);

		RightWheel.setShutdownOptions(true);

		LeftWheel.setShutdownOptions(true);*/
	/*
		RightWheel.addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					System.out.println(" Gpio pin state change: " + event.getPin() + " = " + event.getState());
					//RightWheelCounter = RightWheelCounter + 1;
					//System.out.println("Right: " + RightWheelCounter);
			}
		});

		LeftWheel.addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					System.out.println(" Gpio pin state change: " + event.getPin() + " = " + event.getState());
					//LeftWheelCounter = LeftWheelCounter + 1;
					//System.out.println("Left: " + LeftWheelCounter);
			}
		});
*/
	/*	while(true){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

	}
/*
	public void RightCounterInterrupt() {

		RightWheelCounter = RightWheelCounter + 1;
	}
	public void LeftCounterInterrupt(){
		LeftWheelCounter = LeftWheelCounter + 1;
	}
	*/
	public double calculateAdjustmentRight(){
		if (RightWheelCounter > LeftWheelCounter){
			RightAdjust = ((double)LeftWheelCounter/(double)RightWheelCounter);
			System.out.println("Right wheel adjusted to: " + RightAdjust);
			LeftAdjust = 1;
		}
		else if(RightWheelCounter < LeftWheelCounter){
			LeftAdjust = ((double)RightWheelCounter/(double)LeftWheelCounter);
			System.out.println("Left wheel adjusted to: " + LeftAdjust);
			RightAdjust = 1;
		}
		else{
			//They are good and adjust levels should not be changed
		}
		return RightAdjust;

	}
	public double calculateAdjustmentLeft(){
		if (RightWheelCounter > LeftWheelCounter){
			RightAdjust = ((double)LeftWheelCounter/(double)RightWheelCounter);
			System.out.println("Right wheel adjusted to: " + RightAdjust);
			LeftAdjust = 1;
		}
		else if(RightWheelCounter < LeftWheelCounter){
			LeftAdjust = ((double)RightWheelCounter/(double)LeftWheelCounter);
			System.out.println("Left wheel adjusted to: " + LeftAdjust);
			RightAdjust = 1;
		}
		else{
			//They are good and adjust levels should not be changed
		}
		return LeftAdjust;

	}




	public static void main(String[] args){
		Scanner input  = new Scanner(System.in);

	System.out.println("I dont know what this does");
	WheelAdjust WA = new WheelAdjust();
	System.out.println("Wheel Adjust created");
	for (int i = 0; i < 10; i++){
		if (debug == false){
	       System.console().readLine("Press <ENTER> to continue program.\r\n");
		}
		else{
			System.out.println("Press <ENTER> to continue program.");
			input.nextLine();
		}
		System.out.println("Right: " + RightWheelCounter + " Left: " + LeftWheelCounter);
		WA.calculateAdjustmentRight();
		WA.calculateAdjustmentLeft();
		System.out.println("Right: " + RightAdjust + " Left: " + LeftAdjust);
	}	
	/*while(true){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	}
}
