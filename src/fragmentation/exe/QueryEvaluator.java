package fragmentation.exe;

import java.io.File;
import basex.BXClient;
import basex.DBInfo;
import basex.MyRunnable;
import basex.PExecutor;
import basex.QueryPlans;
import basex.QueryResult_IntStringList;
import basex.common;
import fragmentation.FragmentIndex;

public class QueryEvaluator {
	static boolean isSerial = false;

	public static void main(String[] args) throws Exception {
		// for test
		if (args.length == 0)
			args = new String[] { "#4", "d:\\data\\fragments\\xmark0.1_hw_4_1000", "xm3.org" };

		DBInfo[] dbs = DBInfo.parse(new String[] { args[0], "" });
		String query = "/site/open_auctions/open_auction/bidder/increase";
		query = QueryPlans.getQueryPlan(args[2]).first();

		// retrieve PRE values of the first level children of the root from all servers.
		BXClient[] bxs = new BXClient[dbs.length];
		int[][] pres = new int[dbs.length][];
		for (int i = 0; i < dbs.length; i++) {
			bxs[i] = BXClient.open(dbs[i].ip);
			bxs[i].execute("set mainmem on");
			String cmd = String.format("xquery for $node in db:open('%s')/site/* return db:node-pre($node) ",
					dbs[i].dbname);
			String result = bxs[i].execute(cmd);
			String[] prevalues = result.split(common.getLinebreak());
			pres[i] = new int[prevalues.length];
			for (int j = 0; j < prevalues.length; j++) {
				pres[i][j] = Integer.parseInt(prevalues[j]);
			}
		}

		// process links
		FragmentIndex[][] linkss = new FragmentIndex[dbs.length][];
		for (int i = 0; i < linkss.length; i++)
			linkss[i] = new FragmentIndex[pres[i].length];
		FragmentIndex[] links = FragmentIndex.readLinks(args[1] + File.separator + "linkinfo.txt");
		for (FragmentIndex link : links) {
			link.mpre = pres[link.mid][link.mrank];
			linkss[link.mid][link.mrank] = link;
		}

		// process queries and return pre-formatted intermediate results.
		System.out.println("processing query...");
		long t1 = System.currentTimeMillis();

		String[] cmds = new String[dbs.length];
		for (int i = 0; i < dbs.length; i++) {
			cmds[i] = String.format("xquery for $node in db:open('%s')%s return (('', db:node-pre($node)), $node)",
					dbs[i].dbname, query);
		}

		PExecutor[] pes = new PExecutor[cmds.length];
		for (int i = 0; i < pes.length; i++)
			pes[i] = new PExecutor(bxs[i], 1, cmds[i]);

		if (isSerial) {
			for (PExecutor pe : pes)
				pe.run();
		} else {
			MyRunnable.parallelRun(pes);
		}

		// map results to fragments by the original PRE values
		long t2 = System.currentTimeMillis();
		for (int i = 0; i < dbs.length; i++) {
			QueryResult_IntStringList rd = (QueryResult_IntStringList) pes[i].sr;
			rd.initResults(linkss[i].length);
			int pos = 0;
			for (int j = 0; j < rd.pres.size(); j++) {
				while (rd.pres.get(j) > linkss[i][pos].mpre && pos < linkss[i].length - 1)
					pos++;
				rd.results.get(pos).add(rd.values.get(j));
			}
		}

		// process the final results.
		int size = 0;
		for (int i = 0; i < linkss.length; i++)
			size += linkss[i].length;
		FragmentIndex[] alllinks = new FragmentIndex[size];

		for (int i = 0; i < linkss.length; i++)
			for (int j = 0; j < linkss[i].length; j++)
				alllinks[linkss[i][j].fid] = linkss[i][j];
		long t3 = System.currentTimeMillis();

		System.out.printf("Completed. Execution time: %d ms, meger time: %d ms. \n", t2 - t1, t3 - t2);
	}

}
