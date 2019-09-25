import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;
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
import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.CompareExpression;
import syntaxtree.FalseLiteral;
import syntaxtree.FormalParameterList;
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
import syntaxtree.PrintStatement;
import syntaxtree.ThisExpression;
import syntaxtree.TimesExpression;
import syntaxtree.TrueLiteral;
import syntaxtree.Type;
import syntaxtree.VarDeclaration;
import syntaxtree.WhileStatement;
import visitor.GJDepthFirst;
import Functions.Label;
import Functions.Temp;

public class MyPigletVisitor extends GJDepthFirst<String, String> {

	Map<String, String> LocalTable;
	ClassMembersVisitor cVisitor;
	String methodCurrentClass; // method's current class.
	String currentMethod; // current method.
	String rightCurrentClass; // this is for multiple assignement of type
								// a=sth.foo().bar().foo_bar()
	BufferedWriter bw; // Buffer we write.

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public MyPigletVisitor(ClassMembersVisitor cVisitor_, BufferedWriter bw_) {
		this.cVisitor = cVisitor_;
		this.bw = bw_;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Goal n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MainClass n, String argu) {
		String _ret = null;
		try {
			bw.write("MAIN ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String classIdentifier = n.f1.accept(this, argu); // Main class.
		this.methodCurrentClass = classIdentifier;
		this.currentMethod = "main";
		n.f14.accept(this, argu);
		n.f15.accept(this, this.methodCurrentClass + "_main");
		n.f16.accept(this, argu);
		n.f17.accept(this, argu);
		try {
			bw.write("END ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassDeclaration n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		this.methodCurrentClass = n.f1.f0.tokenImage.toString();
		n.f2.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		return _ret;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ClassExtendsDeclaration n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		this.methodCurrentClass = n.f1.f0.tokenImage.toString();
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		return _ret;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(AllocationExpression n, String argu) {
		try {
			bw.write("BEGIN ");
			String buffer = null;
			String _ret = null;
			n.f0.accept(this, argu);
			String className = n.f1.f0.tokenImage.toString();
			this.rightCurrentClass = className; //In case of new B().sth..
			methodClass functionsClass = this.cVisitor.methodTable
					.get(className);
			Map<String, classVariableInfo> variableMapClass = this.cVisitor.classVarsTable
					.get(className);		
			int VTsize;
			int Classsize;
			String tempVTable = Temp.newTemp();
			String tempVariables = Temp.newTemp();

			if (functionsClass != null) {	//If we have function,hallocate space.
				VTsize = functionsClass.methods.size();
				int tmpVT = VTsize * 4;
				buffer = "MOVE " + tempVTable + " " + "HALLOCATE "
						+ Integer.toString(tmpVT) + " ";
				bw.write(buffer);
			} else {
				VTsize = 0;
			}
			if (variableMapClass != null) {		//If we have variables,hallocate space.
				Classsize = variableMapClass.size();
				int tempCS = (Classsize + 1) * 4;
				buffer = "MOVE " + tempVariables + " " + "HALLOCATE "
						+ Integer.toString(tempCS) + " ";
				bw.write(buffer);
			} else {
				Classsize = 0;

			}

			int position;
			if (functionsClass != null) {
				for (Map.Entry<String, Integer> entry : functionsClass.methods
						.entrySet()) {
					position = entry.getValue() * 4;
														//Hallocate space for each function
														//in the VTABLE
					buffer = "HSTORE " + tempVTable + " "
							+ Integer.toString(position) + " " + entry.getKey()
							+ " ";
					bw.write(buffer);

				}
			}
			for (int i = 0; i < Classsize; i++) {			//Hallocate space for each variable
															//in the class space..
				position = (i + 1) * 4;
				buffer = "HSTORE " + tempVariables + " "
						+ Integer.toString(position) + " " + "0 ";
				bw.write(buffer);
			}
			buffer = "HSTORE " + tempVariables + " " + "0 " + tempVTable + " ";
			bw.write(buffer);
			buffer = "RETURN " + tempVariables + " " + "END ";
			bw.write(buffer);
			n.f2.accept(this, argu);
			n.f3.accept(this, argu);
			return _ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(CompareExpression n, String argu) {
		String _ret = null;
		try {
			bw.write("LT ");
			n.f0.accept(this, argu);
			n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			return _ret;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MinusExpression n, String argu) {
		String _ret = null;
		try {
			bw.write("MINUS ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PlusExpression n, String argu) {
		String _ret = null;
		try {
			bw.write("PLUS ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(TimesExpression n, String argu) {
		String _ret = null;
		try {
			bw.write("TIMES ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
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

	public String visit(PrimaryExpression n, String argu) {
		//
		if (n.f0.choice.toString().contains("Identifier")) {
			return n.f0.accept(this, "//left");
		} else {

			return n.f0.accept(this, argu);
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MessageSend n, String argu) {
		try {
			String _ret = null;
			String buffer;
			bw.write("CALL BEGIN ");
			String label1 = Label.newLabel();
			String tmpName1 = Temp.newTemp();
			String tmpName2 = Temp.newTemp();
			String tmpName3 = Temp.newTemp();
			bw.write("MOVE " + tmpName1 + " ");
			n.f0.accept(this, argu);
			bw.write("CJUMP LT " + tmpName1 + " 1 " + label1 + " ");
			bw.write("ERROR ");
			bw.write(label1 + " HLOAD " + tmpName2 + " " + tmpName1 + " 0 ");
			bw.write("HLOAD " + tmpName3 + " " + tmpName2 + " ");
			n.f1.accept(this, argu);
			String methodName = n.f2.f0.tokenImage.toString();
			methodClass VTable = this.cVisitor.methodTable
					.get(this.rightCurrentClass);			
			String type = VTable.returnType.get(rightCurrentClass + "_"		//right current class is the clas
					+ methodName);											//the current expression has in MessageSend.
			int offset = VTable.methods.get(rightCurrentClass + "_"
					+ methodName);
			
			bw.write(offset * 4 + " RETURN " + tmpName3 + "END( " + tmpName1
					+ " ");
			n.f3.accept(this, argu);
			n.f4.accept(this, argu);
			n.f5.accept(this, argu);
			buffer = " ) ";
			bw.write(buffer);
			if (type != null) {			//We make the righcurrentClass ready for next assignement.
				if (!type.equals("int") && !type.equals("boolean")
						&& !type.equals("int[]")) {
					rightCurrentClass = type;
				}
			}
			return _ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MethodDeclaration n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		String methodName = n.f2.accept(this, argu);
		this.currentMethod = methodName;
		methodClass meth = this.cVisitor.methodTable
				.get(this.methodCurrentClass);
		if (meth != null) {
			try {
				bw.write(this.methodCurrentClass
						+ "_"
						+ methodName
						+ " [ "
						+ meth.methodParameters.get(this.methodCurrentClass
								+ "_" + this.currentMethod) + " ] BEGIN ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, this.methodCurrentClass);
		n.f9.accept(this, argu);
		try {
			bw.write("RETURN ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f10.accept(this, argu);
		try {
			bw.write("END ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f11.accept(this, argu);
		n.f12.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayLookup n, String argu) {
		try {
			String _ret = null;
			String returnTemp = Temp.newTemp();
			String randomTemp1 = Temp.newTemp();
			String tempTemp=Temp.newTemp();
			String sizeTemp=Temp.newTemp();
			String fLabel=Label.newLabel();
			String endLabel=Label.newLabel();
			String endLabel2=Label.newLabel();

			bw.write("BEGIN ");
			bw.write("MOVE "+tempTemp+" ");
			n.f0.accept(this, argu);
			bw.write(" CJUMP LT " + tempTemp + " 1 " +fLabel + " ");
			bw.write("ERROR ");
			bw.write(fLabel+" ");
			bw.write("HLOAD "+sizeTemp+" ");
			n.f0.accept(this, argu);
			bw.write("0");
			bw.write("CJUMP LT " + sizeTemp + " PLUS TIMES ");		//Case index >size.
			n.f2.accept(this, argu);
			bw.write(" 4 4 ");
			bw.write(endLabel + " ");
			bw.write("ERROR ");
			bw.write(endLabel + " CJUMP LT ");
			n.f2.accept(this, argu);
			bw.write(" 0 " + endLabel2		//Case index <0.
					+ " ");
			bw.write("ERROR ");
			bw.write(endLabel2 + " NOOP ");
			bw.write("MOVE " + randomTemp1 + " " + "PLUS ");
			n.f0.accept(this, argu);
			bw.write("TIMES PLUS ");
			n.f2.accept(this, argu);

			bw.write("1 4 ");
			bw.write("HLOAD " + returnTemp + " " + randomTemp1 + " 0 ");

			n.f1.accept(this, argu);
			n.f3.accept(this, argu);
			bw.write("RETURN " + returnTemp + " ");
			bw.write("END ");
			return _ret;
		} catch (IOException e) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FormalParameterList n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(WhileStatement n, String argu) {
		try {
			String _ret = null;
			String startLabel = Label.newLabel();
			String endLabel = Label.newLabel();
			bw.write(startLabel + " CJUMP ");
			n.f0.accept(this, argu);
			n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			bw.write(endLabel + " ");
			n.f3.accept(this, argu);
			n.f4.accept(this, argu);
			bw.write("JUMP " + startLabel + " ");
			bw.write(endLabel + " NOOP ");
			return _ret;
		} catch (IOException e) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayAssignmentStatement n, String argu) {
		String _ret = null;
		try {
			String sizeTemp = Temp.newTemp();
			String sizeTemp2 = Temp.newTemp();

			String endLabel = Label.newLabel();
			String endLabel2 = Label.newLabel();
			String fLabel=Label.newLabel();
			boolean foundLocal = false;
			Map<String, variableInfo> tmp1992 = this.cVisitor.methodsLocalTable
					.get(this.methodCurrentClass + "_" + this.currentMethod);
			if (tmp1992 != null) {
				variableInfo loco = tmp1992.get(n.f0.f0.tokenImage.toString());
				if (loco != null) {
					bw.write("HLOAD " + sizeTemp + " ");		//Case of a local array.
					n.f0.accept(this, argu);
					bw.write(" 0 ");
					bw.write("MOVE "+sizeTemp2+" ");
					n.f0.accept(this, argu);

					foundLocal = true;
				}
			}
			if (foundLocal == false) {					//case of class array.
				bw.write("HLOAD " + sizeTemp2 + " ");
				n.f0.accept(this, argu);
				bw.write("HLOAD " + sizeTemp + " " + sizeTemp2 + " 0 ");

			}
			bw.write(" CJUMP LT "+sizeTemp2+" ");
			bw.write(" 1 " +fLabel + " ");
			bw.write("ERROR ");
			bw.write(fLabel+" CJUMP LT " + sizeTemp + " PLUS TIMES ");		//Case index >size.
			n.f2.accept(this, argu);
			bw.write(" 4 4 ");
			bw.write(endLabel + " ");
			bw.write("ERROR ");
			bw.write(endLabel + " CJUMP LT ");
			n.f2.accept(this, argu);
			bw.write(" 0 " + endLabel2		//Case index <0.
					+ " ");
			bw.write("ERROR ");
			bw.write(endLabel2 + " NOOP ");
			String randomTemp1 = Temp.newTemp();
			String randomTemp2 = Temp.newTemp();
			boolean found = false;
			Map<String, classVariableInfo> tmp2 = this.cVisitor.classVarsTable
					.get(this.methodCurrentClass);
			Map<String, variableInfo> tmp3 = this.cVisitor.methodsLocalTable
					.get(this.methodCurrentClass + "_" + this.currentMethod);
			if (tmp2 != null) {
				if (tmp3 == null
						|| tmp3.containsKey(n.f0.f0.toString()) != true) {

					classVariableInfo tmp2Class = tmp2
							.get(this.methodCurrentClass + "."
									+ n.f0.f0.toString());
					String tmpCurrClass = this.cVisitor.extendedClassMap
							.get(this.methodCurrentClass);
					while (tmp2Class == null && tmpCurrClass != null) {	//repeatedly check if variable
																		// of current class or super class etc.
						tmp2Class = tmp2.get(tmpCurrClass + "."
								+ n.f0.f0.toString());
						tmpCurrClass = this.cVisitor.extendedClassMap
								.get(tmpCurrClass);

					}

					if (tmp2Class != null) {
						found = true;
						String buffer = null;
						bw.write("                                                               ");
						bw.write("HLOAD " + randomTemp2 + " ");
						n.f0.accept(this, argu);
						bw.write("MOVE " + randomTemp1 + " "
								+ "PLUS TIMES PLUS 1 ");
						n.f2.accept(this, argu);
						bw.write("4 " + randomTemp2 + " ");
						buffer = "HSTORE " + randomTemp1 + " 0 ";
						bw.write(buffer);

						n.f3.accept(this, argu);
						n.f4.accept(this, argu);
						n.f5.accept(this, argu);
						n.f6.accept(this, argu);
						
					}

				}
			}
			if (found == false) {
				String tempRand = Temp.newTemp();
				bw.write("MOVE " + tempRand + " PLUS TIMES PLUS ");

				n.f2.accept(this, argu);
				bw.write("1 4 ");
				n.f0.accept(this, argu);
				bw.write("HSTORE " + tempRand + " 0 ");
				n.f3.accept(this, argu);
				n.f4.accept(this, argu);
				n.f5.accept(this, argu);
				n.f6.accept(this, argu);

			}
			return _ret;
		} catch (IOException e) {
			return null;
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ArrayLength n, String argu) {  //Division algorithm implemented,since there is no division
		try {											//operands in piglet.
			String _ret = null;
			String tmpName1=Temp.newTemp();
			String label1=Label.newLabel();
			String randTemp1 = Temp.newTemp();
			String randTemp2 = Temp.newTemp();
			String randTemp3 = Temp.newTemp();
			bw.write("BEGIN ");
			bw.write("                                                                              ");
			bw.write("MOVE " + tmpName1 + " ");
			n.f0.accept(this, argu);
			String startLabel = Label.newLabel();
			String endLabel = Label.newLabel();
			bw.write("CJUMP LT " + tmpName1 + " 1 " + label1 + " ");
			bw.write("ERROR ");
			bw.write(label1 +" ");
			bw.write("HLOAD " + randTemp1 + " ");
			n.f0.accept(this, argu);
			bw.write(" 0 ");
			bw.write("MOVE " + randTemp2 + " 0 ");
			bw.write("MOVE " + randTemp3 + " " + " 0 ");
			bw.write(startLabel + " CJUMP LT " + randTemp2 + " " + randTemp1
					+ " " + endLabel + " ");
			bw.write("MOVE " + randTemp3 + " PLUS " + randTemp3 + " 1 ");
			bw.write("MOVE " + randTemp2 + " PLUS " + randTemp2 + " 4 ");
			bw.write("JUMP " + startLabel + " " + endLabel + " NOOP RETURN "
					+ randTemp3 + " END ");
			n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			return _ret;
		} catch (IOException e) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(AssignmentStatement n, String argu) { //Check whether its classVariable(HSTORE) 
		String _ret = null;									//or local variable(MOVE).
		try {

			boolean found = false;
			Map<String, classVariableInfo> tmp2 = this.cVisitor.classVarsTable
					.get(this.methodCurrentClass);
			Map<String, variableInfo> tmp3 = this.cVisitor.methodsLocalTable
					.get(this.methodCurrentClass + "_" + this.currentMethod);
			if (tmp2 != null) {
				if (tmp3 == null
						|| tmp3.containsKey(n.f0.f0.toString()) != true) {
					classVariableInfo tmp2Class = tmp2
							.get(this.methodCurrentClass + "."
									+ n.f0.f0.toString());
					String tmpCurrClass = this.cVisitor.extendedClassMap
							.get(this.methodCurrentClass);
					while (tmp2Class == null && tmpCurrClass != null) {	

						tmp2Class = tmp2.get(tmpCurrClass + "."
								+ n.f0.f0.toString());
						tmpCurrClass = this.cVisitor.extendedClassMap
								.get(tmpCurrClass);

					}

					if (tmp2Class != null) {
						found = true;
						String buffer = null;
						buffer = "HSTORE ";
						bw.write(buffer);

					}

				}
			}
			if (found == false) {
				bw.write("MOVE ");

			}
			n.f0.accept(this, argu);
			n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			n.f3.accept(this, argu);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(VarDeclaration n, String argu) { // Variables returned
															// with , between
															// type and name.
		String _ret = null;
		Map<String, variableInfo> tmp = this.cVisitor.methodsLocalTable
				.get(this.methodCurrentClass + "_" + this.currentMethod);
		variableInfo tmp2 = tmp.get(n.f1.f0.tokenImage.toString());
		try {
			bw.write("MOVE " + tmp2.tempNameVar + " 0 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_ret = n.f0.accept(this, argu);
		_ret = _ret + "," + n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return _ret;

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IfStatement n, String argu) {
		String _ret = null;
		try {
			n.f0.accept(this, argu);
			bw.write("CJUMP ");
			n.f1.accept(this, argu);
			String elseLabel = Label.newLabel();

			n.f2.accept(this, argu);
			bw.write(elseLabel + " ");
			n.f3.accept(this, argu);
			n.f4.accept(this, argu);
			String exitLabel = Label.newLabel();
			bw.write("JUMP " + exitLabel + " ");
			n.f5.accept(this, argu);
			bw.write(elseLabel + " ");
			n.f6.accept(this, argu);
			bw.write(exitLabel + " " + "NOOP ");
			return _ret;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return "";
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Type n, String argu) {

		String ret = n.f0.accept(this, argu);
		return ret;

	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String visit(ThisExpression n, String argu) {
		try {
			bw.write("TEMP 0 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.accept(this, argu);
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Identifier n, String argu) {

		try {
			boolean found = false;
			if (argu != null && argu.equals("//left")) { //Vase of a function after reaching here with //left as argu.
				Map<String, variableInfo> methodMap = this.cVisitor.methodsLocalTable
						.get(this.methodCurrentClass + "_" + this.currentMethod);
				boolean foundHLOAD = false;
				if (methodMap != null) {
					variableInfo methodVarClass = methodMap.get(n.f0.tokenImage
							.toString());
					if (methodVarClass != null) {
						String type = methodVarClass.type;
						if (!type.equals("int") && !type.equals("int[]")
								&& !type.equals("boolean")) {
							this.rightCurrentClass = type;		//Set up current expression type the returning	
						}
						
						bw.write(methodVarClass.tempNameVar + " ");
						foundHLOAD = true;
					}
				}
				if (foundHLOAD == false) {	  //Else,case of class object.
					Map<String, classVariableInfo> rightOp = this.cVisitor.classVarsTable
							.get(this.methodCurrentClass);
					if (rightOp != null) { 
						classVariableInfo rightOpClass = rightOp
								.get(this.methodCurrentClass + "."
										+ n.f0.tokenImage.toString());
						String tmpCurrClass = this.cVisitor.extendedClassMap
								.get(this.methodCurrentClass);
						while (rightOpClass == null && tmpCurrClass != null) {

							rightOpClass = rightOp.get(tmpCurrClass + "."
									+ n.f0.tokenImage.toString());
							tmpCurrClass = this.cVisitor.extendedClassMap
									.get(tmpCurrClass);

						}
						String type = rightOpClass.type;
						if (!type.equals("int") && !type.equals("int[]")
								&& !type.equals("boolean")) {
							this.rightCurrentClass = type;
						}
						String buffer = null;
						buffer = "BEGIN ";
						bw.write(buffer);
						String randomTemp = Temp.newTemp();
						buffer = "HLOAD " + randomTemp + " " + " TEMP 0 "
								+ rightOpClass.position * 4 + " ";
						bw.write(buffer);
						buffer = "RETURN " + randomTemp + " ";
						bw.write(buffer);
						buffer = "END ";
						bw.write(buffer);
						return "";

					}
				}

				return "";
			}
			if (argu != null && this.currentMethod != null) { //Else case of class variables
				Map<String, variableInfo> methodMap = this.cVisitor.methodsLocalTable
						.get(this.methodCurrentClass + "_" + currentMethod);
				if (methodMap != null) {
					variableInfo methodVarClass = methodMap.get(n.f0.tokenImage
							.toString());
					if (methodVarClass != null) {
						bw.write(methodVarClass.tempNameVar + " ");
						found = true;
					}
				}
			}
			if (argu != null && found == false) { 
				boolean innerFound = false;
				Map<String, variableInfo> tmp = this.cVisitor.methodsLocalTable
						.get(this.methodCurrentClass + "_" + this.currentMethod);
				if (tmp != null) {
					variableInfo local = tmp.get(n.f0.tokenImage.toString());
					if (local != null) {
						String TEMPNAME = local.tempNameVar;
						innerFound = true;
						try {
							bw.write(TEMPNAME + " ");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if (innerFound == false) {
					Map<String, classVariableInfo> tmp2 = this.cVisitor.classVarsTable
							.get(this.methodCurrentClass);
					classVariableInfo tmp2_2 = tmp2.get(this.methodCurrentClass
							+ "." + n.f0.tokenImage.toString());
					String tmpCurrClass = this.cVisitor.extendedClassMap
							.get(this.methodCurrentClass);
					while (tmp2_2 == null && tmpCurrClass != null) {
						tmp2_2 = tmp2.get(tmpCurrClass + "."
								+ n.f0.tokenImage.toString());
						tmpCurrClass = this.cVisitor.extendedClassMap
								.get(tmpCurrClass);

					}
					bw.write(tmp2_2.tempNameVar + " ");

				}
				return null;
			}
		} catch (IOException e) {
		}
		return n.f0.tokenImage.toString();

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

	public String visit(ArrayAllocationExpression n, String argu) {
		try {
			String _ret = null;
			bw.write("BEGIN ");
			String returnTemp = Temp.newTemp();
			String randomTemp = Temp.newTemp();
			String startLabel = Label.newLabel();
			String endLabel = Label.newLabel();
			bw.write("MOVE " + returnTemp + " " + "HALLOCATE TIMES PLUS ");
			n.f3.accept(this, argu);
			bw.write("1 4 ");
			bw.write("MOVE " + randomTemp + " 4 ");
			bw.write(startLabel + " CJUMP LT " + randomTemp + " TIMES PLUS ");
			n.f3.accept(this, argu);
			bw.write("1 4 " + endLabel + " ");
			bw.write("HSTORE PLUS " + returnTemp + " " + randomTemp + " "
					+ " 0 0 ");
			bw.write("MOVE " + randomTemp + " " + "PLUS " + randomTemp + " 4 ");
			bw.write("JUMP " + startLabel + " ");
			bw.write(endLabel + " " + "HSTORE " + returnTemp + " " + "0 TIMES ");
			n.f3.accept(this, argu);
			bw.write(" 4 ");
			bw.write("RETURN " + returnTemp + " ");
			bw.write("END ");
			
			return _ret;
		} catch (IOException e) {
			return null;

		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BooleanType n, String argu) {
		String ret = n.f0.tokenImage.toString();

		return ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerLiteral n, String argu) {
		try {
			bw.write(n.f0.tokenImage.toString() + " ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PrintStatement n, String argu) {
		String _ret = null;
		try {
			bw.write("PRINT ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(AndExpression n, String argu) {
		try {
			String _ret = null;
			bw.write("BEGIN ");
			String endLabel = Label.newLabel();
			String randomTemp = Temp.newTemp();
			bw.write("MOVE " + randomTemp + " 0 ");
			bw.write("CJUMP ");
			n.f0.accept(this, argu);
			bw.write(endLabel + " ");
			n.f1.accept(this, argu);
			bw.write("CJUMP ");
			n.f2.accept(this, argu);
			bw.write(endLabel + " ");
			bw.write("MOVE " + randomTemp + " 1 ");
			bw.write(endLabel + " NOOP RETURN " + randomTemp + " END ");
			return _ret;
		} catch (IOException e) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NotExpression n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		try {
			bw.write("MINUS 1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f1.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(TrueLiteral n, String argu) {
		try {
			bw.write("1 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(FalseLiteral n, String argu) {
		try {
			bw.write("0 ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.accept(this, argu);
	}
	

}
