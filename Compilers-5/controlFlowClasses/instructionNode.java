package controlFlowClasses;

import java.util.HashSet;
import java.util.Set;

public class instructionNode {
	public Set <String> defSet;
	public Set <String> useSet;
	public instructionNode next;
	public instructionNode previous;
	public instructionNode prevJump;
	public twoPathInstructionNext twoPathNext;
	public twoPathInstructionPrevious twoPathPrevious;
	public boolean isFunction=false;
	//public String instruction;
	public boolean isLabel=false;
	public String label=null;
	public Set <String>liveSet;
	public int instruction;
	public Set<String> inSet;
	public Set <String> outSet;
	public instructionNode(int instruction) {
		this.defSet = new HashSet<String>();
		this.useSet = new HashSet<String>();
		this.liveSet=new HashSet<String>();
		this.next = null;
		this.previous = null;
		this.prevJump=null;
		this.instruction = instruction;
		this.inSet = new HashSet<String>();
		this.outSet = new HashSet<String>();
		this.twoPathNext = new twoPathInstructionNext();
		this.twoPathPrevious = new twoPathInstructionPrevious();

	}
	

}
