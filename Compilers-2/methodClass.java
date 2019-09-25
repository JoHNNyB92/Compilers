import java.util.HashMap;
import java.util.Map;


public class methodClass {
	public String returnType;
	public Map<String,String> parameters;
	//public String methodName;
	public methodClass(String _returnType,Map<String,String> _parameters){
		this.returnType=_returnType;
		this.parameters=_parameters;
		//this.methodName=_methodName;
	}

}
