package fragmentation;

import java.util.ArrayList;
import java.util.Random;

/**
 * A merged tree is a tree that formed by merging a list of fragments.
 * 
 * @author Hao
 *
 */
public class MergedTree {

	/**
	 * A unique integer for a root-merged tree.
	 */
	public int mid;

	/**
	 * The list of fragments this merged tree holds.
	 */
	public ArrayList<Fragment> fragments;

	public MergedTree(int mid) {
		this.mid = mid;
		this.fragments = new ArrayList<Fragment>();
	}

	public static MergedTree[] createTrees(ArrayList<Fragment> fs) {
		int ns = 0;
		for (Fragment f : fs)
			ns = f.mid > ns ? f.mid : ns;

		MergedTree[] trees = new MergedTree[ns + 1];
		for (int i = 0; i < trees.length; i++)
			trees[i] = new MergedTree(i);
		fs.forEach(f -> trees[f.mid].fragments.add(f));
		return trees;
	}

	/**
	 * generate N merged trees from a list of fragments.
	 * 
	 * @param fs
	 * @param N
	 * @return
	 */
	public static MergedTree[] createTrees(ArrayList<Fragment> fragments, int N, int seed) {
		// replicate fragments for shuffling to avoid changing the original order.
		ArrayList<Fragment> fs = new ArrayList<>();
		fragments.forEach(fs::add);
		java.util.Collections.shuffle(fs, new Random(seed));

		// declare and initialize a merged tree array
		MergedTree[] trees = new MergedTree[N];
		for (int i = 0; i < trees.length; i++)
			trees[i] = new MergedTree(i);

		// randomize
		for (int i = 0; i < fs.size(); i++)
			trees[i % N].fragments.add(fs.get(i));

		// arrange fragments of a merged tree in order of fid
		for (int i = 0; i < trees.length; i++)
			java.util.Collections.sort(trees[i].fragments, (a, b) -> a.fid > b.fid ? 1 : -1);

		// compute pre index
		for (MergedTree tree : trees) {
			int mpre = 2;
			for (Fragment f : tree.fragments) {
				f.rootPath.get(0).mpre = 1;
				f.firstRoot.mpre = mpre + 1;
				for (int i = 1; i < f.rootPath.size(); i++) {
					mpre++;
					f.rootPath.get(i).mpre = mpre;
				}
				f.mpre = mpre;
				f.gpre = f.firstRoot.gpre;
				mpre += f.size;
			}
		}

		return trees;
	}

	public static FragmentIndex[] getInfo(MergedTree[] trees) {
		FragmentIndex[] fis = new FragmentIndex[MergedTree.getTreeFragmentSize(trees)];

		for (int i = 0; i < trees.length; i++) {
			for (int j = 0; j < trees[i].fragments.size(); j++) {
				Fragment f = trees[i].fragments.get(j);
				fis[f.fid] = new FragmentIndex(f.fid, trees[i].mid, j, f.size, f.gpre, f.mpre);
			}
		}

		return fis;
	}

	public static int getTreeFragmentSize(MergedTree[] trees) {
		int size = 0;
		for (int i = 0; i < trees.length; i++)
			size += trees[i].fragments.size();
		return size;
	}

	/**
	 * A formated string representing the path to the root. such as
	 * "1:site;2:regions;5536:namerica;2".
	 * 
	 * @return
	 */
	public String toPathString() {
		StringBuilder sb = new StringBuilder();
		for (Fragment f : fragments) {
			String path = f.rootPath.stream().map(n -> String.format("%d:%s", n.gpre, n.name))
					.reduce((result, current) -> result + ";" + current).get();
			sb.append(path + ";" + f.fid + "\n");
		}
		return sb.toString();
	}

}
