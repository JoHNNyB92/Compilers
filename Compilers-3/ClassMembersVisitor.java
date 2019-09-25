import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import syntaxtree.ArrayType;
import syntaxtree.BooleanType;
import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.FormalParameter;
import syntaxtree.FormalParameterList;
import syntaxtree.FormalParameterTerm;
import syntaxtree.Identifier;
import syntaxtree.IntegerType;
import syntaxtree.MainClass;
import syntaxtree.MethodDeclaration;
import syntaxtree.Node;
import syntaxtree.NodeListOptional;
import syntaxtree.Type;
import syntaxtree.VarDeclaration;
import visitor.GJDepthFirst;
import Functions.Temp;

public class ClassMembersVisitor extends GJDepthFirst<String, String> {
	Map<String, Map<String, classVariableInfo>> classVarsTable; // Keep
																// variables of
																// a class.
	Map<String, methodClass> methodTable; // Map with methods
	Map<String, Map<String, variableInfo>> methodsLocalTable; // Map with local
																// variables.
	String currClassName; // Current class.
	Map<String, String> extendedClassMap; // List with <class,Parentclass>.

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public ClassMembersVisitor() {
		classVarsTable = new HashMap<String, Map<String, classVariableInfo>>();
		methodTable = new HashMap<String, methodClass>();
		extendedClassMap = new HashMap<String, String>();
		this.methodsLocalTable = new HashMap<String, Map<String, variableInfo>>();
		Map<String, Map<String, String>> methodLocalTable = new HashMap<String, Map<String, String>>();

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MainClass n, String argu) 
	{
		String _ret = null;
		String classIdentifier = n.f1.accept(this, argu); // Main class.
		this.currClassName = classIdentifier;		//Class that contains main class.
		String variables = n.f14.accept(this, argu);	//Main's variables.
		Map<String, variableInfo> methodMap = new HashMap<String, variableInfo>(); 
		Map<String, String> mainMap = new HashMap<String, String>();
		if (variables != "") {
			String[] vars = variables.split("//");
			for (int i = 0; i < vars.length; i++) {
				String[] typeName = vars[i].split(",");	
				String TEMPNAME = Temp.newTemp();	//TempName of a variable for future use.
				variableInfo tmp = new variableInfo(TEMPNAME,0,
						typeName[0]);				//Main fields for (tempName,offset,typeOfVar)
				methodMap.put(typeName[1], tmp);		//Put it in main's map.
			}
		}
		this.methodsLocalTable.put(currClassName + "_main", methodMap); //Main is a method,so we keep its variables
		return _ret;													//in its map.
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassDeclaration n, String argu) 
	{
		String _ret = null;
		String classIdentifier = n.f1.accept(this, argu);
		this.currClassName = classIdentifier;
		String classVars = n.f3.accept(this, argu); // Simple class Variables.
		String[] arrClassVars = classVars.split("//"); // Split incoming
														// variables(with form
														// type1,name1//type2,name2
		Map<String, classVariableInfo> tempMap = new HashMap<String, classVariableInfo>(); // etc
																							// to
																							// a
																							// map
																							// .
		if (!classVars.equals("")) { // Check if class has no variables.
			for (int i = 0; i < arrClassVars.length; i++) {
				String[] typeName = arrClassVars[i].split(",");
				String TEMPNAME = "TEMP 0 " + (i + 1) * 4;		//Class that contains several fields ,we need to keep  
				classVariableInfo tmp = new classVariableInfo(TEMPNAME, false,	//its position.
						0, i + 1, typeName[0]);
				tempMap.put(classIdentifier + "." + typeName[1], tmp);
			}
		}
		this.classVarsTable.put(classIdentifier, tempMap); // Put classname with
															// map of variables
															// into map.
		_ret = n.f4.accept(this, argu); // _ret contains all methods in form
		if (_ret.isEmpty() != true) { // returnType-methodName-numOfArguments1//returnType2-methodName2-numOfArguments1.
			String[] methods = _ret.split("//");  //Splitting methods.
			Map<String, Integer> namePositionmap = new LinkedHashMap<String, Integer>(); 
			Map<String, Integer> nameParametersNumber = new HashMap<String, Integer>();
			Map<String, String> rt = new HashMap<String, String>();
			for (int i = 0; i < methods.length; i++) {
				String[] methodNTV = methods[i].split("-");
				nameParametersNumber.put(classIdentifier + "_" + methodNTV[1],Integer.parseInt(methodNTV[2]) + 1);
				rt.put(classIdentifier + "_" + methodNTV[1], methodNTV[0]);
				namePositionmap.put(classIdentifier + "_" + methodNTV[1], i);	//Position of function.

			}
			methodClass temp = new methodClass(rt, namePositionmap,nameParametersNumber);
			this.methodTable.put(classIdentifier, temp); //Put in method table the class and the methodMap that 
		}												//contains info for methods.
		return "";

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassExtendsDeclaration n, String argu) {
		String _ret = null;
		String className = n.f1.accept(this, argu); // Extended ClassName.
		String extendedClassName = n.f3.accept(this, argu);
		this.extendedClassMap.put(className, extendedClassName);
		this.currClassName = className;
		Map<String, classVariableInfo> extendedClassVarsMap = new HashMap<String, classVariableInfo>(
				this.classVarsTable.get(extendedClassName));
		int superClassSize = extendedClassVarsMap.size();
		Map<String, classVariableInfo> classVarsMap = new HashMap<String, classVariableInfo>();
		String classVars = n.f5.accept(this, argu); // This classes variables.
		String[] arrClassVars = classVars.split("//");
		if (!classVars.equals("")) {
			int parentVarsSize = 0;
			if (extendedClassVarsMap != null) {
				parentVarsSize = extendedClassVarsMap.size();
			} else {
				parentVarsSize = 0;
			}
			for (int i = parentVarsSize; i < arrClassVars.length
					+ parentVarsSize; i++) { 
				String[] typeName = arrClassVars[i - parentVarsSize].split(",");
				String TEMPNAME = "TEMP 0 " + (i + 1) * 4;
				classVariableInfo tmp = new classVariableInfo(TEMPNAME, false,
						0, i + 1, typeName[0]);
				classVarsMap.put(className + "." + typeName[1], tmp);
				}
		}
		extendedClassVarsMap.putAll(classVarsMap); // Copy all elements of
													// current class variables
													// map to extended.
		this.classVarsTable.put(className, extendedClassVarsMap);
		_ret = n.f6.accept(this, argu);
		methodClass extendedClassMap = new methodClass(null, null, null);
		if (this.methodTable.containsKey(extendedClassName)) // Search if the
																// extended
																// class has
																// methods.
		{
			this.methodTable.get(extendedClassName).copy(extendedClassMap); // If
																			// so
																			// ,copy
																			// it.
		} else {
			extendedClassMap = new methodClass();
		}

		if (_ret.isEmpty() != true) { // Methods Exist in class.
			String[] methods = _ret.split("//");
			for (int i = 0; i < methods.length; i++) {	//Check if the function is inherited.
				boolean found = false;
				String[] methodNTV = methods[i].split("-");
				for (Map.Entry<String, Integer> entry : extendedClassMap.methods.entrySet()) {
					String[] temp = entry.getKey().toString().split("_");
					int position = Integer.parseInt(entry.getValue().toString());
					if (temp[1].equals(methodNTV[1])) {

						extendedClassMap.methodParameters.put(className + "_"+ methodNTV[1],Integer.parseInt(methodNTV[2]) + 1);
						extendedClassMap.methods.remove(entry.getKey().toString());
						extendedClassMap.methods.put(className + "_"+ methodNTV[1], position);
						found = true;
						break;
					}


				}
				if (found == false) {	//If not found,insert a new entry in the map.
					extendedClassMap.methodParameters.put(className + "_"
							+ methodNTV[1], Integer.parseInt(methodNTV[2]) + 1);
					extendedClassMap.methods.put(
							className + "_" + methodNTV[1], superClassSize + i);
									//- 1);
					extendedClassMap.returnType.put(className + "_"
							+ methodNTV[1], methodNTV[0]);

				}

				this.methodTable.put(className, extendedClassMap);

			}

		}

		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeListOptional n, String argu) { // For the * ,which
															// visits
															// NodeListOptional,we
															// return all
		if (n.present()) { // nodes with // between them.
			if (n.size() == 1)
				return n.elementAt(0).accept(this, argu);
			String _ret = "";
			int _count = 0;
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				_ret = _ret + "//" + e.nextElement().accept(this, argu);
				_count++;
			}
			return _ret.substring(2);
		} else
			return "";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(VarDeclaration n, String argu) { // Variables returned
															// with , between
															// type and name.
		String _ret = null;
		_ret = n.f0.accept(this, argu);
		_ret = _ret + "," + n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Type n, String argu) {

		String ret = n.f0.accept(this, argu);
		return ret;

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerType n, String argu) {
		return n.f0.tokenImage.toString();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayType n, String argu) {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return "int[]";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BooleanType n, String argu) {
		String ret = n.f0.tokenImage.toString();

		return ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String visit(Identifier n, String argu) {
		return n.f0.tokenImage.toString();

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String visit(MethodDeclaration n, String argu) {	//Insert into local method table,the local variables.
		String _ret = null;
		_ret = n.f1.accept(this, null) + "-";
		String methodName = n.f2.accept(this, argu);
		Map<String, variableInfo> methodMap = new HashMap<String, variableInfo>();
		_ret = _ret + methodName + "-";
		String parameters = n.f4.accept(this, argu); // NAI
		int numOfParams = 0;
		if (parameters != null) {
			String[] arrParameters = parameters.split("//");
			numOfParams = arrParameters.length;

			for (int i = 0; i < arrParameters.length; i++) {
				String[] typeName = arrParameters[i].split(",");
				int k = i + 1;
				String TEMPNAME = "TEMP " + k;
				variableInfo tmp = new variableInfo(TEMPNAME,0,
						typeName[0]);
				methodMap.put(typeName[1], tmp);

			}
		}
		_ret = _ret + Integer.toString(numOfParams);

		String variables = n.f7.accept(this, argu);
		if (variables != "") {
			String[] vars = variables.split("//");
			for (int i = 0; i < vars.length; i++) {
				String[] typeName = vars[i].split(",");
				String TEMPNAME = Temp.newTemp();
				variableInfo tmp = new variableInfo(TEMPNAME,0,
						typeName[0]);

				methodMap.put(typeName[1], tmp);
			}
		}
		
		this.methodsLocalTable.put(this.currClassName + "_" + methodName,methodMap);
		n.f8.accept(this, argu);

		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameter n, String argu) {
		String returnString;
		returnString = n.f0.accept(this, argu) + "," + n.f1.accept(this, argu); // Parameters
																				// of
																				// method
																				// separated
																				// with
		return returnString; // commas.
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameterList n, String argu) {
		String _ret = "";
		_ret = n.f0.accept(this, argu);
		String _ret2 = n.f1.accept(this, argu);
		if (!_ret2.equals("")) {
			_ret = _ret + "//" + _ret2; // Different parameters separated with
										// //.
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameterTerm n, String argu) {
		String _ret = "";
		n.f0.accept(this, argu);
		_ret = _ret + n.f1.accept(this, argu);
		return _ret;
	}

}
