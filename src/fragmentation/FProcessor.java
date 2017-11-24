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
		System.out.println("Start. root.size=" + root.elementSize);

		ArrayList<Fragment> fragments = doFragmentation(Arrays.asList(root), context.maxsize);

		bxhelper.close();
		return fragments;
	}

	ArrayList<Fragment> doFragmentation(List<Node> nodes, int maxsize) throws Exception {
		System.out.println("doFragmentation.nodes: input size=" + nodes.size());
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

		return fragments;
	}

	public void makeXDocs(MergedTree tree, String path) throws Exception {
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
				fw.append(bxhelper.getNodeContent(frag.subtrees.get(i).toPREString()));

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
				fw.append(bxhelper.getNodeContent(frag.subtrees.get(i).toPREString()));

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
	public String getRootInfo(MergedTree[] trees, FragmentInfo[] links) throws Exception {
		ArrayList<String> pathlist = new ArrayList<String>();
		for (FragmentInfo lk : links) {
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
