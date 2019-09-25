import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import syntaxtree.AllocationExpression;
import syntaxtree.AndExpression;
import syntaxtree.ArrayAllocationExpression;
import syntaxtree.ArrayAssignmentStatement;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.ArrayType;
import syntaxtree.AssignmentStatement;
import syntaxtree.BooleanType;
import syntaxtree.BracketExpression;
import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.Clause;
import syntaxtree.CompareExpression;
import syntaxtree.Expression;
import syntaxtree.ExpressionList;
import syntaxtree.ExpressionTail;
import syntaxtree.ExpressionTerm;
import syntaxtree.FalseLiteral;
import syntaxtree.Goal;
import syntaxtree.Identifier;
import syntaxtree.IfStatement;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.MainClass;
import syntaxtree.MessageSend;
import syntaxtree.MethodDeclaration;
import syntaxtree.MinusExpression;
import syntaxtree.Node;
import syntaxtree.NodeListOptional;
import syntaxtree.NotExpression;
import syntaxtree.PlusExpression;
import syntaxtree.PrimaryExpression;
import syntaxtree.Statement;
import syntaxtree.ThisExpression;
import syntaxtree.TimesExpression;
import syntaxtree.TrueLiteral;
import syntaxtree.Type;
import syntaxtree.VarDeclaration;
import syntaxtree.WhileStatement;
import visitor.GJDepthFirst;

