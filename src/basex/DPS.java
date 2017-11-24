package basex;

public class DPS extends MyRunnable {

	public static void main(String[] args) throws Exception {

		String myargs = "-db xmark40 -query xm5b.dps -run 1 -p 4 -ips 50:54:55:56";
		Context c = Context.initContent(myargs.split(" "));

		// declare 4 dps processors.
		DPS[] dpss = new DPS[1];
		for (int i = 0; i < dpss.length; i++)
			dpss[i] = new DPS(c.server, c.database, i + 1, c.p);

		// open all processors.
		for (int i = 0; i < dpss.length; i++)
			dpss[i].open();

		// evaluate
		for (int i = 0; i < dpss.length; i++)
			dpss[i].evaluate(c.query);

		// waiting
		for (int i = 0; i < dpss.length; i++) {
			while (dpss[i].isRunning())
				Thread.sleep(1);
		}

		// output
		for (int i = 0; i < dpss.length; i++) {
			DPS dps = dpss[i];
			System.out.println("------ pos = " + i + "------\n");
			System.out.println(dps.result == null ? 0 : dps.result.length());
			System.out.println(dps.totaltime + " ms");
		}

		// close
		for (int i = 0; i < dpss.length; i++)
			dpss[i].close();
	}

	public long totaltime;
	public String result;
	public QueryPlan query;

	String server;
	String database;
	int position;
	int P;

	BXClient[] pros;
	QueryResult[] queryResults;
	PExecutor[] pes;
	ServerSide ss;

	Thread t;

	public DPS(String server, String database, int position, int P) {
		this.server = server;
		this.database = database;
		this.position = position;
		this.P = P;
		ss = new ServerSide(database);
		ss.tmpdb = database + "_" + position + "_parts";
	}

	public void open() throws Exception {
		// processors
		pros = new BXClient[P + 1];

		// for holding results of partitioned processors.
		queryResults = new QueryResult[P];

		// open all connections. A server could be "localhost" or "172.21.52.99"
		for (int i = 0; i < pros.length; i++)
			pros[i] = BXClient.open(server);

		// prepare parallel executors.
		pes = new PExecutor[P];
		for (int i = 0; i < pes.length; i++)
			pes[i] = new PExecutor(pros[i + 1]);

		// "set mainmem on; drop db parts;create db parts <root></root>";
		pros[0].execute("set mainmem on");
		pros[0].execute("drop db " + ss.tmpdb);
		pros[0].execute("create db " + ss.tmpdb + " <root></root>");

//		String xquery = String.format("xquery count(db:open('%s')//*)", ss.tmpdb);
//		System.out.println("xpath: " + xquery);
//		System.out.println(pros[0].execute(xquery));

	}

	public void close() throws Exception {
		for (BXClient pro : pros)
			pro.close();
	}

	public void evaluate(QueryPlan query) {
		this.query = query;
		t = new Thread(this);
		t.start();
	}

	public boolean isRunning() {
		return t != null && (t.getState() == Thread.State.RUNNABLE);
	}

	@Override
	public void run() {
		try {
			// clear intermediate result in temp database
			for (String setting : ss.createTempdb().split(";"))
				pros[0].execute(setting);
			result = null;

			/********************* suffix part ********************/
			String prefix = ss.getPrefix(query, position, P);
			QueryResult sr = pros[0].exeQuery(prefix);
			totaltime = sr.totalTime();

			/********************* prefix part ********************/
			String[] suffix = ss.getSuffix(query, P);
			long suffix_total = 0;
			// parallel execute
			for (int i = 0; i < pes.length; i++) {
				pes[i].xquery = suffix[i];
				// System.out.println(suffix[i]);
			}
			MyRunnable.parallelRun(pes);

			for (int i = 0; i < queryResults.length; i++) {
				if (pes[i].sr == null)
					continue;

				queryResults[i] = pes[i].sr;
				suffix_total = queryResults[i].totalTime() > suffix_total ? queryResults[i].totalTime() : suffix_total;

				// System.out.println("suffix_" + i + ": " +
				// queryResults[i].totalTime());
			}
			totaltime += suffix_total;

			// calculate result size and merge result
			int totalsize = 0;
			for (int i = 0; i < queryResults.length; i++)
				if (queryResults[i] != null)
					totalsize += queryResults[i].resultSize();
			long t1 = System.currentTimeMillis();
			StringBuilder sb = new StringBuilder(totalsize + 5 * P);
			for (int i = 0; i < queryResults.length; i++)
				if (queryResults[i] != null)
					sb.append(queryResults[i].result + "\n");
			long t2 = System.currentTimeMillis();
			totaltime += t2 - t1;
			result = sb.toString();
			
//			if (result.length() == 2)
//				System.out.println("result: " + result);
//			else
//				System.out.println("result.length: " + result.length());
//			System.out.println("totaltime: " + totaltime + " ms.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (latch != null)
			latch.countDown();
	}

	public static void wait(DPS[] dpss) throws Exception {
		for (int i = 0; i < dpss.length; i++) {
			while (dpss[i].isRunning())
				Thread.sleep(1);
		}
	}

	public static DPS open(String server, String database, int position, int p2) throws Exception {
		DPS dps = new DPS(server, database, position, p2);
		dps.open();
		return dps;
	}
}
