import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;

import syntaxtree.BinOp;
import syntaxtree.CJumpStmt;
import syntaxtree.Call;
import syntaxtree.ErrorStmt;
import syntaxtree.Exp;
import syntaxtree.Goal;
import syntaxtree.HAllocate;
import syntaxtree.HLoadStmt;
import syntaxtree.HStoreStmt;
import syntaxtree.IntegerLiteral;
import syntaxtree.JumpStmt;
import syntaxtree.Label;
import syntaxtree.MoveStmt;
import syntaxtree.NoOpStmt;
import syntaxtree.Node;
import syntaxtree.NodeList;
import syntaxtree.NodeListOptional;
import syntaxtree.Operator;
import syntaxtree.PrintStmt;
import syntaxtree.Procedure;
import syntaxtree.Stmt;
import syntaxtree.StmtExp;
import syntaxtree.Temp;
import visitor.GJDepthFirst;
import Temp.myTemp;

public class MyPigletVisitor extends GJDepthFirst<String, String> {
	myTemp myTemp;		//Class that keeps highest temp till now.
	BufferedWriter bw;	//File we write.

	public MyPigletVisitor(int maxTemp, BufferedWriter _bw) {
		myTemp = new myTemp(maxTemp); //New clas with object's parameter highest temp we met.
		this.bw = _bw;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String visit(Goal n, String argu) {
		String _ret = null;
		try {
			bw.write("MAIN \n");
			n.f0.accept(this, argu);
			n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			bw.write("END \n");
			n.f3.accept(this, argu);
			n.f4.accept(this, argu);
		} catch (IOException e) {
			e.getMessage();
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Procedure n, String argu) {
		String _ret = null;
		String funcName = n.f0.accept(this, "function_name");  //If we reach the label,then it will know that 
		n.f1.accept(this, argu);								//print format is function_name [ numOfParameters].
		String numOfParameters = n.f2.accept(this, "offset");  //For the integer literal to write simply the number).
		try {
			bw.write(funcName + " [ " + numOfParameters + " ] \n");
		} catch (IOException e) {
			e.getMessage();
		}
		n.f3.accept(this, argu);
		n.f4.accept(this, "function");  //When it is a function we want the begin/end rule.
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Exp n, String argu) {
		String expression = n.f0.accept(this, argu);
		return expression;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(StmtExp n, String argu) {
		String _ret = null;
		try {
			if (argu != null && argu.equals("function")) {	//Function case ,write begin.
				bw.write("BEGIN \n");

			}
			n.f0.accept(this, argu);
			n.f1.accept(this, null);
			n.f2.accept(this, argu);
			String rtTemp = n.f3.accept(this, null);
			if (argu != null && argu.equals("function")) {
				bw.write("RETURN " + rtTemp + " \n");
				bw.write("END \n");
			}
			n.f4.accept(this, argu);
			return rtTemp;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Call n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String temp1 = n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			String temp2 = n.f3.accept(this, argu);
			String[] splitParameters = temp2.split(",");
			String currentTemp = this.myTemp.newTemp();
			String returnString = "CALL " + temp1 + " ( ";			//Call time in a string.We include the case
			for (int i = 0; i < splitParameters.length; i++) {		//where a function is in a parameter.
				if (splitParameters[i].contains("CALL") == true) { 
					String tmp = this.myTemp.newTemp();	 //If we found a call function as a parameter,we move the result
					bw.write("MOVE " + tmp + " " + splitParameters[i] + " \n"); //to a temporary variables
					splitParameters[i] = tmp;
				}
				returnString = returnString + splitParameters[i] + " ";
			}
			returnString = returnString + " ) ";
			bw.write("MOVE " + currentTemp + " " + returnString + " \n");
			n.f4.accept(this, argu);
			return currentTemp;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeList n, String argu) {
		if (n.size() == 1)
			return n.elementAt(0).accept(this, argu);
		String _ret = null;
		int _count = 0;
		String rtTemp = "";
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
			rtTemp = rtTemp + "," + e.nextElement().accept(this, argu);
			_count++;
		}
		return rtTemp;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeListOptional n, String argu) {
		if (n.present()) {
			if (n.size() == 1)
				return n.elementAt(0).accept(this, argu) + ",";
			String _ret = null;
			int _count = 0;
			String rtTemp = "";
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				rtTemp = rtTemp + ","
						+ e.nextElement().accept(this, "constant");
				_count++;
			}
			rtTemp = rtTemp.substring(1);
			return rtTemp;
		} else
			return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Stmt n, String argu) {
		String stmt = n.f0.accept(this, argu);
		return stmt;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NoOpStmt n, String argu) {
		try {
			bw.append("NOOP \n");
			return n.f0.accept(this, argu);
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ErrorStmt n, String argu) {
		try {
			bw.write("ERROR \n");
		} catch (IOException e) {
			e.getMessage();
		}
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(CJumpStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String expression = n.f1.accept(this, argu);
			String label = n.f2.accept(this, "cjump");
			bw.write("CJUMP " + expression + " " + label + " \n");
			return _ret;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(JumpStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String label = n.f1.accept(this, "jump");
			bw.write("JUMP " + label + " \n");
			return _ret;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HStoreStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String storeTemp = n.f1.accept(this, argu);
			String offset = n.f2.accept(this, "offset");
			String loadTemp = n.f3.accept(this, "hstore");
			bw.write("HSTORE " + storeTemp + " " + offset + " " + loadTemp
					+ " \n");
			return storeTemp;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HLoadStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String loadedTemp = n.f1.accept(this, argu);
			String memory = n.f2.accept(this, argu);
			String offset = n.f3.accept(this, "offset");
			// System.out.println("FILE:HLOAD "+loadedTemp+" "+memory+" "+offset);
			bw.write("HLOAD " + loadedTemp + " " + memory + " " + offset
					+ " \n");
			return loadedTemp;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MoveStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String assignedTemp = n.f1.accept(this, argu);
			String assignmentTemp = n.f2.accept(this, "constant");
			bw.write("MOVE " + assignedTemp + " " + assignmentTemp + " \n");
			return assignedTemp;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PrintStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String rtTemp = n.f1.accept(this, argu);
			bw.write("PRINT " + rtTemp + " \n");
			return _ret;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HAllocate n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String rtTemp = n.f1.accept(this, argu);
		return "HALLOCATE " + rtTemp;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BinOp n, String argu) {
		try {
			String _ret = null;
			String operator = n.f0.accept(this, argu);
			String operand1 = n.f1.accept(this, "constant");
			String operand2 = n.f2.accept(this, "constant");
			String rtType = this.myTemp.newTemp();
			bw.write("MOVE " + rtType + " " + operator + " " + operand1 + " "
					+ operand2 + " \n");
			return rtType;
		} catch (IOException e) {
			e.getMessage();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Operator n, String argu) {
		int returnOperator = n.f0.which;
		if (returnOperator == 0) {
			return "LT ";
		}
		if (returnOperator == 1) {
			return "PLUS ";
		}
		if (returnOperator == 2) {
			return "MINUS ";
		}
		if (returnOperator == 3) {
			return "TIMES ";
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Temp n, String argu) {
		String _ret = null;
		return n.f0.tokenImage.toString() + " " + n.f1.f0.tokenImage.toString();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerLiteral n, String argu) {
		if (argu != null && (argu.equals("constant") || argu.equals("hstore"))) { //In any case(hstore or constant
			String tmp = this.myTemp.newTemp();			//we must put the integer value in a temp variable.
			try {
				bw.write("MOVE " + tmp + " " + n.f0.tokenImage.toString()
						+ " \n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return tmp;
		} else if (argu != null && argu.equals("offset")) {
			return n.f0.tokenImage.toString();

		}
		return n.f0.tokenImage.toString();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Label n, String argu) {
		if (argu != null && argu.equals("hstore")) {
			String returnTemp = this.myTemp.newTemp();
			try {
				bw.write("MOVE " + returnTemp + " "
						+ n.f0.tokenImage.toString() + " \n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return returnTemp;
		} else if (argu != null
				&& (argu.equals("cjump") || argu.equals("jump") || argu
						.equals("function_name"))) {
			return n.f0.tokenImage.toString();

		}
		try {
			bw.write(n.f0.tokenImage.toString() + " ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.tokenImage.toString();
	}

}
