package main;

import shared.*;
import shared.Enumerations.MotorState;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import headset.HeadsetController;

public class AppController {
	volatile private static AppController appCtr = null;

	private UI gui;
	private HeadsetController hcThread;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private ObjectInputStream socketInput;
	private ObjectOutputStream socketOutput;

	private AppController() {
		hcThread = new HeadsetController();
		gui = new UI();
	}

	public static AppController getInstance() {
		if(appCtr == null) {
			appCtr = new AppController();
		}

		return appCtr;
	}

	public synchronized Boolean setUp() {

		if(OSChecker.isMac) {
			System.out.println("Is mac");
		}
		if(OSChecker.isLinux) {
			System.out.println("Is linux");
		}
		if(OSChecker.isWindows) {
			System.out.println("Is windows, Uck!!!");
		}

		hcThread.start();

		gui.postMessage("Connecting to Motor Controller");

		// using port 2000 for no real reason
        try {
			serverSocket = new ServerSocket(4000);
		    clientSocket = serverSocket.accept();
		    gui.postMessage("Motor Controller connected");
		    gui.changeMotorControllerConnectionColor("green");
			socketOutput = new ObjectOutputStream(clientSocket.getOutputStream());
			socketInput = new ObjectInputStream( clientSocket.getInputStream());
        } catch (IOException e) {
        	gui.postMessage("Problem Connecting to Motor Controller");
		    gui.changeMotorControllerConnectionColor("red");
			e.printStackTrace();
		}

		return true;
	}

	public void listen() {
		while(true)
		{
			try {
				String input = (String)socketInput.readObject();
				if(input.contains("Shutdown")) break;
				gui.postMessage(input);
			} catch (EOFException e) {
				gui.postMessage("Conection to Motor controller lost...");
			    gui.changeMotorControllerConnectionColor("red");
				try {
					socketOutput.close();
					socketInput.close();
					clientSocket = serverSocket.accept();
				    gui.postMessage("Motor Controller connected");
				    gui.changeMotorControllerConnectionColor("GREEN");
					socketOutput = new ObjectOutputStream(clientSocket.getOutputStream());
					socketInput = new ObjectInputStream( clientSocket.getInputStream());
				} catch (IOException e1) {
					e1.printStackTrace();
					gui.postMessage("Attempt to reconnect failed");
				    gui.changeMotorControllerConnectionColor("red");
					return;
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

		// shutdown
		try {
			socketInput.close();
			socketOutput.close();
			clientSocket.close();
			serverSocket.close();
			HeadsetController.option = MotorState.shutdown;
			HeadsetController.pending = 1;
			hcThread.join();
		} catch (IOException e) {
			// do nothing closing the socket
		} catch (InterruptedException e) {
			// do nothing closing the socket
		}
	}

	public void postMessageToGui(String string) {
		gui.postMessage(string);
	}
	
	public void postEmoUpdate(int state, double power) {
		gui.registerCommand(Enumerations.intToMotorState(state), (int) (power * 100));
	}

	public void sendState(MotorState state) {
		try {
			socketOutput.writeObject(state);
		} catch (IOException e) {
			gui.postMessage("Failed to send new state: " + state.toString());
		} catch (NullPointerException e) {
			gui.postMessage("Not connected to Motor Controller");
		}
	}
	
	public void headsetConnected(boolean connected) {
		if(connected)
			gui.changeHeadsetConnectionColor("green");
		else
			gui.changeHeadsetConnectionColor("red");
	}


	public static void main(String[] args) {
		if(AppController.getInstance().setUp() == false) {
			AppController.getInstance().postMessageToGui("Failed to set up");
		}
		AppController.getInstance().listen();
	}

}
