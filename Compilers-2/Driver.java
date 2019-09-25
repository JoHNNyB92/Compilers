import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import syntaxtree.Goal;

class Driver {
	public static void main(String[] args) {
		FileInputStream fis = null;

		for (int i = 0; i < args.length; i++) {
			try {
				System.out
				.println("--------------------------------------------------------------.");
				fis = new FileInputStream(args[i]);
				MiniJavaParser parser = new MiniJavaParser(fis);
				ClassVisitor cVisitor = new ClassVisitor();   //1st Visitor	/ Classes-extendedClasses
				ClassMembersVisitor eval = new ClassMembersVisitor(); //2nd Visitor / Variables-Methods of class
				Goal tree = parser.Goal();
				tree.accept(cVisitor, null);
				tree.accept(eval, null);
				TypeCheckingVisitor tcVisitor = new TypeCheckingVisitor(eval, //3rd Visitor / TypeChecking
						cVisitor);

			
				tree.accept(tcVisitor, null);
				System.out.println("File " + args[i]
						+ " is semantically correct.");
			} catch (SemanticException ex) {
				System.out.println("File " + args[i]
						+ " is semantically incorrect.");

			}

			catch (ParseException ex) {
				System.out.println(ex.getMessage());
				System.out.println("File "+args[i]+" could not be parsed.");
			} catch (FileNotFoundException ex) {
				System.err.println(ex.getMessage());
			}

			finally {

				try {
					if (fis != null)
						fis.close();
				} catch (IOException ex) {
					System.err.println(ex.getMessage());
				}
			}
		}
	}
}
