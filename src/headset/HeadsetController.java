package headset;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import com.emotiv.Iedk.*;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

import shared.Enumerations;
import shared.Enumerations.MotorState;
import shared.Enumerations.HeadsetConnectionState;
import main.AppController;

public class HeadsetController extends Thread {
	public static final Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
	public static final Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();
	public static BufferedReader in;
	public static HeadsetConnectionState connectionStatus = HeadsetConnectionState.Disconnected;
	private static Boolean run = true;

	public static int state = 0;
	public static IntByReference engineUserID = null;
	public static IntByReference profileID = null;
	public static String profileName = null;
	public static int pending = 0;
	public static MotorState option = null;
	

	public void run() {

		profileName = "Jason Gardner";
		
		engineUserID = new IntByReference(0);
		profileID 	 = new IntByReference(-1);

		
//		if (Edk.INSTANCE.IEE_EngineRemoteConnect("127.0.0.1", (short) 1726, "Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
//			System.out.println("Cannot connect to EmoComposer on [127.0.0.1]");
//		}
		
		if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
			AppController.getInstance().postMessageToGui("Emotiv Engine start up failed.");
		return;
		}
		
		SavingLoadingFunction(false);
		
		startLiveClassificationProcess();
		
		AppController.getInstance().postMessageToGui("Shutting Down Headset");
		run = false;
		
		SavingLoadingFunction(true);

		Edk.INSTANCE.IEE_EngineDisconnect();
		Edk.INSTANCE.IEE_EmoStateFree(eState);
		Edk.INSTANCE.IEE_EmoEngineEventFree(eEvent);
		
