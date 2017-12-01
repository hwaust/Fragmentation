package basex;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import fragmentation.Node;

public class BaseXHelper {

	public String server;
	public String db;
	BXClient bx;

	// the max number of children of a node can be processed at a time.
	public static int splitSize = 1024 * 1024;

	public BaseXHelper(String server, String database) {
		this.server = server;
		this.db = database;
	}

	public void open() throws Exception {
		bx = BXClient.open(server);
	}

	public void close() throws Exception {
		bx.close();
	}

	public String getQuery(String pre, String query) {
		return String.format("xquery db:open-pre('%s', %s)%s", db, pre, query);
	}

	public String getQueryIndexform(String pre, String query) {
		return String.format("xquery db:node-pre(db:open-pre('%s', %s)%s)", db, pre, query);
	}

	public String execute(String query) throws Exception {
		return bx.execute(query);
	}

	public List<Node> getChildren(Node node) throws Exception {
		// to be returned.
		ArrayList<Node> nodes = new ArrayList<Node>();

		// step 1: get distinct names of node's children.
		String steps = node.getLocationSteps();
		String q0 = String.format("xquery distinct-values(db:open('%s')%s/*/name())", db, steps);
		String[] names = common.split2lines(bx.execute(q0));

		// process each name
		for (String name : names) {
			String q1 = String.format("xquery count(db:open('%s')%s/%s)", db, steps, name);
			int[] sects = common.splitArray(Integer.parseInt(bx.execute(q1)), splitSize);

			// split a node with children more than splitSize.
			for (int i = 1; i < sects.length; i++) {
				String query = String.format("xquery for $node in db:open('%s')%s/%s", db, steps, name);
				if (sects.length > 2)
					query += String.format("[position() = %d to %d]", sects[i - 1] + 1, sects[i]);
				query += " return (db:node-pre($node) || ',' || count($node//*) || ',' || count($node//@*)";
				query += " || ',' || count($node//*/text()) || ',' || $node/name() )";
				String[] strs = common.split2lines(bx.execute(query));
				for (String s : strs) {
					Node n = new Node();
					node.addChild(n);
					n.gpre = Integer.parseInt(s.split(",")[0]);
					n.elementSize = Integer.parseInt(s.split(",")[1]);
					n.attributeSize = Integer.parseInt(s.split(",")[2]);
					n.contentSize = Integer.parseInt(s.split(",")[3]);
					n.name = s.split(",")[4];
					nodes.add(n);
				}
			}
		}

		return nodes;
	}

	public int getDescendantSize(String pre) throws Exception {
		String query = String.format("xquery count(db:open-pre('%s', %s)%s)", db, pre, "//*");
		return Integer.parseInt(bx.execute(query));
	}

	public ArrayList<Integer> getParent(int pre) throws Exception {
		ArrayList<Integer> path = new ArrayList<Integer>();
		String parent = pre + "";
		while (!parent.equals("1")) {
			parent = bx.execute(this.getQueryIndexform(parent + "", "/parent::*"));
			if (parent.length() == 0)
				break;
			path.add(Integer.parseInt(parent));
		}
		return path;
	}

	public String getNodeName(String pre) throws Exception {
		return bx.execute(getQuery(pre, "/name()"));
	}

	public String getNodeContent(String pre) throws Exception {
		return bx.execute(getQuery(pre, ""));
	}

	public String[] getChildPres(String pre) throws Exception {
		String pres = bx.execute(getQueryIndexform(pre, "/*"));
		String[] chs = common.split2lines(pres);
		chs = chs.length == 1 ? chs[0].split("\n") : chs;
		return chs;

	}

//	public void writeTrees(ArrayList<Node> subtrees, FileWriter fw) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		sb.append("xquery for $pre in (");
//		for (int i = 0; i < subtrees.size(); i++)
//			sb.append(subtrees.get(i).gpre + ",");
//		sb.setCharAt(sb.length() - 1, ')');
//		sb.append(String.format("return db:open-pre('%s', $pre)", db));
//		bx.executeToFile(sb.toString(), fw);
//	}

	public int getRootSize() throws Exception {
		return Integer.parseInt(bx.execute("xquery count(db:open('" + db + "')//*)"));
	}

	public List<Node> process(Node root) throws Exception {
		List<Node> nodes = new ArrayList<Node>();
		String[] names = { "regions", "categories", "catgraph", "people", "open_auctions", "closed_auctions" };

		for (int i = 0; i < names.length; i++) {
			String query = String.format("xquery for $node in db:open('%s')/site/%s", db, names[i]);
			query += " return (db:node-pre($node) || ',' || count($node//*) || ',' || count($node//@*)";
			query += " || ',' || count($node//*/text()) || ',' || $node/name() )";

			String results = bx.execute(query);
			String[] strs = common.split2lines(results);
			for (String s : strs) {
				Node n = new Node();
				root.addChild(n);
				n.gpre = Integer.parseInt(s.split(",")[0]);
				n.elementSize = Integer.parseInt(s.split(",")[1]);
				n.attributeSize = Integer.parseInt(s.split(",")[2]);
				n.contentSize = Integer.parseInt(s.split(",")[3]);
				n.name = s.split(",")[4];
				nodes.add(n);
			}

			System.out.println(nodes.size() + ", " + query);

		}

		return nodes;
	}

	public void execute(String query, FileWriter fw) throws Exception{ 
		bx.execute(query, fw);
	}

 
}
