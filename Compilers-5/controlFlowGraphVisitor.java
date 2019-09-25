import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import syntaxtree.BinOp;
import syntaxtree.CJumpStmt;
import syntaxtree.Call;
import syntaxtree.ErrorStmt;
import syntaxtree.Exp;
import syntaxtree.Goal;
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
import syntaxtree.PrintStmt;
import syntaxtree.Procedure;
import syntaxtree.SimpleExp;
import syntaxtree.StmtExp;
import syntaxtree.StmtList;
import syntaxtree.Temp;
import visitor.GJDepthFirst;
import controlFlowClasses.functionInfo;
import controlFlowClasses.instructionNode;

public class controlFlowGraphVisitor extends GJDepthFirst<String, instructionNode> {
	Map<Integer, instructionNode> controlFlowGraph;  //Flow graph for instructions.
	Map<String, Integer> labelInstructions;  //Instructions that they have a label before.
	Map<String, Integer> awaitingLabels;	//Label found,and awaiting the instruction(CJUMP,JUMP) referring
	Map<String, Integer> whileLabels;		//to them.
	Map<String, String> Stack = new LinkedHashMap<String, String>();  //Stack for algorithm.
	Set<String> candidatesTospill = new HashSet<String>(); //Candidates to spill.
	Set<Integer> deadInstructions = new HashSet<Integer>(); //Set with dead instruction,containing their number.
	
	ArrayList tRegisters = new ArrayList(Arrays.asList("t0", "t1", "t2", "t3","t4", "t5", "t6", "t7", "t8", "t9"));
	ArrayList sRegisters = new ArrayList(Arrays.asList("s0", "s1", "s2", "s3","s4", "s5", "s6", "s7"));
	
	Map<String, Map<String, String>> stackMap = new HashMap<String, Map<String, String>>(); //Map for the stack
	Map<String, String> assignedRegisters = new HashMap<String, String>();					//of every function.
	public Map<String, Set<String>> Graph;		//Graph containing variables and it's neighbors.
	public Map<String, functionInfo> functionalInfo;	//Info for the class(Parameters,stack usage,max inside function parameters.
	public Map<String, Set<Integer>> andOrMap = new HashMap<String, Set<Integer>>();  //Case there is && .
	public String currentLabel=null;
	boolean whileCase = false;
	boolean isCJUMP = false;
	boolean isJUMP = false;
	boolean isAndOr = false;
	
