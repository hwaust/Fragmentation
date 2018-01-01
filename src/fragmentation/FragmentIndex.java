package fragmentation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import utils.common;

public class FragmentIndex {
	public int fid;
	public int mid;
	public int mrank;
	public int mpre;
	public int gpre;
	public int size;

	/**
	 * Intermediate results.
	 */
	public ArrayList<String> results;

	public FragmentIndex() {
		results = new ArrayList<String>();
	}

	/**
	 * the order on the original tree, tree id and new order on its merged tree.
	 * 
	 * @param order
	 * @param mid
	 * @param position
	 */
	public FragmentIndex(int fid, int mid, int mrank, int size, int gpre, int mpre) {
		this.fid = fid;
		this.mid = mid;
		this.mrank = mrank;
		this.size = size;
		this.gpre = gpre;
		this.mpre = mpre;
	}

	public String toString() {
		return String.format("fid=%d,mid=%d,mrank=%d,size=%d,gpre=%d,mpre=%d", fid, mid, mrank, size, gpre, mpre);
	}

	public static FragmentIndex parse(String s) {
		FragmentIndex link = new FragmentIndex();
		try {
			String[] strs = s.split(",");
			link.fid = Integer.parseInt(strs[0].split("=")[1]);
			link.mid = Integer.parseInt(strs[1].split("=")[1]);
			link.mrank = Integer.parseInt(strs[2].split("=")[1]);
		} catch (Exception ex) {
			link = null;
			ex.printStackTrace();
		}

		return link;
	}

	public static void writeAllInfo(FragmentIndex[] fis, String filepath) {
		StringBuilder sb = new StringBuilder();
		for (FragmentIndex fi : fis)
			sb.append(fi.toString() + "\n");

		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1); // remove last '\n';
		
		common.saveStringtoFile(sb.toString(), filepath);
	}

	public static FragmentIndex[] readLinks(String file) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(file));
		ArrayList<FragmentIndex> links = new ArrayList<FragmentIndex>();
		for (String s : lines) {
			links.add(FragmentIndex.parse(s));
		}

		FragmentIndex[] linkarray = new FragmentIndex[links.size()];
		for (int i = 0; i < links.size(); i++)
			linkarray[i] = links.get(i);

		return linkarray;
	}

}
