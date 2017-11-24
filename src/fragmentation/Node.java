package fragmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * A tree node used for merged tree.
 * 
 * @author Hao
 *
 */
public class Node {
	/**
	 * The PRE value on the original tree.
	 */
	public int gpre;

	/**
	 * The PRE value on the merged tree.
	 */
	public int mpre;

	public ArrayList<Node> children;

	public Node parent;

	/**
	 * Tag name used to recover the whole tree in XML
	 */
	public String name;

	/**
	 * The number of all element nodes.
	 */
	public int elementSize;

	/**
	 * The number of all attribute nodes.
	 */
	public int attributeSize;

	/**
	 * The number of all content nodes.
	 */
	public int contentSize;

	/**
	 * The list of children in PRE values.
	 */
	public String[] subtreePREs;

	/**
	 * Total number of nodes, including all three node types.
	 */
	public int size;

	public Node() {
		children = new ArrayList<Node>();
		subtreePREs = new String[0];
	}

	public Node(int pre) {
		this.gpre = pre;
		children = new ArrayList<Node>();
		subtreePREs = new String[0];
	}

	public void addChild(Node node) {
		children.add(node);
		node.parent = this;
	}

	public String toPREString() {
		return gpre + "";
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("pre=%d, descendants.size=%d, ", gpre, elementSize));

		if (this.subtreePREs != null)
			sb.append("child size(str)=" + this.subtreePREs.length);
		if (children.size() == 0)
			sb.append("child.size=0, ");
		else
			sb.append(String.format("children.size=%d (first.pre=%d), ", children.size(), children.get(0).gpre));

		sb.append("path=" + getPath());

		return sb.toString();
	}

	public String getPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.gpre + "");
		Node p = this.parent;
		while (p != null) {
			sb.insert(0, p.gpre + ".");
			p = p.parent;
		}
		return sb.toString();
	}

	public Node lastChild() {
		return children.size() == 0 ? null : children.get(children.size() - 1);
	}

	public int size() {
		int size = 1;
		for (int i = 0; i < children.size(); i++)
			size += children.get(i).size();
		return size;
	}

	public void print() {
		Node.print(this, 0);
	}

	public static void print(Node node, int level) {
		StringBuilder head = new StringBuilder();
		for (int i = 0; i < level; i++)
			head.append("  ");
		System.out.println(head + node.toString());

		if (node.subtreePREs != null) {
			System.out.println(head + node.toString() + "***");
		} else {
			for (Node ch : node.children)
				print(ch, level + 1);
		}
	}

	/**
	 * Returns a clone of this node.
	 */
	public Node clone() {
		Node node = new Node();
		node.name = this.name;
		node.gpre = this.gpre;
		node.elementSize = this.elementSize;
		node.attributeSize = this.attributeSize;
		node.contentSize = this.contentSize;
		node.size = this.size;
		node.subtreePREs = this.subtreePREs;
		return node;
	}

	/**
	 * Create a list of nodes for a string in format: "id.id.id", where id is the
	 * PRE value and "." is the delimiter.
	 */
	public static List<Node> parseNodes(String s) {
		if (s == null || s.length() == 0)
			return null;

		String[] strs = s.split("\\.");

		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < strs.length; i++)
			nodes.add(new Node(Integer.parseInt(strs[i])));

		return nodes;
	}

	/**
	 * Create a tree from a list of paths. A path is formatted as pre1.pre2.pre3,
	 * where pre1, pre2, and pre3 are concatenated integers delimited by dots.
	 * 
	 * @param lines
	 *            a list a strings representing input paths.
	 * @return The root of the created tree.
	 */
	public static Node makeTree(List<String> lines) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("1", 1);
		Node root = new Node(1);
		for (String s : lines) {
			Node cur = root;
			List<Node> nodes = Node.parseNodes(s);
			for (int i = 1; i < nodes.size(); i++) {
				// To remove repeated paths.
				map.putIfAbsent(nodes.get(i).gpre + "", nodes.get(i).gpre);

				Node last = cur.lastChild();
				if (last == null) {
					cur.addChild(nodes.get(i).clone());
					cur = cur.lastChild();
				} else if (last.gpre == nodes.get(i).gpre) {
					cur = last;
				} else {
					cur.addChild(nodes.get(i).clone());
					cur = last;
				}
			}
		}

		return root;
	}

	/**
	 * 1. set root as the starting node. 2. get all unique paths of subtrees. 3.
	 * merge all unique paths into a whole tree.
	 * 
	 * @param subtrees
	 * @return
	 */
	public static Node createRoot(ArrayList<Node> subtrees) {
		Node root = new Node(1);
		for (Node n : subtrees) {
			Node cur = root;
			List<Node> nodes = Node.parseNodes(n.getPath());
			nodes = n.getSelfandAncestors();

			for (int i = 1; i < nodes.size(); i++) {
				Node last = cur.lastChild();
				if (last == null) {
					cur.addChild(nodes.get(i).clone());
					cur = cur.lastChild();
				} else if (last.gpre == nodes.get(i).gpre) {
					cur = last;
				} else {
					cur.addChild(nodes.get(i).clone());
					cur = last;
				}
			}
		}

		return root;
	}

	List<Node> getSelfandAncestors() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		Node p = this;
		while (p != null) {
			nodes.add(p);
			p = p.parent;
		}
		return nodes;
	}

	public int childsize() {
		return this.subtreePREs.length > 0 ? subtreePREs.length : children.size();
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	/**
	 * Top part does not include leaves.
	 * 
	 * @param node
	 * @return
	 */
	public static int getTopsize(Node node) {
		if (node.isLeaf())
			return 0;

		int tsize = 1;
		for (Node n : node.children)
			tsize += Node.getTopsize(n);

		return tsize;
	}

	public static int getMaxDepth(Node node, int depth) {
		if (node.isLeaf())
			return depth;

		int maxDepth = 1;
		for (Node n : node.children) {
			int d = Node.getMaxDepth(n, depth + 1);
			maxDepth = maxDepth < d ? d : maxDepth;
		}
		return maxDepth;
	}

	public static void output(Node node) {
		Stack<Node> stack = new Stack<Node>();
		stack.push(node);
		StringBuilder sb = new StringBuilder();

		while (!stack.isEmpty()) {
			Node n = stack.pop();

			if (n.isLeaf()) {
				sb.append(n.getPath() + ":");
				sb.append(n.elementSize + "\n");
			} else {
				sb.append(n.getPath() + "\n");
				for (int i = n.children.size() - 1; i >= 0; i--)
					stack.push(n.children.get(i));
			}

		}

		System.out.println(sb.toString());

	}

	public String getLocationSteps() {
		StringBuilder sb = new StringBuilder();
		sb.append("/" + name);

		Node tmp = this;
		while (tmp.parent != null) {
			tmp = tmp.parent;
			sb.insert(0, "/" + tmp.name);
		}

		return sb.toString();
	}

}
