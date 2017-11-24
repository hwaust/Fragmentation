package fragmentation.exe;

import java.util.ArrayList;

import fragmentation.*;

/**
 * Used to create a list of merged trees and related information from a BaseX
 * database.
 * 
 * @author Hao
 *
 */
public class TreeMaker {

	public static void main(String[] args) throws Exception {
		args = args.length == 0 ? "-db,xmark80,-ms,200000,-export,off".split(",") : args;
		// args = args.length == 0 ? "-db,xmark600,-ms,20000000,-export,off".split(",") : args;

		FContext fc = FContext.parse(args);
		FProcessor proc = new FProcessor(fc);

		/*** Step 1: apply fragmentation: an input tree -> N root-merged trees ****/
		System.out.println("Ver.1.0: " + fc + "Applying fragmentation...");
		long t1 = System.currentTimeMillis();
		ArrayList<Fragment> fs = proc.apply(new Node(1));
		MergedTree[] trees = MergedTree.createTrees(fs, fc.N);
		long t2 = System.currentTimeMillis();
		System.out.printf("Time cost: %d s.\n", (t2 - t1) / 1000);

		/**************** Step 2: output data ***************/
		System.out.println("Saving merged trees and related information...");
		// 2.1 output pruned tree
		Fragment.saveInfo(fs, fc.getFullPath("prunedtree.txt"));

		// 2.2 output fragments information
		FragmentInfo.writeAllInfo(MergedTree.getInfo(trees), fc.getFullPath("fragmentindex.txt"));
		long t3 = System.currentTimeMillis();

		// 2.3 output merged trees if isExported is true.
		if (fc.needExport) {
			for (int i = 0; i < trees.length; i++)
				proc.makeXDocs1(trees[i], fc.getFullPath("mfrag" + i + ".xml"));
		}

		System.out.printf("Time cost: %d s.\nOutput directory: %s\n", (t3 - t2) / 1000, fc.getFullPath(""));
	}

}
