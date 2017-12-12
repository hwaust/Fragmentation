package tests;

import basex.BXClient;

public class SerializationModeTest {

	public static void main(String[] args) throws Exception { 
		String[] modes = { "xml", "xhtml", "html", "adaptive", "basex" }; 
		String xquery = "xquery declare option output:method '{mode}';\n"
				+ "declare option output:item-separator '[';\n"
				+ "for $node in db:open('xmark10')//keyword\n"
				+ "  return (db:node-pre($node), $node)";
		
		double totaltime = 0;
		long starttime = 0;
		String result = null;  // for store results of queries.
		int runningTime = 100;

		BXClient bx = BXClient.open("localhost");
		for (String mode : modes) {
			String q = xquery.replace("{mode}", mode);
			bx.execute(q); // first run for warm up
			
			totaltime = 0;
			for (int i = 0; i < runningTime; i++) { 
				starttime = System.currentTimeMillis();
				result = bx.execute(q);
				totaltime += System.currentTimeMillis() - starttime;
			}
			System.out.printf("%s\t %.2f ms, results.length = %d KiB.\n", mode, totaltime / runningTime,
					result.length() / 1024);
		}
		bx.close();
	}
}