		System.out.println("Wating for the Thread");
	}

	

	public static void startLiveClassificationProcess() {

		while (run) {
			if (pending == 1) {
				handleUserInput(engineUserID, profileName, option);
				pending = 0;
			}

			state = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);

			if (state == EdkErrorCode.EDK_OK.ToInt()) {
				int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, engineUserID);

				if (eventType == Edk.IEE_Event_t.IEE_UserAdded.ToInt()) {
					connectionStatus = HeadsetConnectionState.Connected;
					AppController.getInstance().headsetConnected(true);
					AppController.getInstance().postMessageToGui("User " + engineUserID.getValue() + " connected");
				}
				
				else if (eventType == Edk.IEE_Event_t.IEE_UserRemoved.ToInt()) {
					connectionStatus = HeadsetConnectionState.Disconnected;
					AppController.getInstance().headsetConnected(false);
					AppController.getInstance().postMessageToGui("User " + engineUserID.getValue() + " disconnected");
				}

				else if (eventType == Edk.IEE_Event_t.IEE_EmoStateUpdated.ToInt()) {
					Edk.INSTANCE.IEE_EmoEngineEventGetEmoState(eEvent, eState); 
					
					System.out.print("MentalCommandGetCurrentAction: ");
					System.out.println(EmoState.INSTANCE
							.IS_MentalCommandGetCurrentAction(eState));
					System.out.print("CurrentActionPower: ");
					System.out.println(EmoState.INSTANCE
							.IS_MentalCommandGetCurrentActionPower(eState));
					AppController.getInstance().postEmoUpdate(
							EmoState.INSTANCE.IS_MentalCommandGetCurrentAction(eState),
							EmoState.INSTANCE.IS_MentalCommandGetCurrentActionPower(eState)
							);
				}
				else if (eventType == Edk.IEE_Event_t.IEE_MentalCommandEvent.ToInt()) {
					handleMentalCommandEvent(engineUserID, eEvent);
					System.out.println("MentalCommandEvent arrived");
				}
					
				else if (eventType == Edk.IEE_Event_t.IEE_FacialExpressionEvent.ToInt()) {
					System.out.println("FacialExpressionEvent arrived");
				}

				else {
					System.out.println("Something Else?");
				}

			} else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt())
				System.out.println("Internal error in Emotiv Engine!");
		}

	}

	public static void SavingLoadingFunction(boolean save) {

//		if (save) {
//			File f = new File(profileFilename);
//			if(f.exists() && !f.isDirectory()) { 
//				System.out.println("Profile with " + profileName + " exists.");
//				
//				if (Edk.INSTANCE.IEE_LoadUserProfile(engineUserID.getValue(), profileFilename) == EdkErrorCode.EDK_OK.ToInt()) {
//					System.out.println("Updating finished");
//				} else {
//					System.out.println("updating failed");
//				}
//			} else {
//				try {
//					f.createNewFile();
//				} catch (IOException e) {
//					System.out.println("file could not be created");
//					return;
//				}
//				System.out.println(f.getAbsolutePath());
//				if (Edk.INSTANCE.IEE_LoadUserProfile(engineUserID.getValue(), profileFilename) == EdkErrorCode.EDK_OK.ToInt())
//					System.out.println("Saving finished");
//				else
//					System.out.println("Saving failed");
//			}
//				
//			return;
//		} else {
//			File f = new File(profileFilename);
//			if(f.exists() && !f.isDirectory()) { 
//				if (Edk.INSTANCE.IEE_LoadUserProfile(engineUserID.getValue(), profileFilename) == EdkErrorCode.EDK_OK.ToInt())
//					System.out.println("Loading finished");
//				else
//					System.out.println("Loading failed");
//			}
//			return;
//		}

	}

	private static void handleMentalCommandEvent(IntByReference engineUserID, Pointer cognitiveEvent) {
		int eventType = Edk.INSTANCE.IEE_MentalCommandEventGetType(cognitiveEvent);
		Edk.INSTANCE.IEE_EmoEngineEventGetEmoState(eEvent, eState); 

		switch (eventType) {

		case 1: {
			System.out.println("MentalCommand training for user " + engineUserID.getValue() + " STARTED!");
			break;
		}

		case 2: {
			AppController.getInstance().postMessageToGui("MentalCommand training for user " + engineUserID.getValue() + " SUCCEEDED!");
			System.out.println("Accept training!!!");
			Edk.INSTANCE.IEE_MentalCommandSetTrainingControl(engineUserID.getValue(),
					Edk.IEE_MentalCommandTrainingControl_t.MC_ACCEPT.getType());
			AppController.getInstance().postEmoUpdate(
					EmoState.INSTANCE.IS_MentalCommandGetCurrentAction(eState),
					EmoState.INSTANCE.IS_MentalCommandGetCurrentActionPower(eState)
					);
			break;
		}

		case 3: {
			AppController.getInstance().postMessageToGui("MentalCommand training for user " + engineUserID.getValue() + " FAILED!");
			break;
		}
		
		case 4: {
			System.out.println("MentalCommand training for user " + engineUserID.getValue() + " COMPLETED!");
			break;
		}

		case 5: {
			System.out.println("MentalCommand training for user " + engineUserID.getValue() + " ERASED!");
			break;
		}

		case 6: {
			System.out.println("MentalCommand training for user " + engineUserID.getValue() + " REJECTED!");
			break;
		}
		
		case 7: {
			System.out.println("MentalCommand training for user " + engineUserID.getValue() + " RESET!");
			break;
		}
		case 8: {
			System.out.println("MentalCommand training for user " + engineUserID.getValue() + " Auto Sampling Neutral Completed!");
			break;
		}

		case 0: {
			System.out.println("MentalCommandAction");
			break;
		}

		default: {
			break;
		}
		}
	}
	
	public static float getSkillLevel(int state ) {
		FloatByReference skill = new FloatByReference(0);
		Edk.INSTANCE.IEE_MentalCommandGetActionSkillRating(engineUserID.getValue(), Enumerations.motorStateToInt(option), skill);
		return skill.getValue();
	}
	
	public static void handleUserInput(IntByReference userId, String profileName, MotorState line) {
		int errorCode = EdkErrorCode.EDK_OK.ToInt();
		switch (line) {
		case training: {
			long action1 = (long) EmoState.IEE_MentalCommandAction_t.MC_PUSH.ToInt();
			long action2 = (long) EmoState.IEE_MentalCommandAction_t.MC_LIFT.ToInt();
			long action3 = (long) EmoState.IEE_MentalCommandAction_t.MC_RIGHT.ToInt();
			long action4 = (long) EmoState.IEE_MentalCommandAction_t.MC_ROTATE_COUNTER_CLOCKWISE.ToInt();
			long listAction = action1 | action2 | action3 | action4;
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetActiveActions(userId.getValue(), listAction);
			if (errorCode == EdkErrorCode.EDK_OK.ToInt())
				AppController.getInstance().postMessageToGui(
						"Setting active actions for user " + userId.getValue());
			else
				if(errorCode == 776) {
					AppController.getInstance().postMessageToGui("Some commands need to be trained to be active");
				}
				else if(errorCode == 774) {
					AppController.getInstance().postMessageToGui("An invalid action bit was set: " + listAction);
				}
				else if(errorCode == 770) {
					AppController.getInstance().postMessageToGui("Some commands need to be trained to be active");
				}
				else {
					AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				}
			
			Edk.INSTANCE.IEE_EngineClearEventQueue(Edk.IEE_Event_t.IEE_EmoStateUpdated.ToInt());

			break;

		}
		case stop: {
			AppController.getInstance().postMessageToGui("Starting training NEUTRAL!");
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingAction(userId.getValue(), EmoState.IEE_MentalCommandAction_t.MC_NEUTRAL.ToInt());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingControl(userId.getValue(), Edk.IEE_MentalCommandTrainingControl_t.MC_START.getType());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			break;
		}
		case forward: {
			AppController.getInstance().postMessageToGui("Starting training FORWARD!");
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingAction(userId.getValue(), EmoState.IEE_MentalCommandAction_t.MC_PUSH.ToInt());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingControl(userId.getValue(), Edk.IEE_MentalCommandTrainingControl_t.MC_START.getType());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			break;
		}
		case backward: {
			AppController.getInstance().postMessageToGui("Starting training BACKWARD!");
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingAction(userId.getValue(), EmoState.IEE_MentalCommandAction_t.MC_LIFT.ToInt());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingControl(userId.getValue(), Edk.IEE_MentalCommandTrainingControl_t.MC_START.getType());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			break;
		}
		case right: {
			AppController.getInstance().postMessageToGui("Starting training RIGHT!");
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingAction(userId.getValue(), EmoState.IEE_MentalCommandAction_t.MC_RIGHT.ToInt());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingControl(userId.getValue(), Edk.IEE_MentalCommandTrainingControl_t.MC_START.getType());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			break;
		}
		case left: {
			AppController.getInstance().postMessageToGui("Starting training LEFT!");
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingAction(userId.getValue(), EmoState.IEE_MentalCommandAction_t.MC_ROTATE_COUNTER_CLOCKWISE.ToInt());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			errorCode = Edk.INSTANCE.IEE_MentalCommandSetTrainingControl(userId.getValue(), Edk.IEE_MentalCommandTrainingControl_t.MC_START.getType());
			if(errorCode != EdkErrorCode.EDK_OK.ToInt()) {
				AppController.getInstance().postMessageToGui("Setting MentalCommand active actions error: " + errorCode);
				break;
			}
			break;
		}
		case run: {
			IntByReference level = new IntByReference(0);
			if (Edk.INSTANCE.IEE_MentalCommandSetActivationLevel(userId.getValue(), 2) == EdkErrorCode.EDK_OK.ToInt())
				Edk.INSTANCE.IEE_MentalCommandGetActivationLevel(userId.getValue(), level);
			else
				System.out.println("Set MentalComand Activation level failed " + level.getValue());
			break;
		}
		case action_level: {
			IntByReference level = new IntByReference(0);
			Edk.INSTANCE.IEE_MentalCommandGetActivationLevel(userId.getValue(), level);
			System.out.println("Current MentalCommand Activation level: " + level.getValue());
		}

//		case "8": {
//			System.out.println("Saving profile...");
//			SavingLoadingFunction(userCloudId.getValue(), userId.getValue(), true, profileName);
//			break;
//		}
//		case "9": {
//			System.out.println("Loading profile...");
//			SavingLoadingFunction(userCloudId.getValue(), userId.getValue(), false, profileName);
//			break;
//		}
//		case "0": {
//			help();
//			break;
//		}
		case shutdown: {
			run = false;
			break;
		}
//		case "r": {
//			startLiveClassificationProcess();
//		}
		default: {
			break;
		}
		}
	}
	
	public static boolean checkTrained(NativeLongByReference action, int userId) {
		int result = Edk.INSTANCE.IEE_MentalCommandGetTrainedSignatureActions(userId, action);
		if (result == EdkErrorCode.EDK_OK.ToInt()) {
			return true;
		}
		return false;
	}

}