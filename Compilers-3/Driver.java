import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import syntaxtree.Goal;


class Driver {
    public static void main (String [] args){
        for (int i = 0; i < args.length; i++) {
        FileInputStream fis = null;
        try{
        	fis = new FileInputStream(args[i]);
			MiniJavaParser parser = new MiniJavaParser(fis);
			ClassMembersVisitor eval = new ClassMembersVisitor(); //2nd Visitor / Variables-Methods of class
			
			Goal tree = parser.Goal();
			tree.accept(eval, null);
			
			String []pgFile=args[i].split("\\.");
			//System.out.println("pgFile="+args[0]);
			File file = new File(pgFile[0]+".pg");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			MyPigletVisitor pg = new MyPigletVisitor(eval,bw);
			tree.accept(pg,null);
			bw.close();
			System.out.println("---------------------------------------------------------------");
			System.out.println("Done with file:"+pgFile[0]+".pg");
			System.out.println("---------------------------------------------------------------");

			
        }
        catch(IOException IOe){
        	System.err.println(IOe.getMessage());
        }
        catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
    }
}
