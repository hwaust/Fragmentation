package fragmentation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import basex.BaseXHelper;
import basex.common;

public class FProcessor {
	public FContext context;
	public BaseXHelper bxhelper;

	public FProcessor(FContext fc) {
		context = fc;
		bxhelper = new BaseXHelper(context.ip, context.db);
	}

	public ArrayList<Fragment> apply(Node root) throws Exception {
		bxhelper.open(); 
		root.name = bxhelper.getNodeName(root.gpre + "");
		root.elementSize = bxhelper.getRootSize();
		System.out.println("root.size=" + root.elementSize);

		ArrayList<Fragment> fragments = doFragmentation(Arrays.asList(root), context.maxsize);

		bxhelper.close();
		return fragments;
	}

	ArrayList<Fragment> doFragmentation(List<Node> nodes, int maxsize) throws Exception {
		System.out.printf("nodes.size=%d\n", nodes.size());
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		Fragment curfrag = new Fragment();
		for (Node node : nodes) {
			if (node.elementSize + 1 > maxsize) {
				if (curfrag.size > 0) {
					curfrag.completeFragment();
					fragments.add(curfrag);
				}
				curfrag = new Fragment();
				List<Node> subnodes = bxhelper.getChildren(node);
				fragments.addAll(doFragmentation(subnodes, maxsize));
			} else if (node.elementSize + curfrag.size > maxsize) {
				if (curfrag.size > 0) {
					curfrag.completeFragment();
					fragments.add(curfrag);
				}
				curfrag = new Fragment();
				curfrag.add(node);
			} else {
				curfrag.add(node);
			}
		}

		if (curfrag.size > 0) {
			curfrag.completeFragment();
			fragments.add(curfrag);
		}

		for (int i = 0; i < fragments.size(); i++)
			fragments.get(i).fid = i;

		
		
		System.gc();

		return fragments;
	}

	public void writeXMLDocuments(MergedTree tree, String path) throws Exception {
		bxhelper.open();

		FileWriter fw = new FileWriter(path);
		String rootName = bxhelper.getNodeName("1");
		fw.write("<" + rootName + ">");

		
		// write fragments of the current merged tree into stream
		for (Fragment frag : tree.fragments) {
			// root path
			for (int i = 1; i < frag.rootPath.size(); i++) {
				Node ni = frag.rootPath.get(i);
				fw.write("<" + ni.name + ">");
			} 

			// Save main contnet of subtrees.
			int size = 1000; // maximun size of subtrees that being process at a time. 
			StringBuilder sb = null;
			int pos = 0;
			int counter = 0;
			while (pos < frag.subtreegpres.length) {
				if (sb == null) {
					sb = new StringBuilder(size * 10);
					sb.append("xquery for $pre in (");
				}

				sb.append(frag.subtreegpres[pos] + ",");

				if (++counter == size || pos == frag.subtreegpres.length - 1) {
					sb.setCharAt(sb.length() - 1, ')');
					sb.append(String.format(" return db:open-pre('%s', $pre)", bxhelper.db));
					// System.out.println(sb);
					fw.append(bxhelper.execute(sb.toString()));
					// fw.flush(); // it does not work.
					fw.close();
					fw = new FileWriter(path, true);
					sb = null;
				} 
				pos++;
			}

			// for (int i = 0; i < frag.subtreegpres.length; i++)
			// fw.append(bxhelper.getNodeContent(frag.subtreegpres[i] + ""));

			// close roots
			for (int i = frag.rootPath.size() - 1; i > 0; i--) {
				fw.write("</" + frag.rootPath.get(i).name + ">");
			}
		}
		fw.write("</" + rootName + ">");

		fw.close();
		bxhelper.close();
	}

	public void makeXDocs1(MergedTree tree, String path) throws Exception {
		bxhelper.open();

		FileWriter fw = new FileWriter(path);
		String rootName = bxhelper.getNodeName("1");
		fw.write("<" + rootName + ">");

		// write fragments of the current merged tree into stream
		for (Fragment frag : tree.fragments) {
			// root path
			for (int i = 1; i < frag.rootPath.size(); i++) {
				Node ni = frag.rootPath.get(i);
				ni.name = bxhelper.getNodeName(ni.gpre + "");
				fw.write("<" + ni.name + ">");
			}

			// subtrees
			for (int i = 0; i < frag.subtrees.size(); i++)
				fw.append(bxhelper.getNodeContent(frag.subtrees.get(i).gpre + ""));

			bxhelper.writeTrees(frag.subtrees, fw);

			// String.format("xquery db:open-pre('%s', %s)%s", database, pre, query)

			// close roots
			for (int i = frag.rootPath.size() - 1; i > 0; i--) {
				fw.write("</" + frag.rootPath.get(i).name + ">");
			}
		}
		fw.write("</" + rootName + ">");

		fw.close();
		bxhelper.close();
	}

	/**
	 * Return the structure of the root part.
	 * 
	 * @param trees
	 * @param links
	 * @return
	 * @throws Exception
	 */
	public String getRootInfo(MergedTree[] trees, FragmentIndex[] links) throws Exception {
		ArrayList<String> pathlist = new ArrayList<String>();
		for (FragmentIndex lk : links) {
			String path = trees[lk.mid].fragments.get(lk.mrank).getPathToRoot();
			if (pathlist.size() == 0 || !pathlist.get(pathlist.size() - 1).equals(path))
				pathlist.add(path);
		}

		StringBuilder sb = new StringBuilder();
		for (String path : pathlist)
			sb.append(path + "\n");

		String[] uniquepres = common.getUniqueStrings(sb.toString().replace('\n', '.').split("\\."));
		sb.append("----\n");
		bxhelper.open();
		for (String pre : uniquepres)
			sb.append(pre + ":" + bxhelper.getNodeName(pre) + "\n");

		bxhelper.close();

		return sb.toString();
	}
}
