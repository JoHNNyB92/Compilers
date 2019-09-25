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
			PigletParser parser = new PigletParser(fis);
			biggestTemp eval=new biggestTemp();
			Goal tree = parser.Goal();
			tree.accept(eval, null);
			String []pgFile=args[i].split("\\.");
			File file = new File(pgFile[0]+".spg");
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			MyPigletVisitor spg=new MyPigletVisitor(eval.maxTemp,bw);
			tree.accept(spg,null);
			bw.close();
			
			
			
			System.out.println("---------------------------------------------------------------");
			System.out.println("Done with file:"+pgFile[0]+".spg");
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
