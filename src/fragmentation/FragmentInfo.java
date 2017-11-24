package fragmentation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import basex.common;

public class FragmentInfo {
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

	public FragmentInfo() {
		results = new ArrayList<String>();
	}

	/**
	 * the order on the original tree, tree id and new order on its merged tree.
	 * 
	 * @param order
	 * @param mid
	 * @param position
	 */
	public FragmentInfo(int fid, int mid, int mrank, int size, int gpre, int mpre) {
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

	public static FragmentInfo parse(String s) {
		FragmentInfo link = new FragmentInfo();
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

	public static void writeAllInfo(FragmentInfo[] fis, String filepath) {
		StringBuilder sb = new StringBuilder();
		for (FragmentInfo fi : fis)
			sb.append(fi.toString() + "\n");
		common.saveStringtoFile(sb.toString(), filepath);
	}

	public static FragmentInfo[] readLinks(String file) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(file));
		ArrayList<FragmentInfo> links = new ArrayList<FragmentInfo>();
		for (String s : lines) {
			links.add(FragmentInfo.parse(s));
		}

		FragmentInfo[] linkarray = new FragmentInfo[links.size()];
		for (int i = 0; i < links.size(); i++)
			linkarray[i] = links.get(i);

		return linkarray;
	}

}
