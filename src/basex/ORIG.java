package basex;

import java.io.FileWriter;

public class ORIG {

	// BaseXProcessor
	public static void main(String... args) throws Exception {
		String testargs = "-db xmark1 -run 3 -query xm1.org -st disk";
		args = args.length == 0 ? testargs.split(" ") : args;
		Context c = Context.initContent(args);
		System.out.println("Processing: " + c);

		BXClient bx = BXClient.open("localhost");

		Logger logger = new Logger(c.getLogfileName() + ".txt");
		String query = String.format("xquery for $node in db:open('%s')%s return $node", c.database, c.query.first());
		for (int rt = 0; rt < c.runningTimes; rt++) {
			System.out.print(".");
			long t1 = System.currentTimeMillis();
			if (c.inMemory) {
				bx.execute(query);
			} else {
				FileWriter fw = new FileWriter(common.getFolder("results") + c.query.key + ".txt");
				bx.execute(query, fw);
				fw.close();
			}

			long t2 = System.currentTimeMillis();
			logger.add("total", t2 - t1);
		}

		bx.close();

		logger.save();

	}

}
