import syntaxtree.Temp;
import visitor.GJDepthFirst;


public class biggestTemp extends GJDepthFirst<String,String> {
	int maxTemp;
	public biggestTemp(){
		maxTemp=0;
	}
	public String visit(Temp n, String argu) {
	      String _ret=null;
	      n.f0.accept(this, argu);
	      //System.out.println(n.f1.f0.tokenImage.toString());
	      int temp=Integer.parseInt(n.f1.f0.tokenImage.toString());
	      if(temp>maxTemp){
	    	  this.maxTemp=temp;
	      }
	      return _ret;
	   }

}
