import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.MainClass;
import visitor.GJVoidDepthFirst;

public class ClassVisitor extends GJVoidDepthFirst<String> {
	Set<String> classNames;      //Set of all classes name.
	Map<String, String> extendedClassNames;	//Map of all classes that extend another class.
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ClassVisitor() {				//Constructor.
		classNames = new HashSet<String>();
		this.extendedClassNames = new HashMap<String, String>();

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void visit(ClassDeclaration n, String _) {
		classNames.add(n.f1.f0.tokenImage.toString());		//Simple class Declaration
		super.visit(n, null);

	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
public void visit(ClassExtendsDeclaration n, String _) {
		if (this.classNames.contains(n.f3.f0.tokenImage.toString())) {		//Check whether the extended class
			this.classNames.add(n.f1.f0.tokenImage.toString());				//is already defined.
			this.extendedClassNames.put(n.f1.f0.tokenImage.toString(),n.f3.f0.tokenImage.toString());
	
		} 
		else 
		{
			throw new SemanticException("Class with name "
					+ n.f3.f0.tokenImage.toString() + " is not defined.");
		}
		super.visit(n, null);

	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void visit(MainClass n, String _) {
		classNames.add(n.f1.f0.tokenImage.toString());	//Insert main class into map.
		super.visit(n, null);

	}

}
