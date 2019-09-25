import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import syntaxtree.Goal;

class Driver {
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(args[i]);
				KangaParser parser = new KangaParser(fis);
				Goal tree = parser.Goal();
				String[] pgFile = args[i].split("\\.");
				File file = new File(pgFile[0] + ".mips");
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				mipsVisitor mips = new mipsVisitor(bw);
				tree.accept(mips, null);
				bw.close();
				System.out.println("---------------------------------------------------------------");
				System.out.println("Done with file:" + pgFile[0] + ".mips");
				System.out.println("---------------------------------------------------------------");

			} catch (IOException IOe) {
				System.err.println(IOe.getMessage());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
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
