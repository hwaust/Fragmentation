package basex;

import java.io.File;
import java.io.FileWriter;

public class ORIG {

	public static void main(String... args) throws Exception {

		String testargs = "-db xmark1 -run 3 -query xm3.org -st disk -f D:\\data\\results";
		args = args.length == 0 ? testargs.split(" ") : args;

		Context c = Context.initContent(args);
		System.out.println("Processing: " + c);

		String outfolder = c.makeOutFolder();
		String outfile = outfolder + c.query.key + ".txt";

		BXClient bx = BXClient.open(c.server);
		String query = String.format("xquery for $node in db:open('%s')%s return $node", c.database, c.query.first());
		Logger logger = new Logger(c.outputfolder + File.separator + c.toCharacterString() + ".txt");
		long executionTime = 0;

		for (int rt = 0; rt < c.runningTimes; rt++) {
			common.gc();

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
