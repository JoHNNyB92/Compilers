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

public class ClassMembersVisitor extends GJDepthFirst<String, String> {
	Map<String, Map<String, String>> classVarsTable;		//Map<className,Map<NameOfVar,TypeOfVar>>
	Map<String, Map<String, methodClass>> methodTable;		//Map<className,Map<MethodName,Methodclass>>
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public ClassMembersVisitor() {
		classVarsTable = new HashMap<String, Map<String, String>>();
		methodTable = new HashMap<String, Map<String, methodClass>>();

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MainClass n, String argu) {
		String _ret = null;
		String classIdentifier = n.f1.accept(this, argu); //Main class.
		String mainClassReturnedVars = n.f14.accept(this, argu);
		String[] mainVars = mainClassReturnedVars.split("//");
		Map<String, String> mainMap = new HashMap<String, String>();
		if (!mainClassReturnedVars.equals("")) {
			for (int i = 0; i < mainVars.length; i++) {
				String[] typeName = mainVars[i].split(",");
				if (i != 0 && mainMap.containsKey(typeName[1]) == true) {
					throw new SemanticException("Duplicate parameter "+ typeName[1] + " in " + classIdentifier + ".main.");
				}
				mainMap.put(typeName[1], typeName[0]);
			}
		}
		this.classVarsTable.put(classIdentifier, mainMap);	//Put main class,with their variables, into map.
		return _ret;

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassDeclaration n, String argu) {
		String _ret = null;
		String classIdentifier = n.f1.accept(this, argu);
		String classVars = n.f3.accept(this, argu); //Simple class Variables.
		String[] arrClassVars = classVars.split("//");   //Split incoming variables(with form type1,name1//type2,name2
		Map<String, String> tempMap = new HashMap<String, String>(); // etc to a map .
		if (!classVars.equals("")) {		//Check if class has no variables.
			for (int i = 0; i < arrClassVars.length; i++) {
				String[] typeName = arrClassVars[i].split(",");
				if (i != 0 && tempMap.containsKey(typeName[1]) == true) {	//Check if the variable was already inserted
					throw new SemanticException("Duplicate field parameter " //before.
							+ typeName[1] + " in " + classIdentifier + ".");
				}
				tempMap.put(typeName[1], typeName[0]);
			}
		}
		this.classVarsTable.put(classIdentifier, tempMap);  //Put classname with map of variables into map.
		_ret = n.f4.accept(this, argu);	//_ret contains all methods in form 
		if (_ret.isEmpty() != true) {	//returnType-methodName-type1,name1/type2,name2//returnType2-methodName2-type1,name1.
			String[] methods = _ret.split("//");
			Map<String, methodClass> methodMap = new HashMap<String, methodClass>();
			for (int i = 0; i < methods.length; i++) {
				String[] methodNTV = methods[i].split("-");
				String[] Variables = methodNTV[2].split("/");
				Map<String, String> typeNamemap = new LinkedHashMap<String, String>();
				if (!methodNTV[2].equals("null")) {
					for (int k = 0; k < Variables.length; k++) {
						String[] typeName = Variables[k].split(",");
						if (typeNamemap.containsKey(typeName[1]) == true) {
							throw new SemanticException("Duplicate parameter "
									+ typeName[1] + " in " + classIdentifier
									+ "." + methodNTV[1] + ".");
						}
						typeNamemap.put(typeName[1], typeName[0]);	//Put it into map with format typeName[1=name
																	//							  typeName[0]=type
					}
					methodClass method = new methodClass(methodNTV[0],typeNamemap);  //New class for method.
					if (methodMap.containsKey(methodNTV[1]) == true) //If exists then it is a double method declaration.
					{
						throw new SemanticException("Duplicate method declaration in "
										+ classIdentifier + "." + methodNTV[1]
										+ ".");

					}
					methodMap.put(methodNTV[1], method); //Put into methodMap name and object method

				} 
				else 
					{
					methodClass method = new methodClass(methodNTV[0], null);// Method
																				// with
																				// no
																				// parameters-null.

					if (methodMap.containsKey(methodNTV[1]) == true) { 
						throw new SemanticException(
								"Duplicate method declaration in "
										+ classIdentifier + "." + methodNTV[1]
										+ ".");

					}
					methodMap.put(methodNTV[1], method);
				}
			}
			this.methodTable.put(classIdentifier, methodMap);
		}
		return "";

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassExtendsDeclaration n, String argu) {
		String _ret = null;
		String className = n.f1.accept(this, argu); //Extended ClassName.
		String extendedClassName = n.f3.accept(this, argu);
		Map<String, String> extendedClassVarsMap = new HashMap<String, String>(this.classVarsTable.get(extendedClassName)); 
		Map<String, String> classVarsMap = new HashMap<String, String>();// Get variables of class that this class extends.
		String classVars = n.f5.accept(this, argu);		//This classes variables.
		String[] arrClassVars = classVars.split("//");
		if(!classVars.equals("")){
			for (int i = 0; i < arrClassVars.length; i++) {	//Search through splitted arrClassVars.
				String[] typeName = arrClassVars[i].split(",");	
				if (classVarsMap.containsKey(typeName[1]) == true) {	//Search if already exists.
					throw new SemanticException("Duplicate field parameter"
							+ typeName[1] + " in " + className + ".");
				}
				classVarsMap.put(typeName[1], typeName[0]);
			}
		}
		extendedClassVarsMap.putAll(classVarsMap);	//Copy all elements of current class variables map to extended.
		this.classVarsTable.put(className, extendedClassVarsMap);
		_ret = n.f6.accept(this, argu);
		Map<String, methodClass> extendedClassMap;
		if (this.methodTable.containsKey(extendedClassName)) //Search if the  extended class has methods.
		{
			extendedClassMap = new HashMap<String, methodClass>(this.methodTable.get(extendedClassName)); //If so ,copy it.
		} 
		else 
		{
			extendedClassMap = new HashMap<String, methodClass>();
		}

		if (_ret.isEmpty() != true) { // Methods Exist in class.
			String[] methods = _ret.split("//");
			Map<String, methodClass> methodMap = new HashMap<String, methodClass>();
			for (int i = 0; i < methods.length; i++) {
				String[] methodNTV = methods[i].split("-");
				if (methodMap.containsKey(methodNTV[1]) == true) {
					throw new SemanticException(
							"Duplicate method declaration in " + className
									+ "." + methodNTV[1] + ".");

				}

				Map<String, String> typeNamemap = new LinkedHashMap<String, String>();
				if (!methodNTV[2].equals("null")) {
					String[] Variables = methodNTV[2].split("/");
					for (int k = 0; k < Variables.length; k++) {
						String[] typeName = Variables[k].split(",");
						if (typeNamemap.containsKey(typeName[1]) == true) {
							throw new SemanticException("Duplicate parameter "
									+ typeName[1] + " in " + className + "."
									+ methodNTV[1] + ".");
						}
						typeNamemap.put(typeName[1], typeName[0]);
					}
					methodClass temp = extendedClassMap.get(methodNTV[1]); // Check
																			// whether
																			// name
																			// of
																			// method
																			// is
																			// define
																			// in
					if (temp != null) { 									// parent class.
						if (methodNTV[0].equals(temp.returnType) == false) 
						{
							throw new SemanticException(
									"Overloading is not accepted in miniJava.Different return type in "
											+ className + "." + methodNTV[1]
											+ ".");
						} 
						else if (temp.parameters == null) 
						{ 									// Different num of arguments.
							throw new SemanticException(
									"Overloading is not accepted in miniJava.Different number of arguments in "
											+ className + "." + methodNTV[1]
											+ ".");
						} 
						else if (temp.parameters.size() != typeNamemap.size()) 
						{
							throw new SemanticException(
									"Overloading is not accepted in miniJava.Different number of arguments in "
											+ className + "." + methodNTV[1]
											+ ".");

						} 
						else 
						{
							String classStringCheck = "";		//Time to check parameters of both methods.
							String extendedClassStringCheck = "";
							for (Map.Entry<String, String> entry : temp.parameters.entrySet()) 
							{
								classStringCheck = classStringCheck
										+ entry.getValue().toString();

							}
							for (Map.Entry<String, String> entry : typeNamemap.entrySet()) 
							{
								extendedClassStringCheck = extendedClassStringCheck
										+ entry.getValue().toString();

							}
							if (classStringCheck.equals(extendedClassStringCheck) == false) {
								throw new SemanticException(
										"Overloading is not accepted in miniJava.Different type of parameters in "
												+ className + "."
												+ methodNTV[1] + ".");

							}
						}

					}

				} 
				else 
				{
					methodClass temp = extendedClassMap.get(methodNTV[1]); // Check
																			// whether
																			// name
																			// of
																			// method
																			// is
																			// define
																			// in
					if (temp != null) { // parent class.
						if (methodNTV[0].equals(temp.returnType) == false) {
							throw new SemanticException(
									"Overloading is not accepted in miniJava.Different return types in "
											+ className + "." + methodNTV[1]
											+ ".");
						} else if (temp.parameters != null) { // Different num
																// of arguments.
							throw new SemanticException(
									"Overloading is not accepted in miniJava.Different number of parameters in "
											+ className + "." + methodNTV[1]
											+ ".");
						}
					}

				}
				methodClass method = new methodClass(methodNTV[0], typeNamemap);

				methodMap.put(methodNTV[1], method);

			}
			if (extendedClassMap.isEmpty() == true) {
				this.methodTable.put(className, methodMap);

			} else {
				extendedClassMap.putAll(methodMap);
				this.methodTable.put(className, extendedClassMap);

			}
		}

		else if (extendedClassMap != null) {
			this.methodTable.put(className, extendedClassMap);
		}
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeListOptional n, String argu) {	//For the * ,which visits NodeListOptional,we return all 
		if (n.present()) {									//nodes with // between them.
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
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(VarDeclaration n, String argu) {  //Variables returned with , between type and name.
		String _ret = null;
		_ret = n.f0.accept(this, argu);
		_ret = _ret + "," + n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Type n, String argu) {

		String ret = n.f0.accept(this, argu);
		return ret;

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerType n, String argu) {
		return n.f0.tokenImage.toString();
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayType n, String argu) {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return "int[]";
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BooleanType n, String argu) {
		String ret = n.f0.tokenImage.toString();

		return ret;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public String visit(Identifier n, String argu) {
		return n.f0.tokenImage.toString();

	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MethodDeclaration n, String argu) {
		String _ret = null;
		String methodType = n.f1.accept(this, argu);
		String methodName = n.f2.accept(this, argu);
		String fpl = n.f4.accept(this, argu); //Variables used as parameters.
		if (fpl != null) 
		{
			fpl = fpl.replaceAll("//", "/");  //Replacing so we do not have conflict with // for splitting methods.
		}
		_ret = methodType + "-" + methodName + "-" + fpl; //Returning returnType-methodName-variables
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameter n, String argu) {
		String returnString;
		returnString = n.f0.accept(this, argu) + "," + n.f1.accept(this, argu); //Parameters of method separated with
		return returnString;													// commas.
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameterList n, String argu) {
		String _ret = "";
		_ret = n.f0.accept(this, argu);
		String _ret2 = n.f1.accept(this, argu);
		if (!_ret2.equals("")) {
			_ret = _ret + "//" + _ret2;		//Different parameters separated with //.
		}
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameterTerm n, String argu) {
		String _ret = "";
		n.f0.accept(this, argu);
		_ret = _ret + n.f1.accept(this, argu);
		return _ret;
	}

}
