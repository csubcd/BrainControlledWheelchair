package motor;

public abstract class IState {
	public abstract Boolean increase();
	public abstract Boolean decrease();
	public abstract Boolean emergencyStop();
	public abstract Boolean adjust();
	public abstract Boolean rightTurn();
	public abstract Boolean leftTurn();
	public abstract int getDutyCycle();
	protected abstract void createInterupt(int milliseconds, Boolean increase);

}
