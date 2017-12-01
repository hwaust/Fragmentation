package fragmentation.exe;

import java.util.ArrayList;

import basex.BXClient;
import basex.MyRunnable;
import basex.PExecutor;
import basex.QueryPlans;
import basex.QueryResult_IntStringList;
import fragmentation.FContext;
import fragmentation.Fragment;
import fragmentation.MergedTree;

public class QueryEvaluator {
	static boolean isSerial = false;

	public static void main(String[] args) throws Exception {
		// for test
		if (args.length == 0)
			args = new String[] { "-iplist", "#4", "-dblist", "range:mfrag:0-3", "-key", "xm3.org", "-f",
					"d:\\data\\fragments\\xmark1_4_20K_20171126" };

		FContext fc = FContext.parse(args);
		System.out.println(fc.toString1());

		ArrayList<Fragment> fs = Fragment.readFragmentList(fc.datafolder);
		MergedTree[] trees = MergedTree.createTrees(fs);

		String query = "/site/open_auctions/open_auction/bidder/increase";
		query = QueryPlans.getQueryPlan(fc.querykey).first();

		// retrieve PRE values of the first level children of the root from all servers.
		BXClient[] bxs = new BXClient[trees.length];

		// process queries and return pre-formatted intermediate results.
		System.out.println("processing query...");
		long t1 = System.currentTimeMillis();

		String[] cmds = new String[trees.length];
		for (int i = 0; i < cmds.length; i++) {
			cmds[i] = String.format("xquery for $node in db:open('%s')%s return (('', db:node-pre($node)), $node)",
					fc.dbs[i], query);
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
		for (int i = 0; i < trees.length; i++) {
			QueryResult_IntStringList rd = (QueryResult_IntStringList) pes[i].sr;
			rd.initResults(trees[i].fragments.size());
			int pos = 0;
			for (int j = 0; j < rd.pres.size(); j++) {
				while (rd.pres.get(j) > trees[i].fragments.get(j).mpre && pos < trees[i].fragments.size() - 1)
					pos++;
				rd.results.get(pos).add(rd.values.get(j));
			}
		}

		//
		// // process the final results.
		// int size = 0;
		// for (int i = 0; i < linkss.length; i++)
		// size += linkss[i].length;
		// FragmentIndex[] alllinks = new FragmentIndex[size];
		//
		// for (int i = 0; i < linkss.length; i++)
		// for (int j = 0; j < linkss[i].length; j++)
		// alllinks[linkss[i][j].fid] = linkss[i][j];
		long t3 = System.currentTimeMillis();

		System.out.printf("Completed. Execution time: %d ms, meger time: %d ms. \n", t2 - t1, t3 - t2);
	}

}
