package fragmentation.exe;

import java.io.File;
import java.util.ArrayList;

import basex.BXClient;
import basex.PExecutor;
import basex.QueryPlan;
import basex.QueryPlans;
import basex.QueryResult_IntStringList;
import basex.ServerSide;
import basex.ServerSideEx;
import basex.common;
import fragmentation.Fragment;
import fragmentation.MergedTree;
import fragmentation.QContext;

public class QueryEvaluatorDPS {

	public static void main(String[] args) throws Exception {

		/*******************************
		 * Initialization
		 *******************************/
		// for test
		if (args.length == 0)
			args = new String[] { "-iplist", "#4", "-dblist", "range:mfrag:0-3", "-key", "xm3a.dps", "-f",
					"D:\\data\\fragments\\xmark1_4_20K_20171126", "-p", "4", "-sys", "win" };
		QContext qc = QContext.parse(args);
		System.out.println(qc);
		BXClient.isTargetServerWinows = QContext.isWin;
		int Ns = qc.dbs.length;
		ArrayList<Fragment> fs = Fragment.readFragmentList(qc.datafolder);
		MergedTree[] trees = MergedTree.createTrees(fs);
		ServerSideEx[] sss = new ServerSideEx[Ns];
		for (int i = 0; i < sss.length; i++)
			sss[i] = new ServerSideEx(qc.dbs[i]);
		String outfolder = qc.datafolder + File.separator + "output_" + qc.query.key.split("\\.")[0] + File.separator;
		new File(outfolder).mkdir();
		PExecutor[] pes = new PExecutor[Ns];
		for (int i = 0; i < Ns; i++)
			pes[i] = new PExecutor(BXClient.open(qc.ips[i], true));

		/*******************************
		 * Process prefix query
		 *******************************/
		System.out.printf("processing query %s...\n", qc.query.key);
		long t1 = System.currentTimeMillis();

		// set mainmem on; drop tmpdb; set new db;
		for (int i = 0; i < Ns; i++) {
			for (String q : sss[i].createTempdb().split(";"))
				pes[i].bx.execute(q);
		}

		String[] prefixes = new String[Ns];
		for (int i = 0; i < Ns; i++)
			prefixes[i] = sss[i].getPrefix(qc.query, qc.p);

		// for prefix
		for (int i = 0; i < pes.length; i++)
			pes[i] = new PExecutor(BXClient.open(qc.ips[i], true), 1, prefixes[i]);
		PExecutor.parallelRun(pes);

		for (int i = 0; i < pes.length; i++)
			System.out.printf("pes[%d]=%s\n", i, pes[i].xquery);

		/*******************************
		 * Process suffix query
		 *******************************/
		String[][] suffixes = new String[trees.length][];
		for (int i = 0; i < prefixes.length; i++)
			suffixes[i] = sss[i].getSuffix(qc.query, qc.p);

		PExecutor[][] pess = new PExecutor[Ns][];
		for (int i = 0; i < Ns; i++) {
			pess[i] = new PExecutor[qc.p];
			for (int j = 0; j < qc.p; j++) {
				pess[i][j] = new PExecutor(BXClient.open(qc.ips[i]), 1, suffixes[i][j]);
				System.out.printf("pess[%d][%d]=%s\n", i, j, pess[i][j].xquery);
			}
		}

		// PExecutor[][] -> PExecutor[] for executing.
		pes = new PExecutor[Ns * qc.p];
		for (int i = 0; i < pes.length; i++)
			pes[i] = pess[i / Ns][i % Ns];
		PExecutor.parallelRun(pes);

		/*******************************
		 * Process results
		 *******************************/
		QueryResult_IntStringList[][] rs = new QueryResult_IntStringList[Ns][];
		for (int i = 0; i < rs.length; i++) {
			rs[i] = new QueryResult_IntStringList[qc.p];
			for (int j = 0; j < qc.p; j++)
				rs[i][j] = (QueryResult_IntStringList) pes[i * Ns + j].sr;
		}

		// Save Results for the first step
		for (int i = 0; i < Ns; i++) {
			for (int j = 0; j < qc.p; j++) {
				QueryResult_IntStringList rij = rs[i][j];
				StringBuilder sb = new StringBuilder();
				for (int k = 0; k < rij.pres.size(); k++) {
					sb.append(rij.pres.get(k) + "\n" + rij.values.get(k) + "\n\n");
				}
				String filename = String.format("%sP1_output_T%d_P%d.txt", outfolder, i, j);
				common.saveStringtoFile(sb.toString(), filename);
			}

		}

		// map results to fragments by the original PRE values
		long t2 = System.currentTimeMillis();

		for (int i = 0; i < trees.length; i++) {
			int pos = 0;
			ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
			for (int i1 = 0; i1 < trees[i1].fragments.size(); i++)
				values.add(new ArrayList<String>());

			for (int k = 0; k < qc.p; k++) {
				QueryResult_IntStringList rd = rs[i][k];
				for (int j = 0; j < rd.pres.size(); j++) {
					while (pos < trees[i].fragments.size() - 1 && rd.pres.get(j) > trees[i].fragments.get(pos + 1).mpre)
						pos++;
					values.get(pos).add(rd.values.get(j));
				}
			}

			StringBuilder sb = new StringBuilder();
			for (ArrayList<String> arr : values)
				for (String s : arr)
					sb.append(s + "\n");
			common.saveStringtoFile(sb.toString(), outfolder + "p2_output_" + i + ".txt");
		}

		StringBuilder sb = new StringBuilder();
		for (Fragment f : fs) {
			// ArrayList<String> arr = rs[f.mid].results.get(f.mrank);
			// arr.forEach(s -> sb.append(s + "\n"));
		}

		common.saveStringtoFile(sb.toString(), outfolder + "p3_out_finalresult.txt");

		long t3 = System.currentTimeMillis();

		System.out.printf("Completed. Execution time: %d ms, meger time: %d ms. \n", t2 - t1, t3 - t2);
		System.out.println("Results are saved to: " + outfolder + "\n");
	}

}
