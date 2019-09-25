import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

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
import syntaxtree.NodeListOptional;
import syntaxtree.NodeOptional;
import syntaxtree.Operator;
import syntaxtree.PrintStmt;
import syntaxtree.Procedure;
import syntaxtree.StmtExp;
import syntaxtree.StmtList;
import syntaxtree.Temp;
import visitor.GJDepthFirst;
import controlFlowClasses.functionInfo;

public class KangaVisitor extends GJDepthFirst<String, String> {
	Set<String> registerSet;
	Map<String, String> assignedRegisters;
	BufferedWriter bw;
	controlFlowGraphVisitor cfg;
	int instructionCounter = 1;
	String currentProcedureName;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public KangaVisitor(controlFlowGraphVisitor _cfg, BufferedWriter _bw) {
		this.bw = _bw;
		this.cfg = _cfg;

	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeOptional n, String argu) {
		if (n.present()) {

			String label = n.node.accept(this, argu);
			try {
				bw.write(label + " ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else
			return null;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeListOptional n, String argu) {
		if (n.present()) {
			if (n.size() == 1) {
				if (argu == null) {
					String _ret = "";
					_ret = n.elementAt(0).accept(this, argu);
					return _ret;

				} else {
					if (cfg.deadInstructions.contains(this.instructionCounter) == false) {
						n.elementAt(0).accept(this, argu);
					} 
					else if(cfg.controlFlowGraph.get(this.instructionCounter).isLabel==true){
						try {
							bw.write(cfg.controlFlowGraph.get(this.instructionCounter).label+" NOOP \n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					this.instructionCounter++;
					return "";

				}
			}

			String _ret = "";
			int _count = 0;
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				if (argu == null) {			//Case temp* ,parameters for call.
					_ret = _ret + "," + e.nextElement().accept(this, argu);
					_count++;
				}

				else {
					if (cfg.deadInstructions.contains(this.instructionCounter) == false) {
						e.nextElement().accept(this, argu);
						_count++;
					}
					else if(cfg.controlFlowGraph.get(this.instructionCounter).isLabel==true){
						e.nextElement();
						try {
							bw.write(cfg.controlFlowGraph.get(this.instructionCounter).label+" NOOP \n");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else {
						
						e.nextElement();

					}
					this.instructionCounter++;

				}
			}
			if (_ret.length() != 0) { //returning temp1,temp2,temp3...tempx parameter list.
				_ret = _ret.substring(1);
			}
			return _ret;
		}

		else
			return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Goal n, String argu) {
		String _ret = null;
		try {
			n.f0.accept(this, argu);
			functionInfo mainInfo = cfg.functionalInfo.get("MAIN");
			this.currentProcedureName = "MAIN";
			bw.write("MAIN[0][" + mainInfo.stackUsedParameters + "]["+ mainInfo.maxInsideFunctionParameters + "]\n");
			this.instructionCounter++;
			n.f1.accept(this, argu);
			n.f2.accept(this, argu);
			bw.write("END\n");
			instructionCounter++;
			n.f3.accept(this, argu);
			n.f4.accept(this, argu);
		} catch (IOException e) {

		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Procedure n, String argu) {
		String _ret = null;
		this.currentProcedureName = n.f0.accept(this, argu);
		String buffer = "";
		String buffer2 = "";
		String parameters = n.f2.accept(this, argu);
		int counterForExtraParameters;
		buffer = this.currentProcedureName
				+ "["
				+ n.f2.accept(this, argu)
				+ "]"
				+ "["
				+ Integer.toString(cfg.functionalInfo
						.get(this.currentProcedureName).stackUsedParameters)
				+ "]"
				+ "["
				+ cfg.functionalInfo.get(this.currentProcedureName).maxInsideFunctionParameters
				+ "]\n";

		try {
			bw.write(buffer + buffer2);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		this.instructionCounter++;
		n.f4.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(StmtList n, String argu) {
		return n.f0.accept(this, "notNull");
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public String visit(StmtExp n, String argu) {

		String _ret = null;
		String buffer = "";
		String buffer2 = "";

		n.f0.accept(this, argu);
		int stackUse = cfg.functionalInfo.get(this.currentProcedureName).stackUsedParameters;
		int parameters = cfg.functionalInfo.get(this.currentProcedureName).parameters;
		int tStack = cfg.functionalInfo.get(this.currentProcedureName).tStack;
		// //////////////////////
		if (parameters < 8) {		//parameters less than s registers.
			// //////////////////////////// <=9
			for (int i = 0; i < parameters; i++) {
				int j = i;
				if (parameters > 4) {
					j = 3 + i;
				}
				buffer = buffer + " ASTORE SPILLEDARG " + j + " s" + i + " \n";

			}
			if (parameters != stackUse) {	//means we have some variables stack for s registers.
				// //////////////////////////////
				if (stackUse - parameters - tStack < 8) {
					// /////////////////////////?<=9
					for (int i = parameters; i < stackUse - parameters - tStack
							+ 1; i++) {
						buffer = buffer + " ASTORE SPILLEDARG "
								+ Integer.toString(i) + " " + " s"
								+ Integer.toString(i) + " \n";
					}
				} else {
					for (int i = parameters; i < 8; i++) {
						buffer = buffer + " ASTORE SPILLEDARG "
								+ Integer.toString(i) + " " + " s"
								+ Integer.toString(i) + " \n";
					}

				}
			}
			if (parameters < 4) {	//Simply move a0,a1,a2.. to s0,s1,s2
				for (int i = 0; i < parameters; i++) {

					buffer2 = buffer2 + "MOVE " + "s" + i + " a" + i + "\n";
				}
			} else {	//Else load also from the stack
				for (int i = 0; i < 4; i++) {
					buffer2 = buffer2 + "MOVE " + "s" + i + " a" + i + "\n";
				}
				for (int i = 4; i < parameters; i++) {
					buffer2 = buffer2 + "ALOAD s" + i + " SPILLEDARG "
							+ Integer.toString(i - 4) + "\n";
				}

			}

		} else {
			for (int i = 0; i < 8; i++) { //Store s registers to stack after the last position passarg-1.
				int j = parameters - 4 + i;
				buffer = buffer + " ASTORE SPILLEDARG " + j + " s" + i + " \n";

			}
			for (int i = 0; i < 4; i++) {
				buffer2 = buffer2 + "MOVE " + "s" + i + " a" + i + "\n";
			}
			for (int i = 4; i < 8; i++) { //for 4 an afterwards,load the passrged arguments from stack.
				buffer2 = buffer2 + "ALOAD s" + i + " SPILLEDARG "+ Integer.toString(i - 4) + "\n";
			}
		}
		try {
			bw.write(buffer + buffer2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.instructionCounter++;

		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		String temp = n.f3.accept(this, argu);
		String reg = "";
		String buffer3 = "";
		String buffer4 = "";
		buffer = "";
		reg=temp;
		if (temp.contains("TEMP") && this.cfg.assignedRegisters.containsKey(temp)) {
			reg = cfg.assignedRegisters.get(temp);
			if (reg.contains("SPILLED")) {
				int position = Integer.parseInt(cfg.stackMap.get(
						this.currentProcedureName).get(temp));

				buffer4 = "ALOAD v0 SPILLEDARG " + position + " \n";
				reg = "v0";
			}
		}
		buffer3 = "MOVE v0 " + reg + " \n";

		if (parameters < 8) {
			for (int i = 0; i < parameters; i++) {
				int j = i;
				if (parameters - 4 > 0) {
					j = 3 + i;
				}
				buffer = buffer + " ALOAD s" + i + " " + " SPILLEDARG " + j
						+ " \n";

			}
			if (parameters != stackUse) {
				if (stackUse - parameters - tStack < 8) {
					for (int i = parameters; i < stackUse - parameters - tStack
							+ 1; i++) {
						buffer = buffer + " ALOAD  s" + Integer.toString(i)
								+ " SPILLEDARG " + Integer.toString(i) + " "
								+ " \n";
					}
				} else {
					for (int i = parameters; i < 8; i++) {
						buffer = buffer + " ALOAD s" + Integer.toString(i)
								+ " SPILLEDARG " + Integer.toString(i) + " "
								+ " \n";
					}

				}
			}
		} else {
			for (int i = 0; i < 8; i++) {
				int j = parameters - 4 + i;

				buffer = buffer + " ALOAD s" + i + " SPILLEDARG " + j + " \n";

			}
		}
		buffer3 = buffer3 + buffer + "END\n";
		try {
			bw.write(buffer4 + buffer3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		this.instructionCounter++;
		n.f4.accept(this, argu);
		this.instructionCounter++;
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MoveStmt n, String argu) {
		try {
			String _ret = null;
			n.f0.accept(this, argu);
			String buffer = "";
			String buffer2 = "";
			String buffer3 = "";
			String temp = n.f1.accept(this, argu);
			String reg = cfg.assignedRegisters.get(temp);
			if (reg.equals("SPILLED")) {
				int position = Integer.parseInt(cfg.stackMap.get(
						this.currentProcedureName).get(temp));
				buffer3 = "ASTORE SPILLEDARG " + position + " v1 \n";
				reg = "v1";

			}
			buffer = buffer + "MOVE " + reg + " ";
			String exp = n.f2.accept(this, argu);
			if (exp != null && exp.contains("TEMP ") && cfg.assignedRegisters.containsKey(exp) == true) {
				String reg2 = cfg.assignedRegisters.get(exp);
				if (reg2.equals("SPILLED") == true) {
					int position = Integer.parseInt(cfg.stackMap.get(
							this.currentProcedureName).get(exp));
					buffer2 = "ALOAD v1 SPILLEDARG " + position + "\n";
					exp = "v1 \n";

				} else {
					exp = reg2 + "\n";
				}

			}

			buffer = buffer + exp + " ";
			bw.write(buffer2 + buffer + buffer3 + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Exp n, String argu) {
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HAllocate n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String simpleExp = n.f1.accept(this, argu);
		if (simpleExp.contains("TEMP ") == true) {
			String reg = cfg.assignedRegisters.get(simpleExp);
			if (reg.equals("SPILLED") == true) {
				int position = Integer.parseInt(cfg.stackMap.get(
						this.currentProcedureName).get(simpleExp));
				try {
					bw.write("ALOAD v1 SPILLEDARG " + position + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				simpleExp = "v1 ";

			} else {
				simpleExp = reg;
			}
		}
		return "HALLOCATE " + simpleExp + " ";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BinOp n, String argu) {
		String _ret = null;
		String buffer = "";
		String buffer2 = "";

		String operator = n.f0.accept(this, argu);
		String temp = n.f1.accept(this, argu);
		String reg = cfg.assignedRegisters.get(temp);
		if (reg.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(temp));
			buffer2 = "ALOAD v1 SPILLEDARG " + position + "\n";
			reg = "v1";

		}

		String simpleExp = n.f2.accept(this, argu);
		if (simpleExp.contains("TEMP ")) {
			String reg2 = cfg.assignedRegisters.get(simpleExp);
			if (reg2.equals("SPILLED")) {
				int position = Integer.parseInt(cfg.stackMap.get(
						this.currentProcedureName).get(simpleExp));
				buffer2 = buffer2 + "ALOAD v0 SPILLEDARG " + position + "\n";
				simpleExp = "v0";

			} else {
				simpleExp = reg2;
			}
		}
		try {
			bw.write(buffer2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return operator + " " + reg + " " + simpleExp + " " + "\n";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NoOpStmt n, String argu) {
		try {
			bw.write("NOOP\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ErrorStmt n, String argu) {
		try {
			bw.write("ERROR\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(CJumpStmt n, String argu) {
		String _ret = null;
		String buffer = "";
		String buffer2 = "";
		n.f0.accept(this, argu);
		String temp = n.f1.accept(this, argu);
		String reg = cfg.assignedRegisters.get(temp);
		if (reg.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(temp));
			buffer2 = "ALOAD v1 SPILLEDARG " + position + "\n";
			reg = "v1";

		}
		String label = n.f2.accept(this, argu);

		try {
			bw.write("CJUMP " + reg + " " + label + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(JumpStmt n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String label = n.f1.accept(this, argu);
		try {
			bw.write("JUMP " + label + " \n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HStoreStmt n, String argu) {
		String _ret = null;
		String buffer = "";
		String buffer2 = "";
		String reg1 = n.f1.accept(this, argu);
		String reg2 = n.f3.accept(this, argu);
		String offset = n.f2.accept(this, argu);
		String reg3 = cfg.assignedRegisters.get(reg1);
		if (reg3.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(reg1));
			buffer2 = "ALOAD v1 SPILLEDARG " + position + "\n";
			reg1 = "v1";

		} else {
			reg1 = reg3;
		}
		reg3 = cfg.assignedRegisters.get(reg2);
		if (reg3.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(reg2));
			buffer2 = "ALOAD v0 SPILLEDARG " + position + "\n";
			reg2 = "v0";

		} else {
			reg2 = reg3;
		}
		buffer = "HSTORE " + reg1 + " " + offset + " " + reg2 + " " + "\n";
		try {
			bw.write(buffer2 + buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PrintStmt n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String temp = n.f1.accept(this, argu);
		String buffer = "";
		String buffer2 = "";
		String reg1 = temp;
		if (temp.contains("TEMP ")) {
			reg1 = cfg.assignedRegisters.get(temp);
			if (reg1.equals("SPILLED")) {
				int position = Integer.parseInt(cfg.stackMap.get(
						this.currentProcedureName).get(temp));
				buffer2 = "ALOAD v1 SPILLEDARG " + position + "\n";
				reg1 = "v1";

			}
		}
		buffer = "PRINT " + reg1 + "\n";
		try {
			bw.write(buffer2 + buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HLoadStmt n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String buffer = "";
		String buffer2 = "";
		String buffer3 = "";
		String reg1 = n.f1.accept(this, argu);
		String reg2 = n.f2.accept(this, argu);
		String offset = n.f3.accept(this, argu);
		String reg3 = cfg.assignedRegisters.get(reg1);
		if (reg3.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(reg1));
			buffer2 = "ASTORE SPILLEDARG " + position + " v1 \n";
			reg1 = "v1";

		} else {
			reg1 = reg3;
		}
		reg3 = cfg.assignedRegisters.get(reg2);
		if (reg3.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(reg2));
			buffer3 = "ALOAD v1 SPILLEDARG " + position + "\n";
			reg2 = "v1";

		} else {
			reg2 = reg3;
		}
		buffer = "HLOAD " + reg1 + " " + reg2 + " " + offset + " " + "\n";
		try {
			bw.write(buffer3 + buffer + buffer2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _ret;
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

	public String visit(Call n, String argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		String buffer = "";
		String buffer2 = "";
		String simpleExp = n.f1.accept(this, argu);
		String reg = cfg.assignedRegisters.get(simpleExp);
		if (reg.equals("SPILLED")) {
			int position = Integer.parseInt(cfg.stackMap.get(
					this.currentProcedureName).get(simpleExp));
			buffer = "ALOAD v1 SPILLEDARG " + position + "\n";
			reg = "v1";
		}

		buffer = "CALL " + reg + " \n";
		n.f2.accept(this, argu);
		String params = n.f3.accept(this, null);
		String[] arrParams = params.split(",");
		Set<String> tempSet = cfg.controlFlowGraph.get(this.instructionCounter).outSet;
		int counter = 1;
		String bufferFortBefore = "";
		String bufferFortAfter = "";
		for (String s : tempSet) {
			String register = this.cfg.assignedRegisters.get(s);
			if (register != null && register.contains("t")) {
				int position = this.cfg.functionalInfo
						.get(this.currentProcedureName).stackUsedParameters
						- counter;
				counter++;
				bufferFortBefore = bufferFortBefore + "ASTORE SPILLEDARG "
						+ position + " " + register + " \n";
				bufferFortAfter = bufferFortAfter + "ALOAD " + register
						+ " SPILLEDARG " + position + " \n";

			}

		}
		if (arrParams.length > 4) {
			for (int i = 0; i < 4; i++) {
				reg = cfg.assignedRegisters.get(arrParams[i]);
				if (reg.contains("SPILLED")) {
					buffer2 = buffer2
							+ "ALOAD v1 SPILLEDARG "
							+ cfg.stackMap.get(this.currentProcedureName).get(
									arrParams[i]) + " \n";
					reg = "v1";
				}
				buffer2 = buffer2 + " MOVE a" + i + " " + reg + "\n";

			}
			for (int i = 4; i < arrParams.length; i++) {
				reg = cfg.assignedRegisters.get(arrParams[i]);

				if (reg.contains("SPILLED")) {
					buffer2 = buffer2
							+ "ALOAD v1 SPILLEDARG "
							+ cfg.stackMap.get(this.currentProcedureName).get(
									arrParams[i]) + " \n";
					reg = "v1";
				}
				buffer2 = buffer2 + " PASSARG " + Integer.toString(i - 3) + " "
						+ reg + "\n";

			}
		} else {
			for (int i = 0; i < arrParams.length; i++) {
				reg = cfg.assignedRegisters.get(arrParams[i]);
				if (reg.contains("SPILLED")) {
					buffer2 = buffer2
							+ "ALOAD v1 SPILLEDARG "
							+ cfg.stackMap.get(this.currentProcedureName).get(
									arrParams[i]) + " \n";
					reg = "v1";
				}
				buffer2 = buffer2 + " MOVE a" + i + " " + reg + "\n";
			}
		}
		try {
			bw.write(buffer2 + bufferFortBefore + buffer + bufferFortAfter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		n.f4.accept(this, argu);
		return "v0";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Temp n, String argu) {
		String _ret = null;
		String temp = n.f0.tokenImage.toString();
		String integer = n.f1.f0.toString();
		return temp + " " + integer;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerLiteral n, String argu) {
		return n.f0.tokenImage.toString();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Label n, String argu) {
		return n.f0.tokenImage.toString();
	}
}
