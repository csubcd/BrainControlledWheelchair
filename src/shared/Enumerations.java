package shared;

public class Enumerations {
	
	public enum HeadsetConnectionState{
		Connected, Disconnected
	}
		
	public enum MotorState {
		forward, backward, left, right, stop, error, emergency, not_found, training, run, action_level, shutdown
	}
	
	public static MotorState intToMotorState(int state) {
		switch (state) {
			case 2: return MotorState.forward;
			case 4: return MotorState.backward;
			case 32: return MotorState.left;
			case 64: return MotorState.right;
			default: return MotorState.not_found;
		}
	}	
	
	public static int motorStateToInt(MotorState state) {
		switch(state) {
		case forward: return 2;
		case backward: return 4;
		case left: return 32;
		case right: return 64;
		default:
			break;
		}
		return 0;
	}
}