public class TypeCheckingVisitor extends GJDepthFirst<String, String> {
	ClassMembersVisitor myVisitor;		//Visitor for variables and methods.
	ClassVisitor classVisitor;			//Visitor for classes and extended classes
	Map<String, String> localTable;		//Local symbol table for each method.
	String methodName;					//Current method.
	String className;					//Current class.
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public TypeCheckingVisitor(ClassMembersVisitor _myVisitor,ClassVisitor _classVisitor) {
		this.myVisitor = _myVisitor;
		this.classVisitor = _classVisitor;

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Goal n, String argu) {
		String _ret = null;
		this.methodName = "main";
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MainClass n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		this.className = n.f1.accept(this, argu);
		n.f13.accept(this, null);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		n.f10.accept(this, argu);
		n.f11.accept(this, argu);
		n.f12.accept(this, argu);
		n.f13.accept(this, argu);
		n.f14.accept(this, argu);
		String print = n.f15.accept(this, argu);
		n.f16.accept(this, argu);
		n.f17.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassDeclaration n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		this.className = n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, className);
		n.f4.accept(this, className);
		n.f5.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassExtendsDeclaration n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		this.className = n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MethodDeclaration n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String methodReturningType = "";
		this.methodName = n.f2.accept(this, argu); 
		Map<String, methodClass> myMap = this.myVisitor.methodTable.get(className);//Get information from methodTable
		this.localTable = new HashMap<String, String>();							//for current method.
		if (myMap != null) {
			methodClass temp = myMap.get(methodName);		//Get the class for return type and parameters.
			methodReturningType = temp.returnType;			
			if (temp.parameters != null) {
				for (Map.Entry<String, String> entry : temp.parameters
						.entrySet()) {
					this.localTable.put(entry.getKey().trim(), entry.getValue().trim()); //Copy parameters to local
				}																		//symbol table.

			}

		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		String variables = n.f7.accept(this, argu);
		if (variables != "") {
			String[] vars = variables.split("//");
			for (int i = 0; i < vars.length; i++) {
				String[] typeName = vars[i].split(",");
				if (this.localTable.containsKey(typeName[1]) == true) {
					throw new SemanticException("Variable " + typeName[1]
							+ " already declared in arguments in "
							+ this.className + "." + methodName + ".");

				}
				localTable.put(typeName[1].trim(), typeName[0]);
			}
		}
		n.f8.accept(this, argu);
		n.f9.accept(this, null);

		String returningMethodType = n.f10.accept(this, argu);//check the type of expression we return
		if (!returningMethodType.equals("int")				//and compare it with the method.returningType.
				&& !returningMethodType.equals("boolean")
				&& !returningMethodType.equals("int[]") && !returningMethodType.equals("this")) {
			returningMethodType = this.search(returningMethodType);
			if (!methodReturningType.equals(returningMethodType)) {
				throw new SemanticException("Wrong type in return.Expecting "
						+ methodReturningType + " got " + returningMethodType
						+ " in " + this.className + "." + this.methodName
						+ " assignment.");
			}

		}
		else if(returningMethodType.equals("this")==true){		//Returning current class object.
			if (!methodReturningType.equals(this.className)) {	//Checking if class object equals directly to 
				boolean foundExtendedClass = false;				// return type.Otherwise,search for extended one.
				String currClass = this.className;  
				String tempClass;
				while (this.classVisitor.extendedClassNames.containsKey(currClass) == true
						&& foundExtendedClass == false) {    	//We check every time for for current class
					//											//and extended class if we find that returning time
					if (currClass.equals(returningMethodType) == true) {  //is a child class of this(current class).
						foundExtendedClass = true;						

					} else {
						tempClass = this.classVisitor.extendedClassNames
								.get(currClass);
						currClass = tempClass;
					}
				}
					if(foundExtendedClass=false){
						throw new SemanticException("Wrong type in return.Expecting "
								+ methodReturningType + " got " + this.className
								+ " in " + this.className + "." + this.methodName
								+ " assignment.");
					}
			
			
			
		}
		}
		else {
			if (!methodReturningType.equals(returningMethodType)) {
				throw new SemanticException("Wrong type in return.Expecting "
						+ methodReturningType + " got " + returningMethodType
						+ " in " + this.className + "." + this.methodName
						+ " assignment.");
			}

		}
		n.f11.accept(this, argu);
		n.f12.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Statement n, String argu) {
		return n.f0.accept(this, argu);
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(VarDeclaration n, String argu) {
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

	public String visit(NodeListOptional n, String argu) {
		if (n.present()) {
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
		String _ret = n.f0.tokenImage.toString();
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(AssignmentStatement n, String argu) {
		String _ret = null;
		String identifierType;
		String identifier = n.f0.accept(this, argu);
		identifierType = this.search(identifier);
		n.f1.accept(this, argu);
		String ret = n.f2.accept(this, argu);
		if (ret.equals("this")) {
			ret = this.className;
		} 
		else if (!ret.equals("int") && !ret.equals("boolean")    //Check if the expression is a variable(not int,boolean,
				&& !ret.equals("int[]")							//int[] and not in class table.
				&& !this.myVisitor.classVarsTable.containsKey(ret)) 
		{
			ret = search(ret);
		} 
		else if (this.myVisitor.classVarsTable.containsKey(ret)) 
		{
			boolean foundExtendedClass = false;
			String currClass = ret;
			String tempClass;
			while (this.classVisitor.extendedClassNames.containsKey(currClass) == true
					&& foundExtendedClass == false) {
				if (currClass.equals(identifierType) == true) 
				{
					foundExtendedClass = true;
					ret = identifierType;

				} 
				else 
				{
					tempClass = this.classVisitor.extendedClassNames
							.get(currClass);
					currClass = tempClass;
				}

			}

			if (foundExtendedClass == false
					&& !currClass.equals(identifierType)) {
				throw new SemanticException("1Different type variables(typeOf("
						+ identifierType + ")=typeOf(" + ret + ")in "
						+ this.className + "." + this.methodName
						+ " assignment.");
			} else {
				ret = identifierType;
			}
		}
		if (!identifierType.equals(ret)) {
			throw new SemanticException("Different type variables(typeOf("
					+ identifierType + ")=typeOf(" + ret + ")in "
					+ this.className + "." + this.methodName + " assignment.");

		}
		n.f3.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayAssignmentStatement n, String argu) { //We only look for int[]=int[]
		String _ret = null;
		String identifier = n.f0.accept(this, argu);
		if (!identifier.equals("int") && !identifier.equals("boolean")
				&& !identifier.equals("int[]")) {
			identifier = search(identifier);
		}
		n.f1.accept(this, argu);
		String expression = n.f2.accept(this, argu);				
		if (!expression.equals("int") && !expression.equals("boolean")
				&& !expression.equals("int[]")) {
			expression = search(expression);
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		String identifier2 = n.f5.accept(this, argu);
		if (!identifier2.equals("int") && !identifier2.equals("boolean")
				&& !identifier2.equals("int[]")) {
			identifier2 = search(identifier2);
		}
		if (!identifier.equals("int[]")) {
			throw new SemanticException("Variable is type of " + identifier
					+ " not of type int [] in " + this.className + "."
					+ this.methodName + " assignment.");

		} else if (!expression.equals("int")) {
			throw new SemanticException("Offset is not of type int in "
					+ this.className + "." + this.methodName + " assignment.");

		} else if (!identifier2.equals("int")) {
			throw new SemanticException("Could not assign " + identifier2
					+ " to int [] in " + this.className + "." + this.methodName
					+ " assignment.");

		}
		n.f6.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayLookup n, String argu) {
		String _ret = null;
		_ret = n.f0.accept(this, argu);
		String identifier1 = search(_ret);
		if (!identifier1.equals("int[]")) {
			throw new SemanticException("Variable " + _ret + " in "
					+ this.className + "." + this.methodName + " is type of "
					+ identifier1 + " ,not int[].");
		}
		n.f1.accept(this, argu);
		_ret = n.f2.accept(this, argu);
		if (!_ret.equals("int")) {
			String identifier2 = search(_ret);
			if (!identifier2.equals("int")) {
				throw new SemanticException("Variable " + _ret + " in "
						+ this.className + "." + this.methodName
						+ " is  type of " + identifier2 + " ,not of int.");
			}
		}
		n.f3.accept(this, argu);
		return "int";
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PrimaryExpression n, String argu) {
		String ret = n.f0.accept(this, argu);
		return ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Expression n, String argu) {
		return n.f0.accept(this, argu);
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PlusExpression n, String argu) {
		String _ret = null;
		String _ret1 = n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String _ret2 = n.f2.accept(this, argu);
		_ret = this.comparePlusMinusTimestChecking(_ret1, _ret2);
		String[] temp = _ret.split(",");
		if (!temp[0].equals(temp[1])) {
			throw new SemanticException("Cannot add types " + temp[0] + " and "
					+ temp[1] + " in " + this.className + "." + this.methodName
					+ ".");
		} else if (temp[0].equals("boolean") && temp[1].equals("boolean")) {
			throw new SemanticException("Cannot multiply types " + temp[0]
					+ " and " + temp[1] + " in " + this.className + "."
					+ this.methodName + ".");

		}
		return temp[0];
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MinusExpression n, String argu) {
		String _ret = null;
		String _ret1 = n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String _ret2 = n.f2.accept(this, argu);
		_ret = this.comparePlusMinusTimestChecking(_ret1, _ret2);
		String[] temp = _ret.split(",");
		if (!temp[0].equals(temp[1])) {
			throw new SemanticException("Cannot delete types " + temp[0]
					+ " and " + temp[1] + " in " + this.className + "."
					+ this.methodName + ".");
		} else if (temp[0].equals("boolean") && temp[1].equals("boolean")) {
			throw new SemanticException("Cannot delete types " + temp[0]
					+ " and " + temp[1] + " in " + this.className + "."
					+ this.methodName + ".");

		}
		return temp[0];
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(TimesExpression n, String argu) {
		String _ret = null;
		String _ret1 = n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String _ret2 = n.f2.accept(this, argu);
		_ret = this.comparePlusMinusTimestChecking(_ret1, _ret2);
		String[] temp = _ret.split(",");
		if (!temp[0].equals(temp[1])) {
			throw new SemanticException("Cannot multiply types " + temp[0]
					+ " and " + temp[1] + " in " + this.className + "."
					+ this.methodName + ".");
		} else if (temp[0].equals("boolean") && temp[1].equals("boolean")) {
			throw new SemanticException("Cannot multiply types " + temp[0]
					+ " and " + temp[1] + " in " + this.className + "."
					+ this.methodName + ".");

		}
		return temp[0];
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(CompareExpression n, String argu) {
		String _ret = null;
		String _ret1 = n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String _ret2 = n.f2.accept(this, argu);
		_ret = this.comparePlusMinusTimestChecking(_ret1, _ret2);
		String[] temp = _ret.split(",");
		if (!temp[0].equals(temp[1])) {
			throw new SemanticException("Cannot compare " + temp[0] + " and "
					+ temp[1] + " in " + this.className + "." + this.methodName
					+ ".");
		} else if (temp[0].equals("boolean") && temp[1].equals("boolean")) {
			throw new SemanticException("Cannot compare " + temp[0] + " and "
					+ temp[1] + " in " + this.className + "." + this.methodName
					+ ".");

		}
		return "boolean";
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerLiteral n, String argu) {
		String _ret;
		_ret = n.f0.accept(this, argu);
		return "int";
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(TrueLiteral n, String argu) {
		n.f0.accept(this, argu);
		return "boolean";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FalseLiteral n, String argu) {
		n.f0.accept(this, argu);
		return "boolean";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayLength n, String argu) {
		String _ret = null;
		_ret = n.f0.accept(this, argu);
		String typeIdentifier = search(_ret);
		if (!typeIdentifier.equals("int[]")) {
			throw new SemanticException("Variable " + _ret + " in "
					+ this.className + "." + this.methodName + " is  type of "
					+ typeIdentifier + " ,not of int array(int[]).");

		}
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return "int";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IfStatement n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String returned = n.f2.accept(this, argu);
		if (!returned.equals("int") && !returned.equals("boolean")
				&& !returned.equals("int[]")) {
			String returnedType = search(returned);
			if (!returnedType.equals("boolean")) {
				throw new SemanticException(returnedType
						+ " detected.Expecting boolean in " + this.className
						+ "." + this.methodName + " call.");

			}
		} else if (!returned.equals("boolean")) {
			throw new SemanticException(returned
					+ " detected.Expecting boolean in " + this.className + "."
					+ this.methodName + " call.");

		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		return _ret;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(WhileStatement n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String returned = n.f2.accept(this, argu);
		if (!returned.equals("int") && !returned.equals("boolean")
				&& !returned.equals("int[]")) {
			String returnedType = search(returned);
			if (!returnedType.equals("boolean")) {
				throw new SemanticException(returnedType
						+ " detected.Expecting boolean in " + this.className
						+ "." + this.methodName + " call.");

			}
		} else if (!returned.equals("boolean")) {
			throw new SemanticException(returned
					+ " detected.Expecting boolean in " + this.className + "."
					+ this.methodName + " call.");

		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ThisExpression n, String argu) {
		n.f0.accept(this, argu);
		return "this";
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MessageSend n, String argu) {
		String _ret = null;
		_ret = n.f0.accept(this, argu);
		methodClass temp = null;
		String methodIdentifier = n.f2.accept(this, argu);
		;
		if (_ret.equals("this")) { // Case this,look only in classTable of
									// current class.

			n.f1.accept(this, argu);

			Map<String, methodClass> methodMap = this.myVisitor.methodTable
					.get(this.className);
			if (methodMap == null) {
				throw new SemanticException("Could not find method"
						+ methodIdentifier + " in " + this.className + "."
						+ this.methodName + " function call.");
			}
			temp = methodMap.get(methodIdentifier);
		} else {
			boolean foundInClassTable = false;

			methodIdentifier = n.f2.accept(this, argu);
			Map<String, methodClass> methodMap = this.myVisitor.methodTable
					.get(_ret); 							// Get name of class in Primary
			if (methodMap != null) {						 // expression to search in localtable.
				
				if (methodMap.containsKey(methodIdentifier) == true) {
					temp = methodMap.get(methodIdentifier);
					foundInClassTable = true;

				} else {
					throw new SemanticException("Could not find method "
							+ methodIdentifier + " in " + this.className + "."
							+ this.methodName + " function call.");

				}

			}
			if (foundInClassTable == false) {
				if (this.localTable!=null && this.localTable.containsKey(_ret) == true ) { // If exists in
																						// local
																						// table
																						// then
					String classNameField = this.localTable.get(_ret); // It is
																							// a
																							// local
																							// field-variable
																							// of
																							// current
																							// class
																							// that
																							// is a
																							// class
																							// type.
					if (this.myVisitor.methodTable.containsKey(classNameField) == true) {
						Map<String, methodClass> methodMap2 = this.myVisitor.methodTable
								.get(classNameField);
						if (!methodMap2.containsKey(methodIdentifier)) {
							throw new SemanticException(
									"Could not find method " + methodIdentifier
											+ " in " + this.className + "."
											+ this.methodName
											+ " function call.");

						} else {
							temp = methodMap2.get(methodIdentifier);
						}
					}
				}
				else {
					if (this.myVisitor.classVarsTable
							.containsKey(this.className) == true) {
						Map<String, String> tempMapForVars = this.myVisitor.classVarsTable
								.get(this.className);
						if (tempMapForVars != null
								&& tempMapForVars.containsKey(_ret)) { // Check
																		// if
																		// Class
																		// Type
																		// is in
																		// curr
																		// class
																		// ST.
							String methodClass2 = tempMapForVars.get(_ret);
							if (this.myVisitor.methodTable
									.containsKey(methodClass2)) { // Check if
																	// class
																	// exists
																	// in method
																	// ST.
								Map<String, methodClass> localMap = this.myVisitor.methodTable
										.get(methodClass2);
								if (localMap.containsKey(methodIdentifier) == true) { // Check
																						// if
																						// function
																						// exists.
									temp = localMap.get(methodIdentifier);
								} else {
									throw new SemanticException(
											"Could not find method "
													+ methodIdentifier + " of "
													+ methodClass2 + " in "
													+ this.className + "."
													+ this.methodName
													+ " function call.");

								}

							} else {
								throw new SemanticException(
										"Could not find method "
												+ methodIdentifier + " in "
												+ this.className + "."
												+ this.methodName
												+ " function call.");

							}

						} else {
							throw new SemanticException(
									"Could not find method " + methodIdentifier
											+ " in " + this.className + "."
											+ this.methodName
											+ " function call.");

						}
					} else {
						throw new SemanticException("Could not find method "
								+ methodIdentifier + " in " + this.className
								+ "." + this.methodName + " function call.");

					}

				}
			}
		}

		n.f3.accept(this, argu);
		String expressionString = n.f4.accept(this, argu);
		if (expressionString != null) {
			String[] parameters = expressionString.split("//");
			if ((temp.parameters==null) || (temp.parameters.size()!= parameters.length)) {
				throw new SemanticException(
						"Could not match number of parameters in "
								+ this.className + "." + this.methodName
								+ " function " + methodIdentifier + " call.");

			}
			for (int i = 0; i < parameters.length; i++) {   //Assigning parameters type to array of parameters for checking
				
				if (parameters[i].equals("int")
						|| parameters[i].equals("boolean")
						|| parameters[i].equals("int[]")
						|| this.classVisitor.classNames.contains(parameters[i])) {
				} else if (parameters[i].equals("this")) {
					parameters[i] = this.className;
				} else {
					parameters[i] = search(parameters[i]);
				}
			}

			int i = 0;
			for (Map.Entry<String, String> entry2 : temp.parameters.entrySet()) {
				
				if (!entry2.getValue().equals(parameters[i])) {
					if (!this.classVisitor.classNames.contains(parameters[i])) {

						throw new SemanticException(
								"Problem with type of parameters in  "
										+ this.className + "."
										+ this.methodName + " function "
										+ methodIdentifier + " call.");
					} else {
						boolean foundExtendedClass = false;
						String currentClass = parameters[i];
						String tempClass;
						while (this.classVisitor.extendedClassNames
								.containsKey(currentClass) == true
								&& foundExtendedClass == false) {
							tempClass = this.classVisitor.extendedClassNames
									.get(currentClass);
							if (tempClass.equals(entry2.getValue()) == true) {
								foundExtendedClass = true;

							} else {
								currentClass = tempClass;
							}

						}
						if (foundExtendedClass == false) {
							throw new SemanticException(
									"Problem with type of parameters in  "
											+ this.className + "."
											+ this.methodName + " function "
											+ methodIdentifier + " call.");

						}
					}
				}
				i++;
			}
		} else {
			if (temp.parameters != null) {
				throw new SemanticException(
						"Could not match number of parameters in "
								+ this.className + "." + this.methodName
								+ " function " + methodIdentifier + " call.");

			}
		}
		n.f5.accept(this, argu);
		return temp.returnType;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ExpressionList n, String argu) {
		String _ret = null;
		String _ret1 = n.f0.accept(this, argu);
		String _ret2 = n.f1.accept(this, argu);
		if (!_ret2.equals("")) {
			_ret = _ret1 + "//" + _ret2;
		} else {
			_ret = _ret1;
		}
		return _ret;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ExpressionTail n, String argu) {
		return n.f0.accept(this, argu);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ExpressionTerm n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		_ret = n.f1.accept(this, argu);
		return _ret;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Clause n, String argu) {
		String _ret = n.f0.accept(this, argu);
		return _ret;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NotExpression n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		_ret = n.f1.accept(this, argu);
		if (!_ret.equals("boolean")) {
			String typeId = search(_ret);
			if (!typeId.equals("boolean")) {
				throw new SemanticException("Variable " + _ret + " in "
						+ this.className + "." + this.methodName
						+ " is  type of " + typeId + " ,not of boolean.");
			}

		}
	
		return "boolean";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(AllocationExpression n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String identifier = n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		return identifier;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayAllocationExpression n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		String offsetIdentifier = n.f3.accept(this, argu);
		if (!offsetIdentifier.equals("int")
				&& !offsetIdentifier.equals("boolean")
				&& !offsetIdentifier.equals("int[]")) {
			offsetIdentifier = this.search(offsetIdentifier);
			if (!offsetIdentifier.equals("int")) {
				throw new SemanticException(
						"Wrong type of offset in array allocation,variable is "
								+ offsetIdentifier + " not of int in "
								+ this.className + "." + this.methodName
								+ " assignment.");
			}

		} else {
			if (!offsetIdentifier.equals("int")) {
				throw new SemanticException(
						"Wrong type of offset  in array allocation,variable is "
								+ offsetIdentifier + " not of int in "
								+ this.className + "." + this.methodName
								+ " assignment.");
			}
		}
		n.f4.accept(this, argu);
		return "int[]";
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BracketExpression n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		_ret = n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(AndExpression n, String argu) {
		String _ret = null;
		String _ret1 = n.f0.accept(this, argu);
		if (!_ret1.equals("int") && !_ret1.equals("boolean")
				&& !_ret1.equals("int[]")) {
			_ret1 = this.search(_ret1);
		}
		n.f1.accept(this, argu);
		String _ret2 = n.f2.accept(this, argu);
		if (!_ret2.equals("int") && !_ret2.equals("boolean")
				&& !_ret2.equals("int[]")) {
			_ret2 = this.search(_ret2);
		}
		if (!_ret1.equals("boolean") || !_ret2.equals("boolean")) {
			throw new SemanticException("Variable " + _ret + " in "
					+ this.className + "." + this.methodName + " is  type of "
					+ _ret1 + " ,not of boolean.");

		}
		return "boolean";
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String comparePlusMinusTimestChecking(String _ret1, String _ret2) {
		if (!_ret1.equals("int") && !_ret1.equals("boolean")) {
			_ret1 = this.search(_ret1);
		}
		if (!_ret2.equals("int") && !_ret2.equals("boolean")) {
			_ret2 = this.search(_ret2);
		}

		return _ret1 + "," + _ret2;

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String search(String _ret1) {
		if (this.localTable != null) {
			if (this.localTable.containsKey(_ret1) == true) {
				_ret1 = this.localTable.get(_ret1);
			} else {
				Map<String, String> tempMap = this.myVisitor.classVarsTable
						.get(className);
				if (tempMap != null) {
					if (tempMap.containsKey(_ret1)) {
						_ret1 = tempMap.get(_ret1);
					} else {
						throw new SemanticException("Uknown local variable "
								+ _ret1 + " in " + this.className + "."
								+ this.methodName + ".");

					}
				} else {
					throw new SemanticException("Uknown local variable "
							+ _ret1 + " in " + this.className + "."
							+ this.methodName + ".");

				}
			}
		} else {
			Map<String, String> tempMap = this.myVisitor.classVarsTable
					.get(className);
			if (tempMap != null) {
				if (tempMap.containsKey(_ret1)) {
					_ret1 = tempMap.get(_ret1);
				} else {
					throw new SemanticException("Uknown local variable "
							+ _ret1 + " in " + this.className + "."
							+ this.methodName + ".");

				}

			} else {
				throw new SemanticException("Uknown local variable " + _ret1
						+ " in " + this.className + "." + this.methodName + ".");

			}

		}
		return _ret1;
	}

}
