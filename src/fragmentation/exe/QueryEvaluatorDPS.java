package fragmentation.exe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import basex.BXClient;
import basex.PExecutor;
import basex.QueryResult_IntStringList;
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
		QContext c = QContext.parse(args);
		System.out.println("Processing " + c + "...");

		// Because a line break is "\r\n" on Windows but "\n" on Linux
		// in the returned results from a BaseX server depending where the
		// BaseX server is on, they are thus treated accordingly.
		BXClient.isTargetServerWinows = QContext.isWin;
		int Ns = c.dbs.length; // number of merged trees or BaseX servers
		int P = c.p; // number of partitions

		// load fragments and trees
		ArrayList<Fragment> fs = Fragment.readFragmentList(c.datafolder);
		MergedTree[] trees = MergedTree.createTrees(fs);

		// for making prefix and suffix queries
		ServerSideEx[] sss = new ServerSideEx[Ns];
		for (int i = 0; i < sss.length; i++)
			sss[i] = new ServerSideEx(c.dbs[i]);

		// for storing output data.
		String outfolder = c.makeOutputFolder();

		// in charge of parallel executing.
		PExecutor[] pes = new PExecutor[Ns];
		for (int i = 0; i < Ns; i++)
			pes[i] = new PExecutor(BXClient.open(c.ips[i], true));

		// prefix queries
		String[] prefixes = new String[Ns];
		for (int i = 0; i < Ns; i++)
			prefixes[i] = sss[i].getPrefix(c.query, P);

		// suffix queries
		String[][] suffixes = new String[trees.length][];
		for (int i = 0; i < prefixes.length; i++)
			suffixes[i] = sss[i].getSuffix(c.query, P);

		// execute three commands:
		// "set mainmem on; drop tmpdb; set new tmpdb;"
		for (int i = 0; i < Ns; i++)
			for (String q : sss[i].createTempdb().split(";"))
				pes[i].bx.execute(q);

		/*******************************
		 * 
		 * Execute prefix queries
		 * 
		 *******************************/

		for (int i = 0; i < pes.length; i++)
			pes[i].setQuery(prefixes[i], 1);
		long Tprefix = PExecutor.parallelRun(pes);

		// Save temp databases.
		for (int i = 0; i < pes.length; i++) {
			String q0 = String.format("xquery db:open('mfrag%d_tmp')", i);
			String r = pes[i].bx.execute(q0);
			common.saveStringtoFile(r, outfolder + "TEMPDB_" + i + ".txt");
		}

		Thread.sleep(1000);
		/*******************************
		 * 
		 * Process suffix query
		 * 
		 *******************************/

		// execute and store the original output of P1.
		for (int i = 0; i < Ns; i++) {
			for (int j = 0; j < P; j++) {
				String data = pes[i].bx.execute(suffixes[i][j]);
				String fileij = outfolder + String.format("PT1_OUTPUT_T%d_P%d_org.txt", i, j);
				common.saveStringtoFile(data, fileij);
			}
		}

		PExecutor[][] pess = new PExecutor[Ns][];
		for (int i = 0; i < Ns; i++) {
			pess[i] = new PExecutor[P];
			for (int j = 0; j < P; j++) {
				pess[i][j] = new PExecutor(BXClient.open(c.ips[i]), 1, suffixes[i][j]);
				System.out.printf("pess[%d][%d].query=%s\n", i, j, pess[i][j].xquery);
			}
		}

		// Convert PExecutor[][] to PExecutor[] for executing.
		pes = new PExecutor[Ns * P];
		for (int i = 0; i < Ns; i++)
			for (int j = 0; j < P; j++)
				pes[i * P + j] = pess[i][j];

		long Tsuffix = PExecutor.parallelRun(pes);

		/*******************************
		 * 
		 * Merge results
		 * 
		 *******************************/
		QueryResult_IntStringList[][] rs = new QueryResult_IntStringList[Ns][];
		for (int i = 0; i < Ns; i++) {
			rs[i] = new QueryResult_IntStringList[P];
			for (int j = 0; j < P; j++) {
				rs[i][j] = (QueryResult_IntStringList) pess[i][j].sr;
				System.out.printf("rs[%d][%d].pres.size()=%d, query=%s\n", i, j, rs[i][j].pres.size(),
						pess[i][j].xquery);
			}
		}

		// Save Results for the first step
		for (int i = 0; i < Ns; i++) {
			for (int j = 0; j < P; j++) {
				QueryResult_IntStringList rij = rs[i][j];
				StringBuilder sb = new StringBuilder();
				for (int k = 0; k < rij.pres.size(); k++)
					sb.append(rij.values.get(k) + "\r\n");
				String filename = String.format("%sPT2_OUTPUT_T%d_P%d.txt", outfolder, i, j);
				common.saveStringtoFile(sb.toString(), filename);
			}
		}

		for (int i = 0; i < trees.length; i++) {
			MergedTree tree = trees[i];
			int pos = 0;
			List<List<String>> values = new ArrayList<List<String>>();
			for (int t = 0; t < tree.fragments.size(); t++)
				values.add(new ArrayList<String>());

			for (int k = 0; k < P; k++) {
				QueryResult_IntStringList rd = rs[i][k];
				for (int j = 0; j < rd.pres.size(); j++) {
					while (pos < trees[i].fragments.size() - 1 && rd.pres.get(j) > trees[i].fragments.get(pos + 1).mpre)
						pos++;
					tree.fragments.get(pos).results.add(rd.values.get(j));
				}
			}

			StringBuilder sb = new StringBuilder();
			for (Fragment f : tree.fragments)
				for (String s : f.results)
					sb.append(s + "\n");
			common.saveStringtoFile(sb.toString(), outfolder + "P3_OUTPUT_T" + i + ".txt");
		}

		StringBuilder sb = new StringBuilder();
		for (Fragment f : fs)
			f.results.forEach(s -> sb.append(s + "\n"));
		common.saveStringtoFile(sb.toString(), outfolder + "P3_OUTPUT_FinalResult.txt");

		System.out.printf("Completed. Tprefix=%d ms; Tsufix=%d ms; \n", Tprefix, Tsuffix);
		System.out.println("Results are saved to: " + outfolder + "\n");
	}

}
