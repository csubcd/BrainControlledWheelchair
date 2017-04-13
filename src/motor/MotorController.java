package motor;

import shared.Enumerations.MotorState;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MotorController {

	private static MotorController motorController = null;
	
	private MotorState nextState = MotorState.stop;
	private MotorState currentState = MotorState.stop;
	
	private GPIOCreator gpio;
	private IState forwardEvent;
	private IState backwardEvent;
	private IState leftEvent;
	private IState rightEvent;
	
	private Socket socket;
	private ObjectInputStream socketInput;
	private ObjectOutputStream socketOutput;	
	private boolean connected = false;
	
	private MotorController() {}
	
	public static MotorController getInstance() {
		if(motorController == null) {
			motorController = new MotorController();
		}
		
		return motorController;
	}
	
	public boolean setUpConection(InetAddress serverIp, int portNumber) {
		gpio = new GPIOCreator();
		forwardEvent = new ForwardState(gpio);
		backwardEvent = new BackwardState(gpio);
		leftEvent = new LeftTurnState(gpio);
		rightEvent = new RightTurnState(gpio);
		
		while(true) {
			try {
				socket = new Socket(serverIp, portNumber);
			} catch (ConnectException e) {
				continue;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			break;
		}
		try {
			socketOutput = new ObjectOutputStream(socket.getOutputStream());
			socketInput = new ObjectInputStream( socket.getInputStream());
			connected = true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void listen() {
		System.out.println("MotorController set up");
		while(true) 
		{
			try {
				nextState = (MotorState)socketInput.readObject();
			} catch (Exception e) {
				nextState = MotorState.error;
			}
			if(gpio.getDutyCycle() == 0) {
				currentState = MotorState.stop;
			}
			switch (currentState) {
			case forward:
				switch (nextState) {
				case forward:
					if(!forwardEvent.increase()) {
						// increase failed...
						// do something
					}
					currentState = MotorState.forward;
					break;
				case emergency:
					forwardEvent.emergencyStop();
					currentState = MotorState.stop;
					break;
				case error:
					forwardEvent.emergencyStop();
					Shutdown();
					return;
				case backward:
					if(!forwardEvent.decrease()) {
						// decrease failed...
						// do something
					}
					if(forwardEvent.getDutyCycle() == 0) {
						currentState = MotorState.stop;
					}
					else {
						nextState = currentState;
					}
					break;
				case stop:
					forwardEvent.emergencyStop();
					currentState = MotorState.stop;
					break;
				case right:
					forwardEvent.rightTurn();
					currentState = MotorState.forward;
				case left:
					forwardEvent.leftTurn();
					currentState = MotorState.forward;
				default:
					break;
				}
				break;
			case backward:
				switch (nextState) {
				case backward:
					if(!backwardEvent.increase()) {
						// increase failed...
					}
					currentState = MotorState.backward;
					break;
				case emergency:
					backwardEvent.emergencyStop();
					currentState = MotorState.stop;
					break;
				case error:
					backwardEvent.emergencyStop();
					Shutdown();
					return;
				case forward:
					if(!backwardEvent.decrease()) {
						// decrease failed
						// do something
					}
					if(backwardEvent.getDutyCycle() == 0) {
						currentState = MotorState.stop;
					}
					else {
						nextState = currentState;
					}
					break;
				case stop:
					backwardEvent.emergencyStop();
					currentState = MotorState.stop;
					break;
				default:
					break;
				}
				break;
			case left:
				switch (nextState) {
				case emergency:
					leftEvent.emergencyStop();
					currentState = MotorState.stop;
					break;
				case error:
					Shutdown();
					return;
				case right:
					leftEvent.decrease();
					currentState = MotorState.stop;
					break;
				case left:
					leftEvent.increase();
					if(leftEvent.getDutyCycle() == 0) {
						currentState = MotorState.stop;
					}
					break;
				case stop:
					leftEvent.decrease();
					currentState = MotorState.stop;
					break;
				default:
					break;
				}
				break;
			case right:
				switch (nextState) {
				case emergency:
					rightEvent.emergencyStop();
					currentState = MotorState.stop;
					break;
				case error:
					Shutdown();
					return;
				case left:
					rightEvent.decrease();
					currentState = MotorState.stop;
					break;
				case right:
					rightEvent.increase();
					if(rightEvent.getDutyCycle() == 0) {
						currentState = MotorState.stop;
					}
					break;
				case stop:
					rightEvent.decrease();
					currentState = MotorState.stop;
					break;
				default:
					break;
				}
				break;
			case stop:
				switch (nextState) {
				case backward:
					if(!backwardEvent.increase()) {
						// increase failed...
					}
					currentState = MotorState.backward;
					break;
				case emergency:
					currentState = MotorState.stop;
					break;
				case error:
					Shutdown();
					return;
				case forward:
					if(!forwardEvent.increase()) {
						// increase failed...
						// do something
					}
					currentState = MotorState.forward;
					break;
				case left:
					leftEvent.increase();
					currentState = MotorState.left;
					break;
				case right:
					rightEvent.increase();
					currentState = MotorState.right;
					break;
				default:
					break;
				}
				break;
			case error:
				Shutdown();
				return;
			default:
				break;
			}
		}
	}
	
	public void postMessage(String string) {
		if(connected) {
			try {
				socketOutput.writeObject(string);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to send message: " + string);
			}
		}
	}
	
	private void Shutdown() {
		forwardEvent.emergencyStop();
		backwardEvent.emergencyStop();
		leftEvent.emergencyStop();
		rightEvent.emergencyStop();
		
		try {
			socketInput.close();
			socketOutput.close();
			socket.close();
			connected = false;
		} catch (IOException e) {
			// do nothing closing the socket
		} 		
		System.out.println("MC Shutdown");
	}

	public static void main(String[] args) {
		InetAddress serverIp = null;
		try {
			if(args.length < 1) {
				serverIp = InetAddress.getByName("127.0.0.1");
			}
			else {
				serverIp = InetAddress.getByName(args[0]);
			}
		} catch (UnknownHostException e) {
			System.out.println("Problem setting up ip address");
		}
		
		// using port 2000 for really no reason
		MotorController.getInstance().setUpConection(serverIp, 4000);
		MotorController.getInstance().listen();
	}
}
