package fragmentation.tests;

import java.util.ArrayList;

import basex.BXClient;
import basex.DBInfo;
import fragmentation.FContext;
import fragmentation.MergedTree;
import fragmentation.Node;
import fragmentation.Fragment;
import fragmentation.FProcessor;

public class MappingTest {

	public static void main(String[] args) throws Exception {
		FContext fc = FContext.parse(args);
		fc.maxsize = 5000;
		fc.db = "xmark0.01";
		System.out.println(fc);
		FProcessor frag = new FProcessor(fc);

		System.out.println("Applying fragmentation...");
		ArrayList<Fragment> fs = frag.apply(new Node(1));
		MergedTree[] trees = MergedTree.createTrees(fs, fc.N);

		for (int i = 0; i < trees.length; i++) {
			System.out.println(i + "=====================");
			MergedTree tree = trees[i];

			for (int j = 0; j < tree.fragments.size(); j++) {
				Fragment fj = tree.fragments.get(j);
				System.out.println("Fragment ID: " + fj.fid + "-----------------------");
				Node nj = Node.createRoot(fj.subtrees);
				Node.output(nj);
			} 

		}
		test(fc);
	}

 

	public static void test(FContext fc) throws Exception {
		DBInfo[] dbs = fc.getDBInfo();
		BXClient bx = BXClient.open(fc.ip);
		bx.execute("set mainmem on");
		for (int i = 0; i < dbs.length; i++) {
			String cmd = String.format("xquery create db {0} {1}", dbs[i].dbname, dbs[i].getFullpath());
			bx.execute(cmd);
		}
	}

}
