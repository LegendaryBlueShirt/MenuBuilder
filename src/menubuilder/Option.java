package menubuilder;

public interface Option {
	
	public int[] getLocation();
	public void drawSelf(double alpha);
	public String getType();
	public String getValue();
	public boolean inputLocked(); //Return true when the option handles its own input.
	public String input(int key); //Process input
}