	String currClassName;
	String andOrLabel = null;
	boolean labelBeforeInstr = false;
	int labelPreviousInstr;
	int instructionCounter = 1;
	Set<Integer> callInstruction = new HashSet<Integer>();  //Case for call instruction.We need to know it
	String currentProcedure = null;							//so we can store t variables,if needed.
	Map<String, String> varProc = new HashMap<String, String>(); //Variable and in which class it belongs.
	public boolean isFunction = false;
	public int maxArgs = 0;
	public functionInfo currentFI;
	int numOfArgs;
	Set<String> bettersRegisters = new HashSet<String>();
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public controlFlowGraphVisitor() {
		this.controlFlowGraph = new HashMap<Integer, instructionNode>();
		this.awaitingLabels = new HashMap<String, Integer>();
		this.labelInstructions = new HashMap<String, Integer>();
		this.whileLabels = new HashMap<String, Integer>();
		this.Graph = new HashMap<String, Set<String>>();
		this.functionalInfo = new HashMap<String, functionInfo>();
		this.varProc = new HashMap<String, String>();
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NodeOptional n, instructionNode argu) {
		if (n.present()) {
			this.labelBeforeInstr = true;  //We encountered an instruction that has a label before.
			return n.node.accept(this, argu);
		} else
			return null;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String visit(NodeListOptional n, instructionNode argu) {
		if (n.present()) {
			if (n.size() == 1) {
				String loco = n.elementAt(0).accept(this, argu);
				if (loco != null && loco.contains("TEMP ")) { //We encounter a temp variable.Add id in the 
					argu.useSet.add(loco);   					//useSet.
				}
				return "1";
			}
			String _ret = null;
			int _count = 0;
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
				String loco = e.nextElement().accept(this, argu);
				if (loco != null && loco.contains("TEMP ")) {
					argu.useSet.add(loco);
				} else {
					this.labelBeforeInstr = false;
				}
				_count++;
			}
			return Integer.toString(_count);
		} else
			return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Goal n, instructionNode argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		instructionNode current = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			current.label=this.currentLabel;
			current.isLabel=true;
			this.currentLabel=null;
		}
		currClassName = "MAIN";
		currentFI = new functionInfo();
		currentFI.stackUsedParameters = 0;
		current.previous = null;
		this.controlFlowGraph.put(instructionCounter, current);
		instructionCounter++;
		n.f1.accept(this, current);
		n.f2.accept(this, argu);
		this.functionalInfo.put("MAIN", currentFI);
		instructionNode current2 = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			current2.label=this.currentLabel;
			this.controlFlowGraph.get(this.instructionCounter).isLabel=true;
			this.currentLabel=null;
		}
		current2.next = null;
		instructionNode previous = this.controlFlowGraph.get(this.instructionCounter - 1);
		previous.next = current2;
		current2.previous = previous;
		this.controlFlowGraph.put(this.instructionCounter, current2);
		this.instructionCounter++;
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Procedure n, instructionNode argu) {
		String _ret = null;
		this.isFunction = true;
		instructionNode curr = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		this.currentFI = new functionInfo();
		curr.isFunction = true;
		curr.previous = null;
		this.controlFlowGraph.put(instructionCounter, curr);
		currClassName = n.f0.accept(this, argu);

		n.f1.accept(this, argu);
		numOfArgs = Integer.parseInt(n.f2.accept(this, argu));
		Map<String, String> tempParams = new HashMap<String, String>();
		if (numOfArgs < 9) {  //Num of args less than all the s registers we have(8).
			this.currentFI.stackUsedParameters = numOfArgs;
			if (numOfArgs <= 4) {		//Check whether we will put something in the stack
				this.currentFI.stackUsedParameters = numOfArgs;
				for (int i = 0; i < numOfArgs; i++) {
					tempParams.put("TEMP " + Integer.toString(i),Integer.toString(i));
					this.assignedRegisters.put("TEMP " + Integer.toString(i),"s" + Integer.toString(i));

				}
			} 
			else {
				this.currentFI.stackUsedParameters = 4 + Integer.parseInt(n.f2.f0.tokenImage.toString());
				//Case where the number of args need to be stored and retrieved in stack.(PASSARG CASE).
				for (int i = 4; i < numOfArgs; i++) {

					tempParams.put("TEMP " + Integer.toString(i),Integer.toString(i - 4));
					this.assignedRegisters.put("TEMP " + Integer.toString(i),"s" + Integer.toString(i));

				}
				for (int i = 0; i < 4; i++) {
					tempParams.put("TEMP " + Integer.toString(i),Integer.toString(i));
					this.assignedRegisters.put("TEMP " + Integer.toString(i),"s" + Integer.toString(i));

				}
			}
		} else {
			for (int i = 0; i < 8; i++) {		//s0-s7 are the parameters.
				tempParams.put(Integer.toString(i),"TEMP " + Integer.toString(i));
				this.assignedRegisters.put("TEMP " + Integer.toString(i), "s"+ Integer.toString(i));

			}
			int max = 0;
			for (int i = 8; i < numOfArgs; i++) { //9th variables and so stored into stack.
				tempParams.put("TEMP " + Integer.toString(i),Integer.toString(5 + i - 9));
				this.assignedRegisters.put("TEMP " + Integer.toString(i),"SPILLED");
				max++;

			}
			this.currentFI.stackUsedParameters = 11 + max + 1;  //Compute stack space.

		}
		this.stackMap.put(currClassName, tempParams);	//put in stackMap.
		this.currentFI.parameters = numOfArgs;
		n.f3.accept(this, argu);
		this.functionalInfo.put(currClassName, currentFI);
		this.instructionCounter++;
		n.f4.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(StmtExp n, instructionNode argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		instructionNode curr = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		this.controlFlowGraph.put(instructionCounter, curr);
		instructionCounter++;
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		instructionNode currReturn = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			currReturn.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		currReturn.next = null;
		n.f3.accept(this, currReturn);
		this.chooseNextPrev(currReturn);
		this.controlFlowGraph.put(this.instructionCounter, currReturn);
		this.instructionCounter++;
		instructionNode current2 = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			current2.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		current2.next = null;
		instructionNode previous = this.controlFlowGraph.get(this.instructionCounter - 1);
		previous.next = current2;
		current2.previous = previous;
		this.controlFlowGraph.put(this.instructionCounter, current2);
		this.instructionCounter++;
		n.f4.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(StmtList n, instructionNode argu) {
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(CJumpStmt n, instructionNode argu) {
		String _ret = null;
		this.isCJUMP = true;
		instructionNode prev;
		instructionNode current = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			current.label=this.currentLabel;
			current.isLabel=true;
			this.currentLabel=null;
		}
		current.useSet.add(n.f1.accept(this, argu));
		prev = this.controlFlowGraph.get(this.instructionCounter - 1);
		this.setPrevNext(prev, current);
		this.controlFlowGraph.put(instructionCounter, current);
		String label = n.f2.accept(this, argu);
		if (this.labelInstructions.containsKey(label) == true) { //Case of &&.Double reference to the same label.
			int position1 = this.labelInstructions.get(label);
			this.labelInstructions.remove(label);
			Set<Integer> positions = new HashSet<Integer>();
			positions.add(position1);
			positions.add(this.instructionCounter);
			this.andOrMap.put(label, positions);

		} 
		else {
			this.labelInstructions.put(label, instructionCounter); //First reference to a label.
		}
		instructionCounter++;
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(JumpStmt n, instructionNode argu) {
		n.f0.accept(this, argu);
		String label = n.f1.accept(this, argu);
		if (this.labelInstructions != null && this.labelInstructions.containsKey(label)) {  //Already visited label.
			instructionNode next = this.controlFlowGraph.get(this.labelInstructions.get(label));
			instructionNode curr = new instructionNode(this.instructionCounter);
			if(this.currentLabel!=null){
				curr.label=this.currentLabel;
				curr.isLabel=true;
				this.currentLabel=null;
			}
			curr.next = next;
			next.prevJump = curr;
			this.chooseNextPrev(curr);
			this.controlFlowGraph.put(instructionCounter, curr);
		} else {
			this.awaitingLabels.put(label, instructionCounter); //About to visit the label.
			instructionNode curr = new instructionNode(this.instructionCounter);
			if(this.currentLabel!=null){
				curr.label=this.currentLabel;
				curr.isLabel=true;
				this.currentLabel=null;
			}
			instructionNode prev = this.controlFlowGraph.get(instructionCounter - 1);
			curr.previous = prev;
			prev.next = curr;
			this.controlFlowGraph.put(instructionCounter, curr);

		}
		this.instructionCounter++;
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(NoOpStmt n, instructionNode argu) {
		instructionNode curr = new instructionNode(this.instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		this.chooseNextPrev(curr);
		this.controlFlowGraph.put(this.instructionCounter, curr);
		this.instructionCounter++;
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(ErrorStmt n, instructionNode argu) {
		instructionNode curr = new instructionNode(this.instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		this.chooseNextPrev(curr);
		this.controlFlowGraph.put(this.instructionCounter, curr);
		this.instructionCounter++;
		return n.f0.accept(this, argu);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HStoreStmt n, instructionNode argu) {
		String _ret = null;
		instructionNode curr = new instructionNode(this.instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		n.f0.accept(this, argu);
		curr.useSet.add(n.f1.accept(this, argu));
		n.f2.accept(this, argu);
		curr.useSet.add(n.f3.accept(this, argu));
		this.chooseNextPrev(curr);
		this.controlFlowGraph.put(this.instructionCounter, curr);
		this.instructionCounter++;
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(HLoadStmt n, instructionNode argu) {
		String _ret = null;
		instructionNode curr = new instructionNode(this.instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		n.f0.accept(this, argu);
		curr.defSet.add(n.f1.accept(this, argu));
		curr.useSet.add(n.f2.accept(this, argu));
		n.f3.accept(this, argu);
		this.chooseNextPrev(curr);
		this.controlFlowGraph.put(this.instructionCounter, curr);
		this.instructionCounter++;
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(MoveStmt n, instructionNode argu) {
		String _ret = null;
		instructionNode curr = new instructionNode(this.instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		n.f0.accept(this, argu);
		curr.defSet.add(n.f1.accept(this, argu));
		n.f2.accept(this, curr);
		this.chooseNextPrev(curr);
		this.controlFlowGraph.put(this.instructionCounter, curr);
		this.instructionCounter++;
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Exp n, instructionNode argu) {
		if (n.f0.accept(this, argu) != null && n.f0.accept(this, argu).contains("TEMP ")) {
			argu.useSet.add(n.f0.accept(this, argu));
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Call n, instructionNode argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		this.callInstruction.add(this.instructionCounter);
		String currentNumberOfArgs = n.f3.accept(this, argu);
		if (Integer.parseInt(currentNumberOfArgs) > this.currentFI.maxInsideFunctionParameters) {
			this.currentFI.maxInsideFunctionParameters = Integer.parseInt(currentNumberOfArgs);
		}
		n.f4.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(BinOp n, instructionNode argu) {
		String _ret = null;
		n.f0.accept(this, argu);
		argu.useSet.add(n.f1.accept(this, argu));
		n.f2.accept(this, argu);
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(SimpleExp n, instructionNode argu) {
		if (argu != null && n.f0.accept(this, argu) != null && n.f0.accept(this, argu).contains("TEMP ")) {
			argu.useSet.add(n.f0.accept(this, argu));
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(PrintStmt n, instructionNode argu) {
		String _ret = null;
		instructionNode curr = new instructionNode(instructionCounter);
		if(this.currentLabel!=null){
			curr.label=this.currentLabel;
			curr.isLabel=true;
			this.currentLabel=null;
		}
		n.f0.accept(this, argu);
		n.f1.accept(this, curr);
		this.chooseNextPrev(curr);
		this.controlFlowGraph.put(instructionCounter, curr);
		this.instructionCounter++;
		return _ret;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Temp n, instructionNode argu) {
		String temp;
		temp = n.f0.tokenImage.toString() + " " + n.f1.f0.tokenImage.toString();
		if (this.assignedRegisters.containsKey(temp) == false) {
			this.varProc.put(temp, this.currClassName);
		}
		return temp;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(Label n, instructionNode argu) {
		this.currentLabel=n.f0.tokenImage.toString();
		if (this.labelBeforeInstr == true) {
			if (this.andOrMap.containsKey(n.f0.tokenImage.toString()) == true) {  //Label Telling the remaining 
				this.isAndOr = true;											  //of instruction that it is 
				this.andOrLabel = n.f0.tokenImage.toString();					//and or instruction.
			}
			if (this.labelInstructions != null && this.labelInstructions.containsKey(n.f0.tokenImage.toString()) == true) {
				//Remaining of the instruction is a cjump true path.
				this.labelPreviousInstr = this.labelInstructions.get(n.f0.tokenImage.toString());
				this.isCJUMP = true;
			} else if (this.awaitingLabels != null && this.awaitingLabels.containsKey(n.f0.tokenImage.toString()) == true) {
				//Remaining of the instruction is a cjump true path.
				this.isJUMP = true;
				this.labelPreviousInstr = this.awaitingLabels.get(n.f0.tokenImage.toString());

			} else {
				this.whileCase = true;
				this.labelInstructions.put(n.f0.tokenImage.toString(),instructionCounter);

			}
		}
		return n.f0.tokenImage.toString();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String visit(IntegerLiteral n, instructionNode argu) {
		return n.f0.tokenImage.toString();
	}

	public void setPrevNext(instructionNode prev, instructionNode curr) {  //Simple set previous-next of instruction.
		curr.previous = prev;
		prev.next = curr;

	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setPrevNext2path(instructionNode prev, instructionNode curr,	
			int pathChoice) {			//Two path next-previous.
		if (pathChoice == 1) {
			prev.twoPathNext.nextFalse = curr;
			curr.previous = prev;
		} else {
			prev.twoPathNext.nextTrue = curr;
			curr.previous = prev;

		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void chooseNextPrev(instructionNode curr) {
		instructionNode prev;
		if (this.isAndOr == true) {
			Set<Integer> tempSet = this.andOrMap.get(this.andOrLabel);
			int counter = 0;
			for (Integer e : tempSet) {  //Previous-1,Previous-2 from andOrMap Set,previous-previous instruction.
				if (counter == 0) {
					instructionNode prev0 = this.controlFlowGraph.get(e);
					prev0.twoPathNext.nextTrue = curr;
					curr.twoPathPrevious.previousTrue = prev0;
					counter++;
				} else {
					instructionNode prev1 = this.controlFlowGraph.get(e);

					prev1.twoPathNext.nextFalse = curr;
					curr.twoPathPrevious.previousTrue = prev1;

				}
			}
			instructionNode prev3 = this.controlFlowGraph.get(this.instructionCounter - 1);
			prev3.next = curr;
			curr.previous = prev3;
			this.isAndOr = false;

		} else if (this.isCJUMP == true && this.labelBeforeInstr == false) { //Previous of cjump,is previous instr.
			this.isCJUMP = false;											//This is the false path after a cjump.
			prev = this.controlFlowGraph.get(this.instructionCounter - 1); 
			this.setPrevNext2path(prev, curr, 1);
		} else if (this.labelBeforeInstr == true) {
			if (this.isCJUMP == true) {
				prev = this.controlFlowGraph.get(this.labelPreviousInstr);
				this.setPrevNext2path(prev, curr, 2);
				this.isCJUMP = false;
			} else if (this.isJUMP == true) {
				curr.prevJump = this.controlFlowGraph.get(this.labelPreviousInstr);
				curr.prevJump.next = curr;
				prev = this.controlFlowGraph.get(instructionCounter - 1);
				this.setPrevNext(prev, curr);
				this.isJUMP = false;
			} else if (this.whileCase == true) {
				curr.previous = this.controlFlowGraph.get(this.instructionCounter - 1);
				instructionNode prev2 = this.controlFlowGraph.get(this.instructionCounter - 1);
				prev2.next = curr;
				this.whileCase = false;

			}
		} else {
			prev = this.controlFlowGraph.get(this.instructionCounter - 1);
			this.setPrevNext(prev, curr);

		}

	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void computeInOutSets() {  //Computing in and out sets.
		int i;
		boolean notChanged = false;
		int counter = 0;
		while (notChanged == false) {
			notChanged = true;
			counter++;
			for (i = this.instructionCounter - 1; i > 0; i--) {
				Set<String> tmpOut = new HashSet<String>();
				Set<String> tmpIn = new HashSet<String>();
				instructionNode temp = this.controlFlowGraph.get(i);
				if (temp.next != null) {
					if (temp.next.inSet.size() != 0) {
						tmpOut.addAll(temp.next.inSet);
					}
				}
				if (temp.twoPathNext.nextFalse != null) {
					if (temp.twoPathNext.nextFalse.inSet.size() != 0) {
						tmpOut.addAll(temp.twoPathNext.nextFalse.inSet);
					}
				}
				if (temp.twoPathNext.nextTrue != null) {
					if (temp.twoPathNext.nextTrue.inSet.size() != 0) {
						tmpOut.addAll(temp.twoPathNext.nextTrue.inSet);
					}
				}
				Set<String> temporaryOutSet = new HashSet<String>(tmpOut);
				for (String e : temp.defSet) {
					temporaryOutSet.remove(e);
				}
				if (temp.useSet.size() != 0) {
					temporaryOutSet.addAll(temp.useSet);
				}
				if (temporaryOutSet.size() != 0) {
					tmpIn.addAll(temporaryOutSet);
				}
				Set<String> in = new HashSet<String>(tmpIn);
				Set<String> out = new HashSet<String>(tmpOut);
				in.removeAll(temp.inSet);
				out.removeAll(temp.outSet);
				if (in.size() != 0) {
					notChanged = false;
					temp.inSet = new HashSet<String>(tmpIn);
				}
				if (out.size() != 0) {
					notChanged = false;
					temp.liveSet = new HashSet<String>(tmpOut);
					for (String e : temp.defSet) {
						if (tmpOut.contains(e) == true) {
							temp.liveSet.add(e);
						}
					}
					temp.outSet = new HashSet<String>(tmpOut);
				}

			}

		}
		for (i = this.instructionCounter - 1; i > 0; i--) {
			if (this.deadInstructions.contains(i) == false) { //We exclude dead instructions when we compute the
				if (this.callInstruction.contains(i)) {		//interference graph.
					this.bettersRegisters.addAll(this.controlFlowGraph.get(i).outSet);
				}
				instructionNode temp = this.controlFlowGraph.get(i);
				for (String e : temp.defSet) {
					if (temp.outSet.contains(e) == false) {
						this.deadInstructions.add(i);

					}
				}
				
				Set<String> tmp = new HashSet<String>(temp.liveSet);
				Set<String> loco;
				for (String e : tmp) {
					loco = new HashSet<String>(tmp);
					loco.remove(e);
					if (this.Graph.containsKey(e) == true) {
						Set<String> previousVertices = new HashSet<String>();
						previousVertices.addAll(this.Graph.get(e));
						previousVertices.addAll(loco);
						this.Graph.put(e, previousVertices);

					} else {
						Set<String> vertices = new HashSet<String>();
						vertices.addAll(loco);
						this.Graph.put(e, vertices);
					}
					tmp.add(e);
				}
			}
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void graphColouring() {
		Stack<String> algorithmStack = new Stack<String>();
		Map<String, String> tempAR = new HashMap<String, String>(assignedRegisters);
		Map<String, String> tempAR3 = new HashMap<String, String>(assignedRegisters);

		boolean coloured = false;
		while (coloured == false) {
			algorithmStack = this.graphColouringAlgorithm();
			boolean foundRegister = true;
			while (algorithmStack.isEmpty() == false && foundRegister == true) {
				ArrayList<String> tempList = new ArrayList(this.tRegisters);
				String node = algorithmStack.pop();
				if (this.assignedRegisters.containsKey(node) == false) {
					Set<String> neighbors = this.Graph.get(node);
					if (this.bettersRegisters.contains(node) == false) { //Registers we better assign them an
						for (String e : neighbors) {					//s register(registers in a call that will 
							if (assignedRegisters.containsKey(e)) {		//be used afterwards).
								tempList.remove(assignedRegisters.get(e));

							}
						}
						if (tempList.size() != 0) {
							this.assignedRegisters.put(node, tempList.get(0));
							tempList.remove(0);
						}

						else {
							ArrayList<String> tempList2 = new ArrayList(this.sRegisters);
							for (String e : neighbors) {
								if (assignedRegisters.containsKey(e)) {
									tempList2.remove(assignedRegisters.get(e));
								}
							}
							if (tempList2.size() != 0) {
								this.assignedRegisters.put(node,
										tempList2.get(0));
							} else {   //Could not assign the variable.
								foundRegister = false;
								this.assignedRegisters = new HashMap<String, String>(tempAR3);
								Map<String, String> temporary = this.stackMap.get(this.varProc.get(node));
								if (temporary != null) {
									temporary.put(node,Integer.toString(this.functionalInfo.get(this.varProc.get(node)).stackUsedParameters));
									this.functionalInfo.get(this.varProc.get(node)).stackUsedParameters++;

								} else {
									temporary = new HashMap<String, String>();
									temporary.put(node, Integer.toString(temporary.size() + 1));

								}
								this.stackMap.put(this.varProc.get(node),temporary);
								this.assignedRegisters.put(node, "SPILLED");
								neighbors = this.Graph.get(node);
								for (String neighborName : neighbors) {
									Set<String> myTemp = new HashSet<String>(this.Graph.get(neighborName));
									myTemp.remove(node);
									this.Graph.put(neighborName, myTemp);
								}
								tempAR3.put(node, "SPILLED");
								this.Graph.remove(node);

							}
						}
					} else {
						tempList = new ArrayList<String>(this.sRegisters);
						for (String e : neighbors) {
							if (assignedRegisters.containsKey(e)) {
								tempList.remove(assignedRegisters.get(e));
							}
						}
						if (tempList.size() != 0) {
							this.assignedRegisters.put(node, tempList.get(0));
							tempList.remove(0);
						}

						else {
							ArrayList<String> tempList2 = new ArrayList(this.tRegisters);
							for (String e : neighbors) {
								if (assignedRegisters.containsKey(e)) {
									tempList2.remove(assignedRegisters.get(e));
								}
							}
							if (tempList2.size() != 0) {
								this.assignedRegisters.put(node,tempList2.get(0));
							} else {
								foundRegister = false;
								this.assignedRegisters = new HashMap<String, String>(tempAR3);
								Map<String, String> temporary = this.stackMap.get(this.varProc.get(node));
								if (temporary != null) {
									temporary.put(node,Integer.toString(this.functionalInfo.get(this.varProc.get(node)).stackUsedParameters));
									this.functionalInfo.get(this.varProc.get(node)).stackUsedParameters++;
								} else {
									temporary = new HashMap<String, String>();
									temporary.put(node,Integer.toString(this.functionalInfo.get(this.varProc.get(node)).stackUsedParameters));
									this.functionalInfo.get(this.varProc.get(node)).stackUsedParameters++;

								}
								this.stackMap.put(this.varProc.get(node),temporary);
								this.assignedRegisters.put(node, "SPILLED");
								neighbors = this.Graph.get(node);
								for (String neighborName : neighbors) {
									Set<String> myTemp = new HashSet<String>(this.Graph.get(neighborName));
									myTemp.remove(node);
									this.Graph.put(neighborName, myTemp);
								}
								tempAR3.put(node, "SPILLED");
								this.Graph.remove(node);

							}

						}
					}
				}

			}
			if (foundRegister == true) {  //If we found a register to assign.
				coloured = true;
				Map<String, String> tempAR2 = new HashMap<String, String>(this.assignedRegisters); //Remove already assigned vars
				for (Map.Entry<String, String> entry : tempAR.entrySet()) {				//from the parameters of functions.
					tempAR2.remove(entry.getKey());
				}
				Map<String, Set<String>> visitedString = new HashMap<String, Set<String>>();
				for (Map.Entry<String, String> entry : tempAR2.entrySet()) {
					if (entry.getValue().contains("s") == true) {  //s register,check if we already put it in 
						String className = this.varProc.get(entry.getKey());	// stack,otherwise inc stack
						if (visitedString.containsKey(className) == true) {		//size.
							Set<String> tempNames = visitedString.get(className);
							if (tempNames.contains(entry.getValue()) == false) {
								this.functionalInfo.get(className).stackUsedParameters++;
								tempNames.add(entry.getValue());
								visitedString.put(className, tempNames);

							} 
						} else {
							Set<String> tempNames = new HashSet<String>();
							tempNames.add(entry.getValue());
							visitedString.put(className, tempNames);
							this.functionalInfo.get(className).stackUsedParameters++;
							
						}
						}
				}
				for (Map.Entry<String, String> entry : tempAR2.entrySet()) {
					if (entry.getValue().contains("SPILLED") == true) {
						String className = this.varProc.get(entry.getKey());
						Map<String, String> temp = new HashMap<String, String>(this.stackMap.get(className));
						functionInfo tempFI = this.functionalInfo.get(className);
						if (tempFI.parameters > 4) {
							temp.put(entry.getKey(), Integer.toString(tempFI.stackUsedParameters));
							tempFI.stackUsedParameters++;
							this.functionalInfo.put(className, tempFI);
							this.stackMap.put(className, temp);
						} else {
							temp.put(entry.getKey(), Integer.toString(tempFI.stackUsedParameters));
							tempFI.stackUsedParameters++;
							this.functionalInfo.put(className, tempFI);
							this.stackMap.put(className, temp);

						}
					}
				}
				Map<String, Integer> funcStackTemp = new HashMap<String, Integer>();

				for (int k : this.callInstruction) {
					Set<String> tempSet = this.controlFlowGraph.get(k).outSet;
					int counter = 0;
					String function = "";
					for (String s : tempSet) {
						
						String register = this.assignedRegisters.get(s);
						if (register.contains("t")) {
							function = varProc.get(s);
							counter++;
						}
					}
					if (function.equals("") == false) {
						if (funcStackTemp.containsKey(function) == true) {
							int stackNumber = funcStackTemp.get(function);
							if (stackNumber < counter) {
								funcStackTemp.put(function, counter);
							}

						} else {
							funcStackTemp.put(function, counter);
						}
					}

				}
				
				for (Map.Entry<String, Integer> entry : funcStackTemp //Compute the extra stack size needed
						.entrySet()) {									//for the parameters to 
					functionInfo myFuncInfo = this.functionalInfo.get(entry.getKey());
					myFuncInfo.stackUsedParameters = myFuncInfo.stackUsedParameters+ entry.getValue();
					myFuncInfo.tStack = entry.getValue();
				}
			}
		}
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Stack<String> graphColouringAlgorithm() {
		Map<String, Set<String>> tempGraph = new HashMap<String, Set<String>>();
		tempGraph.putAll(this.Graph);
		Stack<String> algorithmStack = new Stack<String>();
		boolean allEmpty = false;
		boolean oneLessThanK = true;  //One less than k=we have still a variable that can be assigned somewhere (less than 18 neighbors).
		boolean allInStack = false; //AllInStack,every node's neighbor is
		while (allInStack == false) {
			while (allEmpty == false && oneLessThanK == true) {
				allEmpty = true;
				oneLessThanK = false;
				Map<String, Set<String>> tempGraph2 = new HashMap<String, Set<String>>(tempGraph);
				for (Map.Entry<String, Set<String>> entry : tempGraph2.entrySet()) {
					String tempName = entry.getKey();
					Set<String> neighbors = tempGraph.get(tempName);
					if (neighbors.size() != 0) {
						allEmpty = false;    //If at least one is not empty.

					}
					if (neighbors.size() <= 18) { 	//If one node has less than 18 register.
						oneLessThanK = true;
						algorithmStack.push(tempName);
						for (String neighborName : neighbors) {
							Set<String> myTemp = new HashSet<String>(tempGraph.get(neighborName));
							myTemp.remove(tempName);
							tempGraph.put(neighborName, myTemp);
						}
						tempGraph.remove(tempName);

					}

				}

			}
			if (allEmpty == false && oneLessThanK == false) { //If we exited because no node has less than 18 neighbours.
				oneLessThanK = true;
				this.candidatesTospill = new HashSet<String>();
				for (Map.Entry<String, Set<String>> entry : tempGraph
						.entrySet()) {
					this.candidatesTospill.add(entry.getKey());
				}
				Map<String, Set<String>> tempGraph2 = new HashMap<String, Set<String>>(tempGraph);
				String tmpMaxNeighbors = null;
				int tmpMaxNoNeighbors = 0;
				for (String s : this.candidatesTospill) {		//Searh for the variable un-assigned with the max ocurrences
					Set<String> neighbors = tempGraph2.get(s);	//through the sets of the 18 neighbor nodes,and push it in the stack.
					int tempNeig = 0;
					for (String e : neighbors) {
						for (String n : this.candidatesTospill) {
							if (tempGraph2.get(n).contains(e)) {
								tempNeig++;

							}
						}
						if (tempNeig >= tmpMaxNoNeighbors) {
							tmpMaxNeighbors = e;
							tmpMaxNoNeighbors = tempNeig;
						}

					}

				}
				algorithmStack.push(tmpMaxNeighbors);
				Set<String> neighbors = tempGraph.get(tmpMaxNeighbors);
				for (String neighborName : neighbors) {
					Set<String> myTemp = new HashSet<String>(tempGraph.get(neighborName));
					myTemp.remove(tmpMaxNeighbors);
					tempGraph.put(neighborName, myTemp);

				}

				tempGraph.remove(tmpMaxNeighbors);

			} else {
				allInStack = true;
			}
		}
		if (oneLessThanK == true || allEmpty == true) {

			return algorithmStack;
		} else {

			return null;

		}
	}

}
