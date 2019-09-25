package Functions;

public class Label {
	static int labelName=1992;
	public static String newLabel(){
		labelName++;
		return "L"+labelName;
	}

}
