package fragmentation.exe;

import java.util.ArrayList;

import fragmentation.FContext;
import fragmentation.FProcessor;
import fragmentation.Fragment;
import fragmentation.MergedTree;

public class TreeExporter {

	public static void main(String[] args) throws Exception {
		args = args.length == 0 ? "-db,xmark10,-ms,200000".split(",") : args;

		FContext fc = FContext.parse(args);
		FProcessor proc = new FProcessor(fc);

		/*** Step 1: apply fragmentation: an input tree -> N root-merged trees ****/
		System.out.println("Ver.1.0: " + fc + "Loading...");
		long t1 = System.currentTimeMillis();
		ArrayList<Fragment> fs = Fragment.readFragmentList(fc.getFullPath(""));
		MergedTree[] trees = MergedTree.createTrees(fs, fc.Ns);

		/**************** Step 2: output data ***************/
		System.out.println("Saving merged trees and related information...");
		for (int i = 0; i < trees.length; i++)
			proc.writeXMLDocuments(trees[i], fc.getFullPath("mfrag" + i + ".xml"));

		long t2 = System.currentTimeMillis();
		System.out.printf("Time cost for exportation: %d s.\nOutput directory: %s\n", (t2 - t1) / 1000,
				fc.getFullPath(""));
	}

}
