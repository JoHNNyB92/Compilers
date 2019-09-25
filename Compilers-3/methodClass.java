import java.util.HashMap;
import java.util.Map;


public class methodClass {
	public Map<String,String> returnType;
	public Map<String,Integer> methods;
	public Map<String,Integer> methodParameters;
	//public String methodName;
	public methodClass(Map<String,String> _returnType,Map<String,Integer> _methods,Map<String,Integer> _methodParameters){
		this.returnType=_returnType;
		this.methods=_methods;
		this.methodParameters=_methodParameters;
		//this.methodName=_methodName;
	}
	public methodClass(){
		this.returnType=new HashMap<String,String>();
		this.methods=new HashMap<String,Integer>();
		this.methodParameters=new HashMap<String,Integer>();
		//this.methodName=_methodName;
	}
	public void copy(methodClass another){
		another.returnType=new HashMap<String,String>(this.returnType);
		another.methods=new HashMap<String,Integer>(this.methods); 
		another.methodParameters=new HashMap<String,Integer>(this.methodParameters);
		
	}

}
