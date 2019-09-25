package Temp;
public class myTemp {
	int counter;
	public myTemp(int maxCounter){
		this.counter=maxCounter;
	}
	public  String newTemp(){
		counter++;
		return  "TEMP "+counter;
	}

}