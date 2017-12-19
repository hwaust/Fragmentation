package basex;

import java.io.File;
import java.io.FileWriter;

public class XQuery {

	public static void main(String[] args) throws Exception {
		String testargs = "-run 3 -query xq1 -st disk -f D:\\data\\results";
		args = args.length == 0 ? testargs.split(" ") : args;

		Context c = Context.initContent(args);
		System.out.println("Processing: " + c);

		String outfolder = c.makeOutFolder();
		String outfile = outfolder + c.query.key + ".txt";

		BXClient bx = BXClient.open(c.server);
		Logger logger = new Logger(c.outputfolder + File.separator + c.toCharacterString() + ".txt");
		long executionTime = 0;

		for (int rt = 0; rt < c.runningTimes; rt++) {
			common.gc();
			String query = "xquery " + c.query.first();
			if (c.inMemory) {
				long startTime = System.currentTimeMillis();
				bx.execute(query);
				executionTime = System.currentTimeMillis() - startTime;
			} else {
				new File(outfile).delete();
				FileWriter fw = new FileWriter(outfile);
				long startTime = System.currentTimeMillis();
				bx.execute(query, fw);
				executionTime = System.currentTimeMillis() - startTime;
				fw.close();
			}

			logger.add("total", executionTime);
		}

		logger.save();
		bx.close();
	}

}
