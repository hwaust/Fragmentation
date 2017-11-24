package fragmentation;

import java.util.ArrayList;
import java.util.Random;

import basex.common;

/**
 * A fragment is a list of subtrees rooted at the same node nr with the path
 * from nr to the root of the whole tree, where the size of nr is smaller than a
 * fixed number. A subtree of a tree T is a tree consisting of a node in T and
 * all of its descendants in T.
 * 
 * @author Hao
 *
 */
public class Fragment {

	/**
	 * A unique integer representing this fragment.
	 */
	public int fid;

	/**
	 * The PRE index on the pruned tree.
	 */
	public int gpre;

	/**
	 * the PRE value of the current node on the merged tree.
	 */
	public int mpre;

	/**
	 * Number of all nodes in this fragment, including element nodes, attribute
	 * nodes and content nodes.
	 */
	public int size;

	/**
	 * A path from subtree to the root of the whole tree.
	 */
	public ArrayList<Node> rootPath;

	/**
	 * The root of the first subtree.
	 */
	public Node firstRoot;

	/**
	 * a list of subtrees of type FNode
	 */
	public ArrayList<Node> subtrees;

	public Fragment() {
		size = 0;
		subtrees = new ArrayList<Node>();
		rootPath = new ArrayList<Node>();
	}

	public void add(Node node) {
		subtrees.add(node);
		size += node.elementSize + 1;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("total size=%d, ", this.size));
		if (subtrees.size() > 0)
			sb.append(subtrees.get(0));

		return sb.toString();
	}

	public Node get(int i) {
		if (i >= 0 && i < subtrees.size())
			return subtrees.get(i);
		return null;
	}

	public ArrayList<Node> getparents() {
		ArrayList<Node> parents = new ArrayList<Node>();
		String lastPath = "";
		for (Node node : subtrees) {
			if (node.parent != null && !node.parent.getPath().equals(lastPath)) {
				parents.add(node.parent);
				lastPath = node.parent.getPath();
			}
		}
		return parents;
	}

	public int size() {
		return subtrees.size();
	}

	public int dsize() {
		int dsize = 0;
		for (Node node : subtrees)
			dsize += node.elementSize;
		return dsize;
	}

	public int getLevel() {
		Node p = this.subtrees.get(0).parent;
		int lv = 0;
		while (p.parent != null) {
			p = p.parent;
			lv++;
		}

		return lv;
	}

	public String getPathToRoot() {
		StringBuilder sb = new StringBuilder();
		Node p = this.subtrees.get(0);
		// sb.append(p.pre);
		while (p.parent != null) {
			p = p.parent;
			sb.insert(0, p.gpre + ".");
		}

		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public void makeRootPath() {
		rootPath.clear();
		Node n = subtrees.get(0);
		while (n.parent != null) {
			n = n.parent;
			Node tmp = n.clone();
			if (rootPath.size() > 0)
				tmp.addChild(rootPath.get(0));
			rootPath.add(0, tmp);
		}
		firstRoot = subtrees.get(0);
	}

	/**
	 * Randomize the order of a list of fragments.
	 * 
	 * @param fs
	 *            A list of fragments.
	 */
	public static void randomize(ArrayList<Fragment> fs) {
		Random rnd = new Random();
		for (int i = 0; i < fs.size(); i++) {
			int p = rnd.nextInt(fs.size());
			Fragment temp = fs.get(i);
			fs.set(i, fs.get(p));
			fs.set(p, temp);
		}
	}

	public static void saveInfo(ArrayList<Fragment> fs, String fullPath) {
		StringBuilder sb = new StringBuilder();
		for (Fragment f : fs) {
			String path = f.rootPath.stream().map(n -> String.format("%d:%s", n.gpre, n.name))
					.reduce((result, current) -> result + ";" + current).get();
			sb.append(path + ":" + f.fid + "\n");
		}
		common.saveStringtoFile(sb.toString(), fullPath);
	}

	public int[] subtreegpres;

	public void completeFragment() {
		// compute path and size.
		makeRootPath();

		// reduce subtrees from nodes to integers.
		subtreegpres = new int[subtrees.size()];

		size = 0;
		for (int i = 0; i < subtreegpres.length; i++) {
			Node n = subtrees.get(i);
			subtreegpres[i] = n.gpre;
			size += n.elementSize + 1 + n.attributeSize + n.contentSize;
		}

		subtrees.clear();

		// System.gc();
	}

}
