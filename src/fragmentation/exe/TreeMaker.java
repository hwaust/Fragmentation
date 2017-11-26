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
		args = args.length == 0 ? "-db,xmark10,-ms,200000".split(",") : args;

		FContext fc = FContext.parse(args);
		FProcessor proc = new FProcessor(fc);

		/*** Step 1: apply fragmentation: an input tree -> N root-merged trees ****/
		System.out.println("Ver.1.1: " + fc + "Applying fragmentation...");
		long t1 = System.currentTimeMillis();
		ArrayList<Fragment> fs = proc.apply(new Node(1));
		MergedTree[] trees = MergedTree.createTrees(fs, fc.Ns, fc.seed);
		long t2 = System.currentTimeMillis();
		System.out.printf("Time cost for fragmentation: %d s.\n", (t2 - t1) / 1000);

		/**************** Step 2: output data ***************/
		System.out.println("Saving merged trees and related information...");
		// 2.1 output pruned tree
		Fragment.saveInfo(fs, fc.getFullPath("prunedtree.txt"));

		// 2.2 output fragments information
		FragmentIndex.writeAllInfo(MergedTree.getInfo(trees), fc.getFullPath("fragmentindex.txt"));

		// 2.3 output the root gpre of subtrees.
		Fragment.writeSubtreesGPREs(fs, fc.getFullPath("subtreeroots.bin"));
		long t3 = System.currentTimeMillis();
		
		System.out.printf("Time cost for exportation: %d s.\nOutput directory: %s\n", (t3 - t2) / 1000,
				fc.getFullPath(""));
	}

}
