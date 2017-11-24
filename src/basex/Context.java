package basex;

public class Context {

	// server IP
	public String server;

	// database name.
	public String database;

	// the query to be evaluated.
	public QueryPlan query;

	// number of partitions.
	public int p;

	// number of times to
	public int runningTimes;

	public String[] iplist;

	public boolean dps;

	// default value
	private Context() {
		p = 1;
		database = null;
		query = null;
		runningTimes = 1;
		dps = false;
		iplist = new String[4];
		for (int i = 0; i < iplist.length; i++)
			iplist[i] = "172.21.52." + (i + 50);
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
		// nocheck for isSerial
	}

	// -db server:database -query query_key -run running_times -ip 7:52:98:99
	public static Context initContent(String[] args) throws Exception {
		String allargs = String.join(" ", args);
		System.out.println("args: " + allargs);

		Context c = new Context();
		String[] ops = allargs.split("-", -1);
		for (int i = 0; i < ops.length; i++) {
			if (ops[i].trim().length() == 0)
				continue;

			String[] strs = ops[i].split(" ");
			String op = strs[0].trim();
			String v = strs.length > 1 ? strs[1].trim() : null;

			if (op.equals("db")) {
				c.database = v;
			} else if (op.equals("query")) {
				c.query = QueryPlans.getQueryPlan(v);
				c.dps = v.contains(".dps");
			} else if (op.equals("run")) {
				c.runningTimes = Integer.parseInt(v);
			} else if (op.equals("ips")) {
				String[] ips = v.split(":");
				c.iplist = new String[ips.length];
				for (int j = 0; j < ips.length; j++)
					c.iplist[j] = "172.21.52." + ips[j];
			} else if (op.equals("p")){
				c.p = Integer.parseInt(v);
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
	public String getLogfileName() {
		if (query.key.contains(".org")) {
			return String.join("_", new String[] { database, query.key, common.getDateString() });
		}

		return String.join("_", new String[] { database, query.key, String.format("%02d", p), common.getDateString() });
	}

	// target: /site//keword --> xquery db:open('xmark1')/vn/site[p]/keyword
	public String getOriginal(int pos) {

		// e.g. /site//keword -> /vn/site[p]/keyword
		String xq = query.first().replace("/site", "/vn/site[" + (pos + 1) + "]");

		// xquery db:open('xmark1')/vn/site[p]/keyword
		xq = "xquery db:open('" + database + "')" + xq;
		return xq;
	}
	
	public static String getOriginal(QueryPlan query, String database, int pos) {

		// e.g. /site//keword -> /vn/site[p]/keyword
		String xq = query.first().replace("/site", "/vn/site[" + (pos + 1) + "]");

		// xquery db:open('xmark1')/vn/site[p]/keyword
		xq = "xquery db:open('" + database + "')" + xq;
		return xq;
	}
}
