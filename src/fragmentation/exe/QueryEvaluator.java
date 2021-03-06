package fragmentation.exe;

import java.io.File;
import java.util.ArrayList;
import basex.BXClient;
import basex.PExecutor;
import basex.QueryPlans;
import basex.QueryResultPre;
import basex.common;
import fragmentation.Fragment;
import fragmentation.MergedTree;
import fragmentation.QContext;

public class QueryEvaluator {
	static boolean isSerial = false;

	public static void main(String[] args) throws Exception {
		// for test
		if (args.length == 0)
			args = new String[] { "-iplist", "#4", "-dblist", "range:mfrag:0-3", "-key", "xm6.org", "-f",
					"d:\\data\\fragments\\xmark1_4_20K_20171126", "-sys", "win" };

		QContext qc = QContext.parse(args); 
		System.out.println(qc);

		String outfolder = qc.datafolder + File.separator + "output_" + qc.query.key.split("\\.")[0] + File.separator;

		File outdir = new File(outfolder);
		if (!outdir.exists())
			outdir.mkdirs();

		ArrayList<Fragment> fs = Fragment.readFragmentList(qc.datafolder);
		MergedTree[] trees = MergedTree.createTrees(fs);

		String query = "/site/open_auctions/open_auction/bidder/increase";
		query = QueryPlans.getQueryPlan(qc.query.key).first();

		// process queries and return pre-formatted intermediate results.
		System.out.println("processing query...");
		long t1 = System.currentTimeMillis();

		String[] cmds = new String[trees.length];
		for (int i = 0; i < cmds.length; i++) {
			cmds[i] = String.format("xquery for $node in db:open('%s')%s return (('', db:node-pre($node)), $node)",
					qc.dbs[i], query);
		}

		common.saveStringtoFile(String.join("\n", cmds), outfolder + "p1_input_queries.txt");

		PExecutor[] pes = new PExecutor[cmds.length];
		BXClient[] bxs = new BXClient[trees.length];
		for (int i = 0; i < pes.length; i++) {
			bxs[i] = BXClient.open(qc.ips[i]);
			bxs[i].tagid = i;
			pes[i] = new PExecutor(bxs[i], 1, cmds[i]);
		}

		for (int i = 0; i < bxs.length; i++) {
			common.saveStringtoFile(bxs[i].execute(cmds[i]), outfolder + "p1_output_" + i + ".txt");
		}

		// for (PExecutor pe : pes)
		// pe.run();
		PExecutor.parallelRun(pes);

		QueryResultPre[] rs = new QueryResultPre[trees.length];
		for (int i = 0; i < rs.length; i++) {
			rs[i] = (QueryResultPre) pes[i].sr;
		}

		// map results to fragments by the original PRE values
		long t2 = System.currentTimeMillis();
		for (int i = 0; i < trees.length; i++) {
			QueryResultPre rd = (QueryResultPre) pes[i].sr;
			rd.initResults(trees[i].fragments.size());

			int pos = 0;
			for (int j = 0; j < rd.pres.size(); j++) {
				while (pos < trees[i].fragments.size() - 1 && rd.pres.get(j) > trees[i].fragments.get(pos + 1).mpre)
					pos++;
				rd.results.get(pos).add(rd.values.get(j));
			}

			// while (pos < mpres.size() - 1 && gpres.get(i) > mpres.get(pos + 1))

			StringBuilder sb = new StringBuilder();
			for (ArrayList<String> arr : rd.results)
				for (String s : arr)
					sb.append(s + "\n");
			common.saveStringtoFile(sb.toString(), outfolder + "p2_output_" + i + ".txt");
		}

		StringBuilder sb = new StringBuilder();
		for (Fragment f : fs) {
			ArrayList<String> arr = rs[f.mid].results.get(f.mrank);
			arr.forEach(s -> sb.append(s + "\n"));
		}

		common.saveStringtoFile(sb.toString(), outfolder + "p3_out_finalresult.txt");

		long t3 = System.currentTimeMillis();

		System.out.printf("Completed. Execution time: %d ms, meger time: %d ms. Results are saved to: %s\n", t2 - t1,
				t3 - t2, outfolder);
	}

}
