import java.io.BufferedWriter;
import java.io.IOException;

import syntaxtree.ALoadStmt;
import syntaxtree.AStoreStmt;
import syntaxtree.BinOp;
import syntaxtree.CJumpStmt;
import syntaxtree.CallStmt;
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
import syntaxtree.NodeOptional;
import syntaxtree.Operator;
import syntaxtree.PassArgStmt;
import syntaxtree.PrintStmt;
import syntaxtree.Procedure;
import syntaxtree.Reg;
import syntaxtree.SimpleExp;
import syntaxtree.SpilledArg;
import syntaxtree.Stmt;
import visitor.GJDepthFirst;


public class mipsVisitor extends GJDepthFirst<String,String>
{
	BufferedWriter  bw;
	boolean labelBeforeInstruction=false;
	String passArg="";
	
	public mipsVisitor(BufferedWriter _bw){
		bw=_bw;
	}
	public String visit(NodeOptional n, String argu) {
		if (n.present()) {

			String label = n.node.accept(this, argu);
			try {
				bw.write(label + ": ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else
			return null;
	}
	/**
	    * f0 -> "MAIN"
	    * f1 -> "["
	    * f2 -> IntegerLiteral()
	    * f3 -> "]"
	    * f4 -> "["
	    * f5 -> IntegerLiteral()
	    * f6 -> "]"
	    * f7 -> "["
	    * f8 -> IntegerLiteral()
	    * f9 -> "]"
	    * f10 -> StmtList()
	    * f11 -> "END"
	    * f12 -> ( Procedure() )*
	    * f13 -> <EOF>
	    */
	   public String visit(Goal n, String argu) {
		   String _ret=null;
		   try{
			  bw.write(".text \n");
		      n.f0.accept(this, argu);
		      bw.write("main:\n");
		      n.f1.accept(this, argu);
		      n.f2.accept(this, argu);
		      n.f3.accept(this, argu);
		      n.f4.accept(this, argu);
		      String secondArgu=n.f5.accept(this, argu);
		      n.f6.accept(this, argu);
		      n.f7.accept(this, argu);
		      n.f8.accept(this, argu);
		      n.f9.accept(this, argu);
		      bw.write("\tadd $sp, $sp, -"+Integer.toString(4*(Integer.parseInt(secondArgu)+1))+" \n");
		      bw.write("\tsw $ra, 4($sp)\n");
		      n.f10.accept(this, argu);
		      bw.write("\tlw $ra, 4($sp)\n"+
		    		  	"\tadd $sp, $sp, "+Integer.toString(4*(Integer.parseInt(secondArgu)+1))+" \n"+
		    		  	"\tadd $v0, $0, 0\n"+
		    		  	"\tjr $ra\n");
		      n.f11.accept(this, argu);
		      n.f12.accept(this, argu);
		      bw.write(".alloc:\n"+
		    		  "\tadd $v0, $0, 9\n"+
		    		  "\tsyscall\n"+
		    		  "\tjr $ra\n"+
		    		  ".print:\n"+
		    			  "\tadd $v0, $0, 1\n"+
		    			  "\tsyscall\n"+
		    			  "\tadd $a0, $0, 10\n"+
		    			  "\tadd $v0, $0, 11\n"+
		    			  "\tsyscall\n"+
		    			  "\tjr $ra\n");
		      n.f13.accept(this, argu);
		   }
	      catch(IOException e){
	    	  e.printStackTrace();
	      }
	      return _ret;
	   }
	   /**
	    * f0 -> Label()
	    * f1 -> "["
	    * f2 -> IntegerLiteral()
	    * f3 -> "]"
	    * f4 -> "["
	    * f5 -> IntegerLiteral()
	    * f6 -> "]"
	    * f7 -> "["
	    * f8 -> IntegerLiteral()
	    * f9 -> "]"
	    * f10 -> StmtList()
	    * f11 -> "END"
	    */
	   public String visit(Procedure n, String argu) {
	      String _ret=null;
	      try{
	      String functionName=n.f0.accept(this, argu);
	      bw.write(functionName+":\n");
	      n.f1.accept(this, argu);
	      n.f2.accept(this, argu);
	      n.f3.accept(this, argu);
	      n.f4.accept(this, argu);
	      String secondArgu=n.f5.accept(this, argu);
	      n.f6.accept(this, argu);
	      n.f7.accept(this, argu);
	      n.f8.accept(this, argu);
	      n.f9.accept(this, argu);
	      bw.write("\tadd $sp, $sp, -"+Integer.toString(4*(Integer.parseInt(secondArgu)+1))+" \n"+
	    	  	  "\tsw $ra, 4($sp)\n");
	      n.f10.accept(this, argu);
	      n.f11.accept(this, argu);
	      bw.write("\tlw $ra, 4($sp)\n"+
	    		  "\tadd $sp, $sp, "+Integer.toString(4*(Integer.parseInt(secondArgu)+1))+" \n"+
	    	  	  "\tjr $ra\n");
	      }
	      catch(IOException e){
	    	  e.printStackTrace();
	      }
	      return _ret;
	   }
	   /**
	    * f0 -> NoOpStmt()
	    *       | ErrorStmt()
	    *       | CJumpStmt()
	    *       | JumpStmt()
	    *       | HStoreStmt()
	    *       | HLoadStmt()
	    *       | MoveStmt()
	    *       | PrintStmt()
	    *       | ALoadStmt()
	    *       | AStoreStmt()
	    *       | PassArgStmt()
	    *       | CallStmt()
	    */
	   public String visit(Stmt n, String argu) {
	      return n.f0.accept(this, argu);
	   }

	   /**
	    * f0 -> "CJUMP"
	    * f1 -> Reg()
	    * f2 -> Label()
	    */
	   public String visit(CJumpStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String register=n.f1.accept(this, argu);
	      String label=n.f2.accept(this, argu);
	      try {
			bw.write("\tbne "+register+", 1,"+label+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }

	   /**
	    * f0 -> "JUMP"
	    * f1 -> Label()
	    */
	   public String visit(JumpStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String label=n.f1.accept(this, argu);
	      try {
			bw.write("\tj "+label+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }

	   /**
	    * f0 -> "HSTORE"
	    * f1 -> Reg()
	    * f2 -> IntegerLiteral()
	    * f3 -> Reg()
	    */
	   public String visit(HStoreStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String register1=n.f1.accept(this, argu);
	      String offset=n.f2.accept(this, argu);
	      String register2=n.f3.accept(this, argu);
	      try {
			bw.write("\tsw "+register2+","+Integer.toString(Integer.parseInt(offset))+"("+register1+")\n");
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }

	   /**
	    * f0 -> "HLOAD"
	    * f1 -> Reg()
	    * f2 -> Reg()
	    * f3 -> IntegerLiteral()
	    */
	   public String visit(HLoadStmt n, String argu) {
	      String _ret=null;
	      String register1=n.f1.accept(this, argu);
	      String register2=n.f2.accept(this, argu);
	      String offset=n.f3.accept(this, argu);
	      try {
			bw.write("\tlw "+register1+","+Integer.toString(Integer.parseInt(offset))+"("+register2+")\n");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }

	   /**
	    * f0 -> "MOVE"
	    * f1 -> Reg()
	    * f2 -> Exp()
	    */
	   public String visit(MoveStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      this.labelBeforeInstruction=false;
	      String register=n.f1.accept(this, argu);
	      String exp=n.f2.accept(this, register);
	      if(this.labelBeforeInstruction==true){
	    	  try {
					bw.write("\tla "+register+","+exp+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
	      }
	      else if(exp!=null){
	    	try {
				bw.write("\tadd "+register+",$0,"+exp+" \n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
	      }
	      return _ret;
	   }

	   /**
	    * f0 -> "PRINT"
	    * f1 -> SimpleExp()
	    */
	   public String visit(PrintStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String simpleExp=n.f1.accept(this, argu);
	      try {
			bw.write("\tadd $a0, $0, "+simpleExp+"\n"+
					   "\tjal .print\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }

	   /**
	    * f0 -> "ALOAD"
	    * f1 -> Reg()
	    * f2 -> SpilledArg()
	    */
	   public String visit(ALoadStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String register1=n.f1.accept(this, argu);
	      String offset=n.f2.accept(this, argu);
	      try {
			bw.write("\tlw "+register1+","+Integer.toString((2+Integer.parseInt(offset))*4)+"($sp)\n");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }

	   /**
	    * f0 -> "ASTORE"
	    * f1 -> SpilledArg()
	    * f2 -> Reg()
	    */
	   public String visit(AStoreStmt n, String argu) {
		    String _ret=null;
		   	n.f0.accept(this, argu);
		      String register1=n.f2.accept(this, argu);
		      String offset=n.f1.accept(this, argu);
		      try {
				bw.write("\tsw "+register1+","+Integer.toString((2+Integer.parseInt(offset))*4)+"($sp)\n");
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      return _ret;
	   }

	   /**
	    * f0 -> "PASSARG"
	    * f1 -> IntegerLiteral()
	    * f2 -> Reg()
	    */
	   public String visit(PassArgStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String offset=n.f1.accept(this, argu);
	      String register=n.f2.accept(this, argu);
	      
			//"\tsw "+register+",-"+ Integer.toString((Integer.parseInt(offset)+1)*4)+"($sp)\n");
	      this.passArg=this.passArg+"sw "+register+",#";
	      return _ret;
	   }

	   /**
	    * f0 -> "CALL"
	    * f1 -> SimpleExp()
	    */
	   public String visit(CallStmt n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String simpleExp=n.f1.accept(this, argu);
	      try {
	    	if(this.passArg.equals("")==false){
	    		String []passedArgument=this.passArg.split("#");
	    		int offset=passedArgument.length;
	    		int counter=0;
	    		while(offset>0){
	    			bw.write(passedArgument[counter]+"-"+Integer.toString((offset+3)*4)+"($sp)\n");
	    			offset--;
	    			counter++;
	    		}
	    		this.passArg="";
	    	}
			bw.write("\tjalr "+simpleExp+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return _ret;
	   }
	   /**
	    * f0 -> "NOOP"
	    */
	   public String visit(NoOpStmt n, String argu) {
	      try {
			bw.write("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   return null;
	   }

	   /**
	    * f0 -> "ERROR"
	    */
	   public String visit(ErrorStmt n, String argu) {
		   try {
				bw.write("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   return null;
	   }

	   /**
	    * f0 -> "HALLOCATE"
	    * f1 -> SimpleExp()
	    */
	   public String visit(HAllocate n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String simpleExp=n.f1.accept(this, argu);
	      try {
			bw.write("\tadd $a0,$0,"+simpleExp+"\n"+
					  	"\tjal .alloc \n"+
					  	"\tadd "+argu+", $v0, $0\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	      return _ret;
	   }

	   /**
	    * f0 -> Operator()
	    * f1 -> Reg()
	    * f2 -> SimpleExp()
	    */
	   public String visit(BinOp n, String argu) {
	      String _ret=null;
	      try{
	      String op=n.f0.accept(this, argu);
	      String operand1=n.f1.accept(this, argu);
	      String operand2=n.f2.accept(this, argu);
	      if(op.equals("LT ")==true){
	    		bw.write("\tslt "+argu+", "+operand1+","+operand2+"\n");
	      }
	      else if(op.equals("PLUS ")==true){
	    	  bw.write("\tadd "+argu+", "+operand1+","+operand2+"\n");
	      }
	      else if(op.equals("MINUS ")==true){
	    	  bw.write("\tsub "+argu+", "+operand1+","+operand2+"\n");
	      }
	      else{
	    	  bw.write("\tmul "+argu+","+operand1+", "+operand2+" \n");
	      }
	      }
	      catch(IOException e){
	    	  e.printStackTrace();
	      }
	      return _ret;
	      }
	   

	   /**
	    * f0 -> "LT"
	    *       | "PLUS"
	    *       | "MINUS"
	    *       | "TIMES"
	    */
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

	   /**
	    * f0 -> "SPILLEDARG"
	    * f1 -> IntegerLiteral()
	    */
	   public String visit(SpilledArg n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      String offset=n.f1.accept(this, argu);
	      return offset;
	   }

	   

	   /**
	    * f0 -> "a0"
	    *       | "a1"
	    *       | "a2"
	    *       | "a3"
	    *       | "t0"
	    *       | "t1"
	    *       | "t2"
	    *       | "t3"
	    *       | "t4"
	    *       | "t5"
	    *       | "t6"
	    *       | "t7"
	    *       | "s0"
	    *       | "s1"
	    *       | "s2"
	    *       | "s3"
	    *       | "s4"
	    *       | "s5"
	    *       | "s6"
	    *       | "s7"
	    *       | "t8"
	    *       | "t9"
	    *       | "v0"
	    *       | "v1"
	    */
	   public String visit(Reg n, String argu) {
		   int num= n.f0.which;
		   if(num<4){
			   return "$a"+num;
		   }
		   else if(num<12){
			   return "$t"+Integer.toString(num-4);

		   }
		   else if(num<20){
			   return "$s"+Integer.toString(num-12);
		   }
		   else if(num==20 || num==21){
			   return "$t"+Integer.toString(num-12);
		   }
		   else{
			   return "$v"+Integer.toString(num-22);
		   }
		   
	   }

	   /**
	    * f0 -> <INTEGER_LITERAL>
	    */
	   public String visit(IntegerLiteral n, String argu) {
	      return n.f0.tokenImage.toString();
	   }

	   /**
	    * f0 -> <IDENTIFIER>
	    */
	   public String visit(Label n, String argu) {
		   this.labelBeforeInstruction=true;
	      return n.f0.tokenImage.toString();
	   }

	}

