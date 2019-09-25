import syntaxtree.IntegerLiteral;
import visitor.GJDepthFirst;


public class PigletVisitor extends GJDepthFirst<String,String> {
	
	public String visit(IntegerLiteral n, String argu) {
		System.out.println("INTEGER_LITERAL="+n.f0.tokenImage.toString());
	      return n.f0.accept(this, argu);
	   }

}
