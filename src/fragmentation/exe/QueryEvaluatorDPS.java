package fragmentation.exe;

import java.io.File;
import java.util.ArrayList;
import basex.BXClient;
import basex.PExecutor;
import basex.QueryResultPre;
import basex.ServerSide;
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
			args = new String[] { "-iplist", "#4", "-dblist", "range:mfrag:0-3", "-key", "xm4c.dps", "-f",
					"D:\\data\\fragments\\xmark1_4_20K_20171126", "-p", "4", "-debug", "off", "-serial", "on" };

		QContext c = QContext.parse(args);
		System.out.println("Processing " + c);

		int Ns = c.dbs.length; // number of merged trees (or workers)
		int P = c.p; // number of partitions

		// load fragments and trees
		ArrayList<Fragment> fs = Fragment.readFragmentList(c.datafolder);
		MergedTree[] trees = MergedTree.createTrees(fs);

		// for making prefix and suffix queries
		ServerSide[] sss = new ServerSide[Ns];
		for (int i = 0; i < sss.length; i++)
			sss[i] = new ServerSide(c.dbs[i]);

		// for storing output data.
		String outfolder = c.makeOutputFolder();
		PExecutor.outputfolder = outfolder + File.separator + "logs";
		new File(PExecutor.outputfolder).mkdirs(); 
		
		// in charge of parallel executing.
		PExecutor[] pes = new PExecutor[Ns];
		for (int i = 0; i < Ns; i++) {
			pes[i] = new PExecutor(BXClient.open(c.ips[i]));
			pes[i].tag = "PRE_" + i;
		}

		// prefix queries
		String[] prefixes = new String[Ns];
		for (int i = 0; i < Ns; i++) {
			prefixes[i] = sss[i].getPrefix(c.query, P);
		}

		// suffix queries
		String[][] suffixes = new String[Ns][];
		for (int i = 0; i < prefixes.length; i++)
			suffixes[i] = sss[i].getSuffix(c.query, P);

		// execute three commands: "set mainmem on; drop tmpdb; set new tmpdb;"
		System.out.println("Prepareing temporary databases...");
		for (int i = 0; i < Ns; i++)
			sss[i].prepare(pes[i].bx);

		/*******************************************
		 * 
		 * Execute prefix queries
		 * 
		 *******************************************/
		for (int i = 0; i < pes.length; i++) {
			pes[i].setQuery(prefixes[i], 0);
		}

		System.out.println("Executing prefix queries...");
		long Tprefix = c.serial ? PExecutor.serialRun(pes) : PExecutor.parallelRun(pes);

		// Save intermediate databases if in debug node.
		if (c.debug) {
			System.out.println("Saving temporary databases...");
			for (int i = 0; i < pes.length; i++) {
				String q0 = String.format("xquery db:open('" + c.dbs[i] + "_tmp')", i);
				String r = pes[i].bx.execute(q0);
				common.saveStringtoFile(r, outfolder + "TEMPDB_" + i + ".txt");
			}
		}

		System.out.println("Prefix query done.");
		/*******************************************
		 * 
		 * Process suffix query
		 * 
		 *******************************************/

		// execute and store the original output of P1 if in debug node.
		if (c.debug) {
			System.out.println("Saving output of P1");
			for (int i = 0; i < Ns; i++) {
				for (int j = 0; j < P; j++) {
					String data = pes[i].bx.execute(suffixes[i][j]);
					String fileij = outfolder + String.format("PT1_OUTPUT_T%d_P%d_org.txt", i, j);
					common.saveStringtoFile(data, fileij);
				}
			}
		}

		System.out.println("Evaluating suffix queries...");
		PExecutor[][] pess = new PExecutor[Ns][];
		for (int i = 0; i < Ns; i++) {
			pess[i] = new PExecutor[P];
			for (int j = 0; j < P; j++) {
				pess[i][j] = new PExecutor(BXClient.open(c.ips[i]), 1, suffixes[i][j]);
				pess[i][j].tag = String.format("SUFFIX_T_%d_F_%d", i, j);
			}
		}

		long Tsuffix = c.serial ? PExecutor.serialRun(PExecutor.toArray(pess))
				: PExecutor.parallelRun(PExecutor.toArray(pess));
		System.out.println("Evaluation of Suffix queries done.");

		/*******************************************
		 * 
		 * Merge results
		 * 
		 *******************************************/
		System.out.println("Starting merging...");
		QueryResultPre[][] rs = new QueryResultPre[Ns][];
		for (int i = 0; i < Ns; i++) {
			rs[i] = new QueryResultPre[P];
			for (int j = 0; j < P; j++) {
				rs[i][j] = (QueryResultPre) pess[i][j].sr;
			}
		}

		// Save Results for the first step if in debug node.
		if (c.debug) {
			for (int i = 0; i < Ns; i++) {
				for (int j = 0; j < P; j++) {
					QueryResultPre rij = rs[i][j];
					StringBuilder sb = new StringBuilder();
					for (int k = 0; k < rij.pres.size(); k++)
						sb.append(rij.values.get(k) + "\r\n");
					String filename = String.format("%sPT2_OUTPUT_T%d_P%d.txt", outfolder, i, j);
					common.saveStringtoFile(sb.toString(), filename);
				}
			}
		}

		int totalsize = 0;
		int totallen = 0;
		for (int i = 0; i < rs.length; i++)
			for (int j = 0; j < rs[i].length; j++) {
				totalsize += rs[i][j].size();
				for (int k = 0; k < rs[i][j].values.size(); k++) {
					totallen += rs[i][j].values.get(k).length();
				}
			}
		System.out.println("totals=" + totalsize + ", total length=" + totallen);

		System.out.println("Regrouping nodes...");
		long Tmerge = System.currentTimeMillis();
		for (int i = 0; i < trees.length; i++) {
			MergedTree tree = trees[i];
			int pos = 0;
			for (int k = 0; k < P; k++) {
				QueryResultPre rd = rs[i][k];
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
		Tmerge = System.currentTimeMillis() - Tmerge;

		// Save the final result if in debug node.
		System.out.println("Saving results...");
		long Tsave = System.currentTimeMillis();
		Fragment.save(fs, outfolder + "P3_OUTPUT_FinalResult.txt");
		Tsave = System.currentTimeMillis() - Tsave;

		System.out.printf("Tprefix=%d ms, Tsufix=%d ms, Tmerge=%d ms, Tsave=%d ms ==> %s\n", Tprefix, Tsuffix, Tmerge,
				Tsave, outfolder);
	}

}
