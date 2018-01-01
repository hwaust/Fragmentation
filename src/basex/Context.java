package basex;

import java.io.File;

import utils.common;

public class Context {

	// server IP
	public String server;

	// database name.
	public String database;

	// the query to be evaluated.
	public QueryPlan query;

	// number of partitions.
	public int p;

	public boolean inMemory;

	// folder that for storing output data.
	public String outputfolder;

	// number of times to
	public int runningTimes;

	// default value
	private Context() {
		p = 1;
		database = null;
		query = null;
		runningTimes = 1; 
	}

	private void check() throws Exception {
		if (p < 1)
			throw new Exception(String.format("violating Context.p > 1: Context.p = %d", p));
		if (database == null)
			throw new Exception("violating Context.table != null");
		if (query == null)
			throw new Exception("violating Context.query != null");
		if (runningTimes < 1)
			throw new Exception(
					String.format("violating Context.runningTimes > 1: Context.runningTimes = %d", runningTimes));
	}

	public static Context initContent(String[] args) throws Exception {
		String allargs = String.join(" ", args);
		Context c = new Context();
		String[] ops = allargs.split("-", -1);

		for (int i = 0; i < ops.length; i++) {
			if (ops[i].trim().length() == 0)
				continue;

			String cmd = ops[i].split(" ")[0];
			String arg = ops[i].split(" ")[1];

			switch (cmd) {
			case "server":
				c.server = arg;
				break;
				
			case "db":
				c.database = arg;
				break;

			case "query":
				c.query = QueryPlans.getQueryPlan(arg);
				break;

			case "run":
				c.runningTimes = Integer.parseInt(arg);
				break;

			case "p":
				c.p = Integer.parseInt(arg);
				break;

			case "st":
				c.inMemory = arg.equals("mem");
				break;

			case "f":
				c.outputfolder = arg;
				new File(c.outputfolder).mkdir();
				break;
			}

		}

		c.check();
		return c;

	}

	/**
	 * Returns a name for log file, containing information of database, option,
	 * query name, testing time etc.
	 *
	 * @return
	 */
	public String toCharacterString() {
		return String.format("%s_%s_%s_%s", database, query.key, inMemory ? "mem" : "disk", common.getDateString());
	}

	public String toString() {
		return String.format("database=%s, querykey=%s, storage=%s", database, query==null? "null": query.key, inMemory ? "memory" : "disk");
	}

	public String makeOutFolder() throws Exception {
		String f = outputfolder + File.separator + "results" + File.separator;
		new File(f).mkdirs();
		return f;
	}
}
