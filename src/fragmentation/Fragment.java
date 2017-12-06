package fragmentation;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

	public List<String> results;

	/**
	 * A unique integer representing this fragment.
	 */
	public int fid;

	/**
	 * The id of merged tree.
	 */
	public int mid;

	/**
	 * The position in the merged tree.
	 */
	public int mrank;

	/**
	 * Number of all nodes in this fragment, including element nodes, attribute
	 * nodes and content nodes.
	 */
	public int size;

	/**
	 * The PRE index on the pruned tree.
	 */
	public int gpre;

	/**
	 * the PRE value of the current node on the merged tree.
	 */
	public int mpre;

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
		results = new ArrayList<String>();
	}

	public void add(Node node) {
		subtrees.add(node);
		size += node.elementSize + 1;
	}

	public String toString() {
		return String.format("fid=%d,mid=%d,mpre=%d,mrank=%d,size=%d", fid, mid, mpre, mrank, size);
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

		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1); // remove last '\n'.

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

	public static void writeSubtreesGPREs(ArrayList<Fragment> fs, String filepath) throws Exception {
		RandomAccessFile rf = new RandomAccessFile(new File(filepath), "rw");
		for (Fragment f : fs) {
			rf.writeInt(f.subtreegpres.length);
			byte[] bts = new byte[f.subtreegpres.length * 4];
			for (int i = 0; i < f.subtreegpres.length; i++) {
				int a = f.subtreegpres[i];
				bts[i * 4 + 0] = (byte) ((a >> 24) & 0xFF);
				bts[i * 4 + 1] = (byte) ((a >> 16) & 0xFF);
				bts[i * 4 + 2] = (byte) ((a >> 8) & 0xFF);
				bts[i * 4 + 3] = (byte) ((a >> 0) & 0xFF);
			}
			rf.write(bts);
		}
		rf.close();
	}

	public static ArrayList<Fragment> readFragmentList(String path) throws Exception {
		ArrayList<Fragment> fs = new ArrayList<Fragment>();

		// read fragment index
		for (String s : Files.readAllLines(Paths.get(path + File.separator + "fragmentindex.txt"))) {
			Fragment f = Fragment.parse(s);
			if (f != null)
				fs.add(f);
		}

		// read subtrees
		List<int[]> its = readIntArray(path + File.separator + "subtreeroots.bin");
		for (int i = 0; i < its.size(); i++)
			fs.get(i).subtreegpres = its.get(i);

		// read path info. format: 1:site;2:regions;561994:namerica:2
		List<String> lines = Files.readAllLines(Paths.get(path + File.separator + "prunedtree.txt"));
		for (int i = 0; i < lines.size(); i++) {
			fs.get(i).rootPath = new ArrayList<Node>();
			for (String s : lines.get(i).split(";")) {
				Node n = new Node();
				n.gpre = Integer.parseInt(s.split(":")[0]);
				n.name = s.split(":")[1];
				fs.get(i).rootPath.add(n);
			}
		}

		return fs;
	}

	static List<int[]> readIntArray(String filepath) throws Exception {
		List<int[]> list = new ArrayList<>();

		RandomAccessFile rf = new RandomAccessFile(new File(filepath), "rw");
		try {
			// when encounter an exception, it means the reading reaches the end of file
			// and the loop will terminate.
			while (true) {
				int len = rf.readInt();

				byte[] bts = new byte[len * 4];
				rf.read(bts);

				int[] its = new int[len];
				for (int i = 0; i < len; i++)
					its[i] = ((bts[i * 4 + 0] & 0xFF) << 24) + ((bts[i * 4 + 1] & 0xFF) << 16)
							+ ((bts[i * 4 + 2] & 0xFF) << 8) + (bts[i * 4 + 3] & 0xFF);

				list.add(its);
			}
		} catch (Exception e) {
		}
		rf.close();

		return list;
	}

	/**
	 * Initialize a fragment from a formated string. Example:
	 * fid=0,mid=4,mrank=0,size=249487,gpre=3,mpre=3
	 * 
	 * @param s
	 * @return
	 */
	public static Fragment parse(String s) {
		if (s == null || s.length() == 0)
			return null;

		Fragment f = new Fragment();
		try {
			String[] strs = s.split(",");
			f.fid = Integer.parseInt(strs[0].split("=")[1]);
			f.mid = Integer.parseInt(strs[1].split("=")[1]);
			f.mrank = Integer.parseInt(strs[2].split("=")[1]);
			f.size = Integer.parseInt(strs[3].split("=")[1]);
			f.gpre = Integer.parseInt(strs[4].split("=")[1]);
			f.mpre = Integer.parseInt(strs[5].split("=")[1]);
		} catch (Exception e) {
			f = null;
		}

		return f;
	}

}
